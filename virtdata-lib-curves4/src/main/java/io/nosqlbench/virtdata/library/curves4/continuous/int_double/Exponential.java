package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ExponentialDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Exponential_distribution">Wikipedia: Exponential distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ExponentialDistribution.html">Commons JavaDoc: ExponentialDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Exponential extends IntToDoubleContinuousCurve {
    public Exponential(double mean, String... mods) {
        super(new ExponentialDistribution(mean), mods);
    }
}
