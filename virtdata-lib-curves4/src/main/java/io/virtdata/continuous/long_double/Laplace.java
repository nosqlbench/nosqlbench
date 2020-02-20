package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LaplaceDistribution;

@ThreadSafeMapper
public class Laplace extends LongToDoubleContinuousCurve {
    public Laplace(double mu, double beta, String... mods) {
        super(new LaplaceDistribution(mu, beta), mods);
    }
}
