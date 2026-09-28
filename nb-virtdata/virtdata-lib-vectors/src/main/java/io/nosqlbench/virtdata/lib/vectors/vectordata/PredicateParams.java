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
import io.nosqlbench.vectordata.anode.PNode;
import io.nosqlbench.vectordata.anode.PNode.Comparand;
import io.nosqlbench.vectordata.anode.PNode.OpType;
import io.nosqlbench.vectordata.anode.PNodeDisplay;
import io.nosqlbench.vectordata.binding.BindException;
import io.nosqlbench.vectordata.binding.Condition;
import io.nosqlbench.vectordata.binding.Layout;
import io.nosqlbench.vectordata.binding.PredicateBinder;
import io.nosqlbench.vectordata.records.RecordFacet;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

/*
 * Binds each predicate record of a predicate facet to the parameters
 * of a filter prepared once: a map of parameter name to comparand
 * value, in condition order, each typed from the metadata field the
 * condition constrains rather than from the comparand. The filter's
 * shape comes from a template — the facet's first predicate, or one
 * written in the display grammar, '(created >= 0 AND count > 0)' — and
 * must be a flat conjunction over named fields; a record of another
 * shape is refused rather than bound into the wrong conditions. A
 * membership condition ('IN') binds a list. The cycle value is the
 * predicate record's ordinal. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class PredicateParams implements LongFunction<Map<String, Object>> {

    private final TestDataView tdv;
    private final RecordFacet predicates;
    private final PredicateBinder binder;

    @Example({"PredicateParams('exampledataset:exampleprofile','metadata_predicates','metadata_content')",
        "Bind each predicate's comparands as parameters, in the shape of the facet's first predicate, typed from the metadata fields"})
    public PredicateParams(String datasetAndProfile, String predicateFacet, String metadataFacet) {
        this(datasetAndProfile, predicateFacet, metadataFacet, "", VectorDataSettings.defaults());
    }

    @Example({"PredicateParams('exampledataset:exampleprofile','metadata_predicates','metadata_content','(created >= 0 AND count > 0)')",
        "Bind each predicate's comparands in the shape of a template written in the display grammar"})
    public PredicateParams(String datasetAndProfile, String predicateFacet, String metadataFacet, String template) {
        this(datasetAndProfile, predicateFacet, metadataFacet, template, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public PredicateParams(String datasetAndProfile, String predicateFacet, String metadataFacet, String template,
                           VectorDataSettings settings) {
        tdv = Catalog.of(CatalogSources.defaults(), settings).openProfile(datasetAndProfile);
        predicates = tdv.openFacetRecords(FacetNames.canonical(predicateFacet));
        Layout layout = Layout.discover(tdv.openFacetRecords(FacetNames.canonical(metadataFacet)));
        PNode sample = template == null || template.isBlank() ? firstPredicate() : PNodeDisplay.parse(template);
        binder = PredicateBinder.compile(sample, layout);
    }

    private PNode firstPredicate() {
        if (predicates.count() == 0) throw BindException.noLayout(predicates.name());
        ANode first = ANode.decode(predicates.recordBytes(0));
        if (!(first instanceof ANode.P p))
            throw BindException.record("facet '" + predicates.name() + "': its first record is not a predicate — its dialect byte says MNode");
        return p.node();
    }

    /// The conditions the filter binds, in order: field, parameter
    /// name, operator, bind type, and arity. Read once, to build the
    /// statement's WHERE clause with a placeholder per parameter.
    public List<Condition> conditions() { return binder.conditions(); }

    /// The parameter names, in condition order — the keys of every map
    /// this function returns.
    public List<String> parameters() {
        List<String> names = new ArrayList<>();
        for (Condition c : binder.conditions()) names.add(c.parameter());
        return names;
    }

    @Override
    public Map<String, Object> apply(long ordinal) {
        Map<String, Object> bound = new LinkedHashMap<>();
        binder.bindEach(predicates.recordBytes(ordinal), (condition, comparands) -> bound.put(condition.parameter(), value(condition, comparands)));
        return bound;
    }

    private static Object value(Condition condition, List<Comparand> comparands) {
        if (condition.op() != OpType.IN && comparands.size() == 1) return RecordProjections.of(comparands.get(0), condition.bindType());
        List<Object> values = new ArrayList<>(comparands.size());
        for (Comparand c : comparands) values.add(RecordProjections.of(c, condition.bindType()));
        return values;
    }
}
