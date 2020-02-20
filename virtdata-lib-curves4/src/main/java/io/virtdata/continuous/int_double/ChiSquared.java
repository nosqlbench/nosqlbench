package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ChiSquaredDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Chi-squared_distribution">Wikipedia: Chi-squared distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ChiSquaredDistribution.html">Commons JavaDoc: ChiSquaredDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class ChiSquared extends IntToDoubleContinuousCurve {
    public ChiSquared(double degreesOfFreedom, String... mods) {
        super(new ChiSquaredDistribution(degreesOfFreedom), mods);
    }
}
