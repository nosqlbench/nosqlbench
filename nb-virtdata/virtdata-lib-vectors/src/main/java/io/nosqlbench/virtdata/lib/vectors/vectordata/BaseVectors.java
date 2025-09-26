package io.nosqlbench.virtdata.lib.vectors.vectordata;

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
