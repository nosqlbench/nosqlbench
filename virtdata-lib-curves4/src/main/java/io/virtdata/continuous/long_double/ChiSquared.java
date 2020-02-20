package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ChiSquaredDistribution;

@ThreadSafeMapper
public class ChiSquared extends LongToDoubleContinuousCurve {
    public ChiSquared(double degreesOfFreedom, String... mods) {
        super(new ChiSquaredDistribution(degreesOfFreedom), mods);
    }
}
