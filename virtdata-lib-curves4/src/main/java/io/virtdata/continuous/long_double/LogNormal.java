package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LogNormalDistribution;

@ThreadSafeMapper
public class LogNormal extends LongToDoubleContinuousCurve {
    public LogNormal(double scale, double shape, String... mods) {
        super(new LogNormalDistribution(scale, shape), mods);
    }
}
