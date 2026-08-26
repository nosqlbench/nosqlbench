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


import io.nosqlbench.vectordata.VectorReader;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/*
* Random access to vector data from a hosted location. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class MetadataResults extends CoreVectors<int[]> {

    @Example({"MetadataResults('exampledataset:exampleprofile')",
        "Find and download vectordata for metadata results, prebuffering (by default) before resuming"})
    public MetadataResults(String datasetAndProfile) {
        super(datasetAndProfile, "metadata_results", true, VectorDataSettings.defaults());
    }

    @Example({"MetadataResults('exampledataset:exampleprofile',false)",
    "Find and download vectordata for metadata results, with demand-paged access"})
    public MetadataResults(String datasetAndProfile, boolean prebuffer) {
        super(datasetAndProfile, "metadata_results", prebuffer, VectorDataSettings.defaults());
    }

    @Example({"MetadataResults('exampledataset:exampleprofile','[0..100k)')",
        "Read only the first 100k records, warming exactly that window before resuming"})
    public MetadataResults(String datasetAndProfile, String window) {
        super(datasetAndProfile, "metadata_results", window, "eager", VectorDataSettings.defaults());
    }

    @Example({"MetadataResults('exampledataset:exampleprofile','[0..100k)','background')",
        "Read a window while it is warmed on another thread; 'eager', 'background', and 'none' select the prefetch mode"})
    public MetadataResults(String datasetAndProfile, String window, String prefetchMode) {
        super(datasetAndProfile, "metadata_results", window, prefetchMode, VectorDataSettings.defaults());
    }

    /** Construct with explicit vectordata settings, including an isolated cache location. */
    public MetadataResults(String datasetAndProfile, boolean prebuffer, VectorDataSettings settings) {
        super(datasetAndProfile, "metadata_results", prebuffer, settings);
    }

    /** Construct with a window, prefetch mode, and explicit vectordata settings. */
    public MetadataResults(String datasetAndProfile, String window, String prefetchMode, VectorDataSettings settings) {
        super(datasetAndProfile, "metadata_results", window, prefetchMode, settings);
    }

    @Override
    protected VectorReader<int[]> getRandomAccessData() {
        return super.tdv.metadataResults();
    }

    @Override
    public int[] apply(long value) {
        return super.apply(value);
    }
}
