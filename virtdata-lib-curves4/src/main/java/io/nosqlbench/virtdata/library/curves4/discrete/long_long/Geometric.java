package io.nosqlbench.virtdata.library.curves4.discrete.long_long;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GeometricDistribution;

@ThreadSafeMapper
public class Geometric extends LongToLongDiscreteCurve {
    public Geometric(double p, String... modslist) {
        super(new GeometricDistribution(p), modslist);
    }
}
