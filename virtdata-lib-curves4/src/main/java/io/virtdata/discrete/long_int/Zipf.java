package io.virtdata.discrete.long_int;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ZipfDistribution;

@ThreadSafeMapper
public class Zipf extends LongToIntDiscreteCurve {
    public Zipf(int numberOfElements, double exponent, String... modslist) {
        super(new ZipfDistribution(numberOfElements, exponent), modslist);
    }
}
