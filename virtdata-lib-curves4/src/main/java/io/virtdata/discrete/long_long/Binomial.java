package io.virtdata.discrete.long_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.BinomialDistribution;

@ThreadSafeMapper
public class Binomial extends LongToLongDiscreteCurve {
    public Binomial(int trials, double p, String... modslist) {
        super(new BinomialDistribution(trials, p), modslist);
    }
}
