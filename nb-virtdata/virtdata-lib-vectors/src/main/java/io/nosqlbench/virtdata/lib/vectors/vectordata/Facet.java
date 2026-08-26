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
import io.nosqlbench.vectordata.VectorReader;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/*
 * Random access to any named facet of a dataset profile — standard
 * facets by canonical name or alias, and custom facets by their own
 * names. Fixed vector facets yield arrays (float[], int[], ...) and
 * scalar facets yield Number values. An optional record window clips
 * the reader and is warmed according to the prefetch mode ('eager',
 * 'background', or 'none'). */
@ThreadSafeMapper
@Categories(Category.vectors)
public class Facet extends CoreVectors<Object> {

    @Example({"Facet('exampledataset:exampleprofile','metadata_results')",
        "Random access to any named facet, prebuffering (by default) before resuming"})
    public Facet(String datasetAndProfile, String facetName) {
        super(datasetAndProfile, facetName, "", "eager", VectorDataSettings.defaults());
    }

    @Example({"Facet('exampledataset:exampleprofile','base_vectors','[0..100k)')",
        "Read only a record window of the facet, warming exactly that window before resuming"})
    public Facet(String datasetAndProfile, String facetName, String window) {
        super(datasetAndProfile, facetName, window, "eager", VectorDataSettings.defaults());
    }

    @Example({"Facet('exampledataset:exampleprofile','base_vectors','[0..100k)','background')",
        "Read a window while it is warmed on another thread; 'eager', 'background', and 'none' select the prefetch mode"})
    public Facet(String datasetAndProfile, String facetName, String window, String prefetchMode) {
        super(datasetAndProfile, facetName, window, prefetchMode, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public Facet(String datasetAndProfile, String facetName, String window, String prefetchMode,
                 VectorDataSettings settings) {
        super(datasetAndProfile, facetName, window, prefetchMode, settings);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected VectorReader<Object> getRandomAccessData() {
        return (VectorReader<Object>) tdv.openFacet(facetName);
    }

    @Override
    public Object apply(long value) {
        return super.apply(value);
    }
}
