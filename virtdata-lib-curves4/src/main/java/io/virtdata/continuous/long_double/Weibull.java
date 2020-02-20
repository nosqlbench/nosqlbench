package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.WeibullDistribution;

@ThreadSafeMapper
public class Weibull extends LongToDoubleContinuousCurve {
    public Weibull(double alpha, double beta, String... mods) {
        super(new WeibullDistribution(alpha, beta), mods);
    }
}
