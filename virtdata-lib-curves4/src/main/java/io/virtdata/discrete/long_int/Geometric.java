package io.virtdata.discrete.long_int;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GeometricDistribution;

@ThreadSafeMapper
public class Geometric extends LongToIntDiscreteCurve {
    public Geometric(double p, String... modslist) {
        super(new GeometricDistribution(p), modslist);
    }
}
