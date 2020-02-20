package io.virtdata.discrete.long_int;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.BinomialDistribution;

@ThreadSafeMapper
public class Binomial extends LongToIntDiscreteCurve {
    public Binomial(int trials, double p, String... modslist) {
        super(new BinomialDistribution(trials, p), modslist);
    }
}
