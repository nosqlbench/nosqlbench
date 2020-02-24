package io.nosqlbench.virtdata.library.curves4.discrete.long_int;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PoissonDistribution;

@ThreadSafeMapper
public class Poisson extends LongToIntDiscreteCurve {
    public Poisson(double p, String... modslist) {
        super(new PoissonDistribution(p), modslist);
    }
}
