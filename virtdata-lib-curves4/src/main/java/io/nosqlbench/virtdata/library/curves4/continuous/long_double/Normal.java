package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.NormalDistribution;

@ThreadSafeMapper
public class Normal extends LongToDoubleContinuousCurve {
    public Normal(double mean, double sd, String... mods) {
        super(new NormalDistribution(mean, sd), mods);
    }
}
