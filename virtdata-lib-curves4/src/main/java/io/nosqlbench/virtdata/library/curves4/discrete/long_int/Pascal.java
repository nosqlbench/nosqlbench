package io.nosqlbench.virtdata.library.curves4.discrete.long_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PascalDistribution;

@ThreadSafeMapper
public class Pascal extends LongToIntDiscreteCurve {
    public Pascal(int r, double p, String... modslist) {
        super(new PascalDistribution(r, p), modslist);
    }
}
