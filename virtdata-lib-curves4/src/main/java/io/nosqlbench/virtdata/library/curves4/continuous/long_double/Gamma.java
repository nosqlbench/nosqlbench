package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GammaDistribution;

@ThreadSafeMapper
public class Gamma extends LongToDoubleContinuousCurve {
    public Gamma(double shape, double scale, String... mods) {
        super(new GammaDistribution(shape, scale), mods);
    }
}
