package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.UniformContinuousDistribution;

@ThreadSafeMapper
public class Uniform extends LongToDoubleContinuousCurve {
    public Uniform(double lower, double upper, String... mods) {
        super(new UniformContinuousDistribution(lower, upper), mods);
    }
}
