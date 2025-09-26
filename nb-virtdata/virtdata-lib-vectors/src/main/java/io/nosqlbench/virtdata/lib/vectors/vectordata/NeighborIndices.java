package io.nosqlbench.virtdata.lib.vectors.vectordata;

import io.nosqlbench.vectordata.spec.datasets.types.FloatVectors;
import io.nosqlbench.vectordata.spec.datasets.types.IntVectors;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

@ThreadSafeMapper
@Categories(Category.vectors)
public class NeighborIndices extends CoreVectors<int[]> {

    @Example({"NeighborIndices('exampledataset:exampleprofile',false)",
        "Find and download vectordata for neighbor indices, prebuffering (by default) before resuming"})
    public NeighborIndices(String datasetAndProfile) {
        super(datasetAndProfile, true);
    }
    @Example({"NeighborIndices('exampledataset:exampleprofile',false)",
        "Find and download vectordata for neighbor indices, with demand-paged access"})
    public NeighborIndices(String datasetAndProfile, boolean prebuffer) {
        super (datasetAndProfile, prebuffer);
    }

    @Override
    protected IntVectors getRandomAccessData() {
        return super.tdv.getNeighborIndices().orElseThrow(()->new RuntimeException("Cannot get neighbor indices from data view " + super.tdv.getName()));
    }

    @Override
    public int[] apply(long value) {
        return super.apply(value);
    }
}
