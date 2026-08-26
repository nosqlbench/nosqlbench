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
import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.FacetNames;
import io.nosqlbench.vectordata.PrefetchHandle;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.VvecReader;
import io.nosqlbench.vectordata.WholeFacetFallback;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/*
 * Random access to a variable-dimension (vvec) facet of a dataset
 * profile, indexed through its IDXFOR offset sidecar. Unlike fixed
 * facets, variable-length readers have no windowed form, so an
 * optional record window here selects what to WARM — through the
 * offset index, exactly the bytes those records occupy — while
 * indices remain absolute. The prefetch mode is 'eager',
 * 'background', or 'none'. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class VariableFacet implements LongFunction<Object> {

    private final TestDataView tdv;
    private final String facetName;
    private final VvecReader<?> reader;
    private final PrefetchHandle backgroundPrefetch;

    @Example({"VariableFacet('exampledataset:exampleprofile','metadata_content')",
        "Random access to variable-dimension records, prebuffering (by default) before resuming"})
    public VariableFacet(String datasetAndProfile, String facetName) {
        this(datasetAndProfile, facetName, "", "eager", VectorDataSettings.defaults());
    }

    @Example({"VariableFacet('exampledataset:exampleprofile','metadata_content','[0..100k)')",
        "Warm the records of a window ahead of reading them; indices remain absolute"})
    public VariableFacet(String datasetAndProfile, String facetName, String window) {
        this(datasetAndProfile, facetName, window, "eager", VectorDataSettings.defaults());
    }

    @Example({"VariableFacet('exampledataset:exampleprofile','metadata_content','[0..100k)','background')",
        "Warm a window on another thread while reads proceed; 'eager', 'background', and 'none' select the prefetch mode"})
    public VariableFacet(String datasetAndProfile, String facetName, String window, String prefetchMode) {
        this(datasetAndProfile, facetName, window, prefetchMode, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public VariableFacet(String datasetAndProfile, String facetName, String window, String prefetchMode,
                         VectorDataSettings settings) {
        tdv = Catalog.of(CatalogSources.defaults(), settings).openProfile(datasetAndProfile);
        this.facetName = FacetNames.canonical(facetName);
        reader = tdv.openVariableFacet(this.facetName);
        DSWindow parsed = DSWindow.parse(window == null ? "" : window);
        backgroundPrefetch = switch (CoreVectors.Prefetch.parse(prefetchMode)) {
            case NONE -> null;
            case BACKGROUND -> tdv.prefetchInBackground(this.facetName, parsed, WholeFacetFallback.REFUSE);
            case EAGER -> {
                // A windowless eager warm-up uses the reader's own
                // prebuffer, which also drives the offset sidecar to
                // resident; a windowed one resolves through the index.
                if (parsed.isEmpty()) reader.prebuffer();
                else tdv.prefetch(this.facetName, parsed, WholeFacetFallback.REFUSE);
                yield null;
            }
        };
    }

    /** The handle for a 'background' prefetch, or null in the other modes. */
    public PrefetchHandle backgroundPrefetch() { return backgroundPrefetch; }

    @Override
    public Object apply(long value) {
        return reader.get(value);
    }
}
