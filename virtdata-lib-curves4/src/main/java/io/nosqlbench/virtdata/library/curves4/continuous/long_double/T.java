package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.TDistribution;

@ThreadSafeMapper
public class T extends LongToDoubleContinuousCurve {
    public T(double degreesOfFreedom, String... mods) {
        super(new TDistribution(degreesOfFreedom), mods);
    }
}
