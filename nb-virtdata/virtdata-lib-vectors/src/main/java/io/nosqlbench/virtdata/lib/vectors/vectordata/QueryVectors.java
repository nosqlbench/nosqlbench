package io.nosqlbench.virtdata.lib.vectors.vectordata;

import io.nosqlbench.vectordata.spec.datasets.types.FloatVectors;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

@ThreadSafeMapper
@Categories(Category.vectors)
public class QueryVectors extends CoreVectors<float[]> {

    @Example({"QueryVectors('exampledataset:exampleprofile',false)",
        "Find and download vectordata for query vectors, prebuffering (by default) before resuming"})
    public QueryVectors(String datasetAndProfile) {
        super(datasetAndProfile, true);
    }
    @Example({"QueryVectors('exampledataset:exampleprofile',false)",
        "Find and download vectordata for query vectors, with demand-paged access"})
    public QueryVectors(String datasetAndProfile, boolean prebuffer) {
        super (datasetAndProfile, prebuffer);
    }


    @Override
    protected FloatVectors getRandomAccessData() {
        return super.tdv.getQueryVectors().orElseThrow(()->new RuntimeException("Cannot get query vectors from data view " + super.tdv.getName()));
    }

    @Override
    public float[] apply(long value) {
        return super.apply(value);
    }
}
