package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
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
