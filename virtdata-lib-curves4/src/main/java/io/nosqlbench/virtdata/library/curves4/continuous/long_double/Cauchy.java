package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.CauchyDistribution;

@ThreadSafeMapper
public class Cauchy extends LongToDoubleContinuousCurve {
    public Cauchy(double median, double scale, String... mods) {
        super(new CauchyDistribution(median, scale), mods);
    }
}
