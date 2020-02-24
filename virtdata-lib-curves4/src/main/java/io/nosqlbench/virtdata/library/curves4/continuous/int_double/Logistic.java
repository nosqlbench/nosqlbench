package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LogisticDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Logistic_distribution">Wikipedia: Logistic distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LogisticDistribution.html">Commons JavaDoc: LogisticDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Logistic extends IntToDoubleContinuousCurve {
    public Logistic(double mu, double scale, String... mods) {
        super(new LogisticDistribution(mu, scale), mods);
    }
}
