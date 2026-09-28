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
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/*
 * Projects one named field of each record in a facet of opaque records
 * — a slab of metadata — to the Java value its wire type says it is:
 * integers by width, floats by width, text as String, timestamps as
 * Instant, UUIDs as UUID, blobs as byte[], containers as lists, sets,
 * and maps. The field is resolved to its position once, when the
 * function is built; a field the facet lacks is refused then, naming
 * the fields it has. The cycle value is the record ordinal. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class RecordField extends CoreRecords<Object> {

    private final Binder binder;

    @Example({"RecordField('exampledataset:exampleprofile','metadata_content','id')",
        "Bind the 'id' field of each metadata record, typed as the record types it"})
    public RecordField(String datasetAndProfile, String facetName, String field) {
        this(datasetAndProfile, facetName, field, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public RecordField(String datasetAndProfile, String facetName, String field, VectorDataSettings settings) {
        super(datasetAndProfile, facetName, settings);
        binder = Binder.select(layout, field);
    }

    /// What the projected value binds as, from the field's tag.
    public BindType bindType() { return binder.types().get(0); }

    @Override
    public Object apply(long ordinal) {
        return RecordProjections.of(binder.bind(facet.recordBytes(ordinal)).get(0));
    }
}
