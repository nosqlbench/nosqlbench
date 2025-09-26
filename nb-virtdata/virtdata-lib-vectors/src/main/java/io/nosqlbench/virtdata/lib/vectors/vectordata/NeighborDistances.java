package io.nosqlbench.virtdata.lib.vectors.vectordata;

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
