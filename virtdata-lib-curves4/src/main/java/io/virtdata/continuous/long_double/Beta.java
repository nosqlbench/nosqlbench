package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.BetaDistribution;

@ThreadSafeMapper
public class Beta extends LongToDoubleContinuousCurve {
    public Beta(double alpha, double beta, String... mods) {
        super(new BetaDistribution(alpha, beta), mods);
    }
}
