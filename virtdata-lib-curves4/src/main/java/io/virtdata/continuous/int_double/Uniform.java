package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.UniformContinuousDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Uniform_distribution_(continuous)">Wikipedia: Uniform distribution (continuous)</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/UniformContinuousDistribution.html">Commons JavaDoc: UniformContinuousDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Uniform extends IntToDoubleContinuousCurve {
    public Uniform(double lower, double upper, String... mods) {
        super(new UniformContinuousDistribution(lower, upper), mods);
    }
}
