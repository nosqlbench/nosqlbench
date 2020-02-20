package io.virtdata.discrete.int_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.UniformDiscreteDistribution;

@ThreadSafeMapper
@Categories({Category.distributions})
public class Uniform extends IntToIntDiscreteCurve {
    public Uniform(int lower, int upper, String... modslist) {
        super(new UniformDiscreteDistribution(lower, upper), modslist);
    }
}
