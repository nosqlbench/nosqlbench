package io.virtdata.discrete.int_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.UniformDiscreteDistribution;

@ThreadSafeMapper
public class Uniform extends IntToLongDiscreteCurve {
    public Uniform(int lower, int upper, String... modslist) {
        super(new UniformDiscreteDistribution(lower, upper), modslist);
    }
}
