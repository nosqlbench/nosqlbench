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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

/*
 * Reifies each predicate of a predicate facet into a prepared-statement
 * fragment: the WHERE clause of the predicate's shape with a marker per
 * comparand, the comparands as bind values typed from the metadata
 * fields they constrain, and the predicate's fingerprint as the form
 * key. The facet is surveyed when the function is built — every
 * predicate read once, every distinct shape initialized: its clause
 * composed and its binder compiled against the metadata layout — and
 * the forms are reported on the console before the first cycle, so how
 * many prepared statements the run will hold is known up front. From
 * then on a cycle costs the record read, the fingerprint lookup, and
 * the value projection. Placed at a bind point of a prepared statement,
 * the fragment has the adapter prepare one statement per form and bind
 * the values through it. Only flat conjunctions over named fields have
 * a clause; '!=' is refused, since CQL has no such relation, and 'IN'
 * binds a list. The cycle value is the predicate record's ordinal. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class PredicateClause implements LongFunction<PreparedFragment> {

    /// One shape of predicate, initialized once: its clause, and the
    /// binder that types and orders its values.
    record Form(String key, String text, PredicateBinder binder, int arity) { }

    /// What a survey of the facet found: how many predicates, and each
    /// form with the number of predicates of that shape, most common
    /// first.
    public record Survey(String facet, long predicates, Map<String, Long> countsByForm, Map<String, String> clauseByForm) {
        /// The number of distinct forms.
        public int forms() { return countsByForm.size(); }

        /// One line per form: count, clause, and the fingerprint.
        public String report() {
            StringBuilder out = new StringBuilder();
            countsByForm.forEach((key, count) -> out.append(String.format("%8d  %s   [%s]%n", count, clauseByForm.get(key), key)));
            return out.toString();
        }

        @Override public String toString() { return forms() + " predicate forms across " + predicates + " predicates in " + facet; }
    }

    private final TestDataView tdv;
    private final RecordFacet predicates;
    private final Layout layout;
    private final Map<String, Form> forms = new LinkedHashMap<>();
    private final Survey survey;

    @Example({"PredicateClause('exampledataset:exampleprofile','metadata_predicates','metadata_content')",
        "Each predicate as a prepared WHERE fragment — 'year >= ? AND topic = ?' with its values — one prepared form per predicate shape, the forms surveyed and reported before the first cycle"})
    public PredicateClause(String datasetAndProfile, String predicateFacet, String metadataFacet) {
        this(datasetAndProfile, predicateFacet, metadataFacet, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public PredicateClause(String datasetAndProfile, String predicateFacet, String metadataFacet, VectorDataSettings settings) {
        this(datasetAndProfile, predicateFacet, metadataFacet, settings, true);
    }

    /// The binding form reports its survey on the console; a survey
    /// taken for its own sake — the `predicateForms` expression — does
    /// not, since a workload evaluates its expressions at every step.
    PredicateClause(String datasetAndProfile, String predicateFacet, String metadataFacet, VectorDataSettings settings, boolean report) {
        tdv = Catalog.of(CatalogSources.defaults(), settings).openProfile(datasetAndProfile);
        predicates = tdv.openFacetRecords(FacetNames.canonical(predicateFacet));
        layout = Layout.discover(tdv.openFacetRecords(FacetNames.canonical(metadataFacet)));
        survey = survey(datasetAndProfile + ":" + FacetNames.canonical(predicateFacet));
        if (report) System.err.printf("[vectordata] %s%n%s", survey, survey.report());
    }

    /// A survey without a binding: the forms a facet holds, quietly.
    public static Survey surveyOf(String datasetAndProfile, String predicateFacet, String metadataFacet) {
        return new PredicateClause(datasetAndProfile, predicateFacet, metadataFacet, VectorDataSettings.defaults(), false).survey();
    }

    /// Reads every predicate once, initializing each shape the first
    /// time it appears and counting the rest. A shape that cannot be
    /// prepared is reported with the others, and refused after the
    /// survey rather than on the cycle that would have met it.
    private Survey survey(String label) {
        Map<String, Long> counts = new LinkedHashMap<>();
        Map<String, String> clauses = new LinkedHashMap<>();
        Map<String, String> refused = new LinkedHashMap<>();
        long total = predicates.count();
        for (long ordinal = 0; ordinal < total; ordinal++) {
            PNode predicate = decode(ordinal);
            String key = predicate.fingerprint().display();
            counts.merge(key, 1L, Long::sum);
            if (forms.containsKey(key) || refused.containsKey(key)) continue;
            try { forms.put(key, initialize(key, predicate)); clauses.put(key, forms.get(key).text()); }
            catch (BindException e) { refused.put(key, e.getMessage()); clauses.put(key, "(refused: " + e.getMessage() + ")"); }
        }
        List<Map.Entry<String, Long>> ordered = new ArrayList<>(counts.entrySet());
        ordered.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        Map<String, Long> byCount = new LinkedHashMap<>();
        Map<String, String> clauseByCount = new LinkedHashMap<>();
        for (Map.Entry<String, Long> e : ordered) { byCount.put(e.getKey(), e.getValue()); clauseByCount.put(e.getKey(), clauses.get(e.getKey())); }
        Survey found = new Survey(label, total, Collections.unmodifiableMap(byCount), Collections.unmodifiableMap(clauseByCount));
        if (!refused.isEmpty()) {
            System.err.printf("[vectordata] %s%n%s", found, found.report());
            throw BindException.record(refused.size() + " of the " + found.forms() + " predicate forms in " + label + " cannot be prepared: " + refused);
        }
        return found;
    }

    private PNode decode(long ordinal) {
        ANode node;
        try { node = ANode.decode(predicates.recordBytes(ordinal)); }
        catch (ANodeException e) { throw BindException.record(e.getMessage()); }
        if (!(node instanceof ANode.P p))
            throw BindException.record("facet '" + predicates.name() + "' record " + ordinal + " is not a predicate — its dialect byte says MNode");
        return p.node();
    }

    /// The survey taken when this function was built.
    public Survey survey() { return survey; }

    /// The forms, fingerprint to clause text, most common first.
    public Map<String, String> forms() { return survey.clauseByForm(); }

    @Override
    public PreparedFragment apply(long ordinal) {
        PNode predicate = decode(ordinal);
        Form form = forms.get(predicate.fingerprint().display());
        if (form == null) throw BindException.record("predicate " + ordinal + " has a shape the survey did not see: " + predicate.display());
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
