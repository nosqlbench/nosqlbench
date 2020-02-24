package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ChiSquaredDistribution;

@ThreadSafeMapper
public class ChiSquared extends LongToDoubleContinuousCurve {
    public ChiSquared(double degreesOfFreedom, String... mods) {
        super(new ChiSquaredDistribution(degreesOfFreedom), mods);
    }
}
