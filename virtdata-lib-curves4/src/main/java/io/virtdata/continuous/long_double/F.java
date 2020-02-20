package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.FDistribution;

@ThreadSafeMapper
public class F extends LongToDoubleContinuousCurve {
    public F(double numeratorDegreesOfFreedom, double denominatorDegreesOfFreedom, String... mods) {
        super(new FDistribution(numeratorDegreesOfFreedom, denominatorDegreesOfFreedom), mods);
    }
}
