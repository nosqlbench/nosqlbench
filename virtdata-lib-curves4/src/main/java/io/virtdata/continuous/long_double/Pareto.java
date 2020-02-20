package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ParetoDistribution;

@ThreadSafeMapper
public class Pareto extends LongToDoubleContinuousCurve {
    public Pareto(double scale, double shape, String... mods) {
        super(new ParetoDistribution(scale, shape), mods);
    }
}
