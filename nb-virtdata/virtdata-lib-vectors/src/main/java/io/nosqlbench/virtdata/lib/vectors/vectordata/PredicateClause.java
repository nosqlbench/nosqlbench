package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.vectordata.Catalog;
import io.nosqlbench.vectordata.CatalogSources;
import io.nosqlbench.vectordata.FacetNames;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.anode.ANode;
import io.nosqlbench.vectordata.anode.ANodeException;
import io.nosqlbench.vectordata.anode.PNode;
import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.binding.BindException;
import io.nosqlbench.vectordata.binding.Condition;
import io.nosqlbench.vectordata.binding.Layout;
import io.nosqlbench.vectordata.binding.PredicateBinder;
import io.nosqlbench.vectordata.records.RecordFacet;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.core.templates.PreparedFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;

/*
 * Reifies each predicate of a predicate facet into a prepared-statement
 * fragment: the WHERE clause of the predicate's shape with a marker per
 * comparand, the comparands as bind values typed from the metadata
 * fields they constrain, and the predicate's fingerprint as the form
 * key. A facet whose predicates differ in shape from query to query
 * yields one form per shape, and each form is initialized once — its
 * clause text composed and its binder compiled against the metadata
 * layout on the first predicate of that shape — after which a cycle
 * costs the record read, the fingerprint lookup, and the value
 * projection. Placed at a bind point of a prepared statement, the
 * fragment has the adapter prepare one statement per form and bind the
 * values through it. Only flat conjunctions over named fields have a
 * clause; '!=' is refused, since CQL has no such relation, and 'IN'
 * binds a list. The cycle value is the predicate record's ordinal. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class PredicateClause implements LongFunction<PreparedFragment> {

    /// One shape of predicate, initialized once: its clause, and the
    /// binder that types and orders its values.
    record Form(String key, String text, PredicateBinder binder, int arity) { }

    private final TestDataView tdv;
    private final RecordFacet predicates;
    private final Layout layout;
    private final ConcurrentHashMap<String, Form> forms = new ConcurrentHashMap<>();

    @Example({"PredicateClause('exampledataset:exampleprofile','metadata_predicates','metadata_content')",
        "Each predicate as a prepared WHERE fragment — 'year >= ? AND topic = ?' with its values — one prepared form per predicate shape"})
    public PredicateClause(String datasetAndProfile, String predicateFacet, String metadataFacet) {
        this(datasetAndProfile, predicateFacet, metadataFacet, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public PredicateClause(String datasetAndProfile, String predicateFacet, String metadataFacet, VectorDataSettings settings) {
        tdv = Catalog.of(CatalogSources.defaults(), settings).openProfile(datasetAndProfile);
        predicates = tdv.openFacetRecords(FacetNames.canonical(predicateFacet));
        layout = Layout.discover(tdv.openFacetRecords(FacetNames.canonical(metadataFacet)));
    }

    /// The forms initialized so far: fingerprint to clause text. Grows
    /// as shapes are met; complete once every distinct shape has been
    /// applied.
    public Map<String, String> forms() {
        Map<String, String> out = new LinkedHashMap<>();
        forms.forEach((key, form) -> out.put(key, form.text()));
        return out;
    }

    @Override
    public PreparedFragment apply(long ordinal) {
        ANode node;
        try { node = ANode.decode(predicates.recordBytes(ordinal)); }
        catch (ANodeException e) { throw BindException.record(e.getMessage()); }
        if (!(node instanceof ANode.P p))
            throw BindException.record("facet '" + predicates.name() + "' record " + ordinal + " is not a predicate — its dialect byte says MNode");
        PNode predicate = p.node();
        Form form = forms.computeIfAbsent(predicate.fingerprint().display(), key -> initialize(key, predicate));
        Object[] values = new Object[form.arity()];
        int[] slot = {0};
        form.binder().bindEach(predicate, (condition, comparands) -> values[slot[0]++] = value(condition, comparands));
        return PreparedFragment.of(form.key(), form.text(), values);
    }

    /// Composes a shape's clause and compiles its binder. Once per shape.
    private Form initialize(String key, PNode sample) {
        PredicateBinder binder = PredicateBinder.compile(sample, layout);
        List<String> relations = new ArrayList<>();
        for (Condition c : binder.conditions()) relations.add(relation(c));
        return new Form(key, String.join(" AND ", relations), binder, binder.conditions().size());
    }

    /// `field op ?` in CQL: `IN ?` binds a list, `MATCHES` is `LIKE`,
    /// and `!=` has no CQL relation to become.
    static String relation(Condition c) {
        return switch (c.op()) {
            case IN -> c.field() + " IN ?";
            case MATCHES -> c.field() + " LIKE ?";
            case NE -> throw BindException.record("condition '" + c.field() + " != …' has no CQL relation to prepare; CQL cannot express it");
            default -> c.field() + " " + c.op().symbol() + " ?";
        };
    }

    private static Object value(Condition condition, List<Comparand> comparands) {
        if (condition.op() != PNode.OpType.IN && comparands.size() == 1) return RecordProjections.of(comparands.get(0), condition.bindType());
        List<Object> values = new ArrayList<>(comparands.size());
        for (Comparand c : comparands) values.add(RecordProjections.of(c, condition.bindType()));
        return values;
    }
}
