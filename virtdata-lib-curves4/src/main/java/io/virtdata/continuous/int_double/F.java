package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.FDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/F-distribution">Wikipedia: F-distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/FDistribution.html">Commons JavaDoc: FDistribution</a>
 *
 * @see <a href="http://mathworld.wolfram.com/F-Distribution.html">Mathworld: F-Distribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class F extends IntToDoubleContinuousCurve {
    public F(double numeratorDegreesOfFreedom, double denominatorDegreesOfFreedom, String... mods) {
        super(new FDistribution(numeratorDegreesOfFreedom, denominatorDegreesOfFreedom), mods);
    }
}
