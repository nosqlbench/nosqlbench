package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.CauchyDistribution;

@ThreadSafeMapper
public class Cauchy extends LongToDoubleContinuousCurve {
    public Cauchy(double median, double scale, String... mods) {
        super(new CauchyDistribution(median, scale), mods);
    }
}
