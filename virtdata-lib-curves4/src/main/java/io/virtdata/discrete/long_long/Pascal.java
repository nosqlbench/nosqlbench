package io.virtdata.discrete.long_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PascalDistribution;

@ThreadSafeMapper
public class Pascal extends LongToLongDiscreteCurve {
    public Pascal(int r, double p, String... modslist) {
        super(new PascalDistribution(r, p), modslist);
    }
}
