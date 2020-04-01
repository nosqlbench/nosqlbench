package io.nosqlbench.virtdata.library.curves4.discrete.long_long;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.UniformDiscreteDistribution;

@ThreadSafeMapper
public class Uniform extends LongToLongDiscreteCurve {
    public Uniform(int lower, int upper, String... modslist) {
        super(new UniformDiscreteDistribution(lower, upper), modslist);
    }
}
