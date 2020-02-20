package io.virtdata.discrete.int_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.BinomialDistribution;

@ThreadSafeMapper
public class Binomial extends IntToLongDiscreteCurve {
    public Binomial(int trials, double p, String... modslist) {
        super(new BinomialDistribution(trials, p), modslist);
    }
}
