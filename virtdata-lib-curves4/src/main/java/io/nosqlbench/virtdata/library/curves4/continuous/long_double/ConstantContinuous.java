package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ConstantContinuousDistribution;

@ThreadSafeMapper
public class ConstantContinuous extends LongToDoubleContinuousCurve {
    public ConstantContinuous(double value, String... mods) {
        super(new ConstantContinuousDistribution(value), mods);
    }
}
