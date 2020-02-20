package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.NormalDistribution;

@ThreadSafeMapper
public class Normal extends LongToDoubleContinuousCurve {
    public Normal(double mean, double sd, String... mods) {
        super(new NormalDistribution(mean, sd), mods);
    }
}
