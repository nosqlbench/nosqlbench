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
import io.nosqlbench.vectordata.binding.Form;
import io.nosqlbench.vectordata.binding.Forms;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/*
 * Binds each record of a facet of opaque records under a form the
 * facet declares in its 'forms' namespace — the fields that form
 * binds, in its order, under the parameter names it gives them. A
 * facet declaring no forms offers one implicit form, 'default', which
 * binds every field under its own name; a form the facet does not
 * offer is refused when the function is built, naming the ones it
 * does. The cycle value is the record ordinal. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class RecordForm extends RecordFields {

    private final Form form;

    @Example({"RecordForm('exampledataset:exampleprofile','metadata_content','row')",
        "Bind each metadata record as the facet's 'row' form declares"})
    public RecordForm(String datasetAndProfile, String facetName, String formName) {
        this(datasetAndProfile, facetName, formName, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public RecordForm(String datasetAndProfile, String facetName, String formName, VectorDataSettings settings) {
        this(datasetAndProfile, facetName, settings, new Form[1], formName);
    }

    private RecordForm(String datasetAndProfile, String facetName, VectorDataSettings settings, Form[] selected, String formName) {
        super(datasetAndProfile, facetName, settings, (facet, layout) -> {
            selected[0] = Forms.byName(facet, formName);
            return selected[0].binder(layout);
        });
        form = selected[0];
    }

    /// The form this function binds under, as the facet declares it.
    public Form form() { return form; }
}
