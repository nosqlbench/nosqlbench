package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.WeibullDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Weibull_distribution">Wikipedia: Weibull distribution</a>
 *
 * @see <a href="http://mathworld.wolfram.com/WeibullDistribution.html">Wolfram Mathworld: Weibull Distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/WeibullDistribution.html">Commons Javadoc: WeibullDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Weibull extends IntToDoubleContinuousCurve {
    public Weibull(double alpha, double beta, String... mods) {
        super(new WeibullDistribution(alpha, beta), mods);
    }
}
