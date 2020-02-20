package io.virtdata.discrete.long_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PoissonDistribution;

@ThreadSafeMapper
public class Poisson extends LongToLongDiscreteCurve {
    public Poisson(double p, String... modslist) {
        super(new PoissonDistribution(p), modslist);
    }
}
