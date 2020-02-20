package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LogisticDistribution;

@ThreadSafeMapper
public class Logistic extends LongToDoubleContinuousCurve {
    public Logistic(double mu, double scale, String... mods) {
        super(new LogisticDistribution(mu, scale), mods);
    }
}
