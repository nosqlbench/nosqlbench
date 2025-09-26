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


import io.nosqlbench.vectordata.spec.datasets.types.FloatVectors;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/*
* Random access to vector data from a hosted location. */
@ThreadSafeMapper
@Categories(Category.vectors)
public class BaseVectors extends CoreVectors<float[]> {

    @Example({"BaseVectors('exampledataset:exampleprofile')",
        "Find and download vectordata for base vectors, prebuffering (by default) before resuming"})
    public BaseVectors(String datasetAndProfile) {
        super(datasetAndProfile,true);
    }

    @Example({"BaseVectors('exampledataset:exampleprofile',false)",
    "Find and download vectordata for base vectors, with demand-paged access"})
    public BaseVectors(String datasetAndProfile, boolean prebuffer) {
        super (datasetAndProfile, prebuffer);
    }

    @Override
    protected FloatVectors getRandomAccessData() {
        return super.tdv.getBaseVectors().orElseThrow(()->new RuntimeException("Cannot get base vectors from data view " + super.tdv.getName()));
    }

    @Override
    public float[] apply(long value) {
        return super.apply(value);
    }
}
