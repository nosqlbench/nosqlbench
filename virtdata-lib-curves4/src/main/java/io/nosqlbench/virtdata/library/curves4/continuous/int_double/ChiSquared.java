package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
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
