package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.NakagamiDistribution;

@ThreadSafeMapper
public class Nakagami extends LongToDoubleContinuousCurve {
    public Nakagami(double mu, double omega, String... mods) {
        super(new NakagamiDistribution(mu, omega), mods);
    }
}
