package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LaplaceDistribution;

@ThreadSafeMapper
public class Laplace extends LongToDoubleContinuousCurve {
    public Laplace(double mu, double beta, String... mods) {
        super(new LaplaceDistribution(mu, beta), mods);
    }
}
