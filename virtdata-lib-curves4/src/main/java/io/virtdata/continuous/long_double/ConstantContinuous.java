package io.virtdata.continuous.long_double;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ConstantContinuousDistribution;

@ThreadSafeMapper
public class ConstantContinuous extends LongToDoubleContinuousCurve {
    public ConstantContinuous(double value, String... mods) {
        super(new ConstantContinuousDistribution(value), mods);
    }
}
