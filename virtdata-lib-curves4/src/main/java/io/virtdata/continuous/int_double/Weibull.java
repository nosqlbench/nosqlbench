package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
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
