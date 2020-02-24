package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LogNormalDistribution;

@ThreadSafeMapper
public class LogNormal extends LongToDoubleContinuousCurve {
    public LogNormal(double scale, double shape, String... mods) {
        super(new LogNormalDistribution(scale, shape), mods);
    }
}
