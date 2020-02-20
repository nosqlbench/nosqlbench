package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LogNormalDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Log-normal_distribution">Wikipedia: Log-normal distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LogNormalDistribution.html">Commons JavaDoc: LogNormalDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class LogNormal extends IntToDoubleContinuousCurve {
    public LogNormal(double scale, double shape, String... mods) {
        super(new LogNormalDistribution(scale, shape), mods);
    }
}
