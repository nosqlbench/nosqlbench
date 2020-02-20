package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LevyDistribution;

@ThreadSafeMapper
public class Levy extends LongToDoubleContinuousCurve { public Levy(double mu, double c, String... mods) { super(new LevyDistribution(mu,c), mods);
    }
}
