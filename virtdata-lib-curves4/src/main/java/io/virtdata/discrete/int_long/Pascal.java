package io.virtdata.discrete.int_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PascalDistribution;

@ThreadSafeMapper
public class Pascal extends IntToLongDiscreteCurve {
    public Pascal(int r, double p, String... modslist) {
        super(new PascalDistribution(r, p), modslist);
    }
}
