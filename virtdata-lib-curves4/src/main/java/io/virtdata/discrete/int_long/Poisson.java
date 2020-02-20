package io.virtdata.discrete.int_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PoissonDistribution;

@ThreadSafeMapper
public class Poisson extends IntToLongDiscreteCurve {
    public Poisson(double p, String... modslist) {
        super(new PoissonDistribution(p), modslist);
    }
}
