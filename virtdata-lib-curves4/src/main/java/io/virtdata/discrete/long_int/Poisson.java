package io.virtdata.discrete.long_int;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PoissonDistribution;

@ThreadSafeMapper
public class Poisson extends LongToIntDiscreteCurve {
    public Poisson(double p, String... modslist) {
        super(new PoissonDistribution(p), modslist);
    }
}
