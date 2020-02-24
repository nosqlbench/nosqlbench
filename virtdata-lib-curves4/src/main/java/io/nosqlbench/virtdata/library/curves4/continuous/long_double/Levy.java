package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LevyDistribution;

@ThreadSafeMapper
public class Levy extends LongToDoubleContinuousCurve { public Levy(double mu, double c, String... mods) { super(new LevyDistribution(mu,c), mods);
    }
}
