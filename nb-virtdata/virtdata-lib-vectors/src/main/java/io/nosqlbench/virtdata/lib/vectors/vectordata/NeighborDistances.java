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

@ThreadSafeMapper
@Categories(Category.vectors)
public class NeighborDistances extends CoreVectors<float[]> {

    @Example({"NeighborDistances('exampledataset:exampleprofile',false)",
        "Find and download vectordata for neighbor distances, prebuffering (by default) before resuming"})
    public NeighborDistances(String datasetAndProfile) {
        super(datasetAndProfile, true);
    }
    @Example({"NeighborDistances('exampledataset:exampleprofile',false)",
        "Find and download vectordata for neighbor distances, with demand-paged access"})
    public NeighborDistances(String datasetAndProfile, boolean prebuffer) {
        super (datasetAndProfile, prebuffer);
    }

    @Override
    protected FloatVectors getRandomAccessData() {
        return super.tdv.getNeighborDistances().orElseThrow(()->new RuntimeException("Cannot get neighbor distances from data view " + super.tdv.getName()));
    }

    @Override
    public float[] apply(long value) {
        return super.apply(value);
    }
}
