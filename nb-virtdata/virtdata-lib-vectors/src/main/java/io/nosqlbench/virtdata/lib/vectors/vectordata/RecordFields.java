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


import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.binding.BindType;
import io.nosqlbench.vectordata.binding.Binder;
import io.nosqlbench.vectordata.binding.Layout;
import io.nosqlbench.vectordata.records.RecordFacet;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Binds each record of a facet of opaque records — a slab of metadata —
 * to a map of parameter name to Java value, in bind order: every field
 * under its own name, or a comma-separated selection in the order
 * given. Names are resolved to positions once, when the function is
 * built, so the per-cycle path reads values in place and never looks
 * at a field name. The cycle value is the record ordinal. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class RecordFields extends CoreRecords<Map<String, Object>> {

    /// Compiles the binder a function drives, against the facet and
    /// the layout learned from it.
    @FunctionalInterface
    interface Compilation { Binder compile(RecordFacet facet, Layout layout); }

    private final Binder binder;

    @Example({"RecordFields('exampledataset:exampleprofile','metadata_content')",
        "Bind every field of each metadata record, under its own name, in the record's order"})
    public RecordFields(String datasetAndProfile, String facetName) {
        this(datasetAndProfile, facetName, "", VectorDataSettings.defaults());
    }

    @Example({"RecordFields('exampledataset:exampleprofile','metadata_content','id,tag')",
        "Bind the named fields, in the order given"})
    public RecordFields(String datasetAndProfile, String facetName, String fields) {
        this(datasetAndProfile, facetName, fields, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public RecordFields(String datasetAndProfile, String facetName, String fields, VectorDataSettings settings) {
        this(datasetAndProfile, facetName, settings, (facet, layout) -> {
            List<String> names = split(fields);
            return names.isEmpty() ? Binder.all(layout) : Binder.select(layout, names);
        });
    }

    RecordFields(String datasetAndProfile, String facetName, VectorDataSettings settings, Compilation compilation) {
        super(datasetAndProfile, facetName, settings);
        binder = compilation.compile(facet, layout);
    }

    static List<String> split(String fields) {
        List<String> names = new ArrayList<>();
        if (fields == null) return names;
        for (String name : fields.split(",")) if (!name.isBlank()) names.add(name.trim());
        return names;
    }

    /// Parameter names, in bind order — the keys of every map this
    /// function returns. Read once, to prepare a statement.
    public List<String> parameters() { return binder.parameters(); }

    /// What each parameter binds as, in bind order.
    public List<BindType> types() { return binder.types(); }

    @Override
    public Map<String, Object> apply(long ordinal) {
        List<String> parameters = binder.parameters();
        Object[] values = new Object[parameters.size()];
        binder.bindEach(facet.recordBytes(ordinal), (slot, field) -> values[slot] = RecordProjections.of(field));
        Map<String, Object> bound = new LinkedHashMap<>(values.length * 2);
        for (int slot = 0; slot < values.length; slot++) bound.put(parameters.get(slot), values[slot]);
        return bound;
    }
}
