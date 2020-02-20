package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ExponentialDistribution;

@ThreadSafeMapper
public class Exponential extends LongToDoubleContinuousCurve {
    public Exponential(double mean, String... mods) {
        super(new ExponentialDistribution(mean), mods);
    }
}
