package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GammaDistribution;

@ThreadSafeMapper
public class Gamma extends LongToDoubleContinuousCurve {
    public Gamma(double shape, double scale, String... mods) {
        super(new GammaDistribution(shape, scale), mods);
    }
}
