package io.nosqlbench.virtdata.library.curves4.discrete.long_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.BinomialDistribution;

@ThreadSafeMapper
public class Binomial extends LongToIntDiscreteCurve {
    public Binomial(int trials, double p, String... modslist) {
        super(new BinomialDistribution(trials, p), modslist);
    }
}
