package io.nosqlbench.virtdata.library.curves4.discrete.int_long;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GeometricDistribution;

@ThreadSafeMapper
public class Geometric extends IntToLongDiscreteCurve {
    public Geometric(double p, String... modslist) {
        super(new GeometricDistribution(p), modslist);
    }
}
