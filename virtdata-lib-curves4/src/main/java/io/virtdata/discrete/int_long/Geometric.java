package io.virtdata.discrete.int_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GeometricDistribution;

@ThreadSafeMapper
public class Geometric extends IntToLongDiscreteCurve {
    public Geometric(double p, String... modslist) {
        super(new GeometricDistribution(p), modslist);
    }
}
