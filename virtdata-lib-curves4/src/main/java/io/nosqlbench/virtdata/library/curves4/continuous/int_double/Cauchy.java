package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.CauchyDistribution;

/**
 * @see <a href="http://en.wikipedia.org/wiki/Cauchy_distribution">Wikipedia: Cauchy_distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/CauchyDistribution.html">Commons Javadoc: CauchyDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Cauchy extends IntToDoubleContinuousCurve {
    public Cauchy(double median, double scale, String... mods) {
        super(new CauchyDistribution(median, scale), mods);
    }
}
