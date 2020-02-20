package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GumbelDistribution;

@ThreadSafeMapper
public class Gumbel extends LongToDoubleContinuousCurve {
    public Gumbel(double mu, double beta, String... mods) {
        super(new GumbelDistribution(mu, beta), mods);
    }
}
