package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LaplaceDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Laplace_distribution">Wikipedia: Laplace distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LaplaceDistribution.html">Commons JavaDoc: LaplaceDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Laplace extends IntToDoubleContinuousCurve {
    public Laplace(double mu, double beta, String... mods) {
        super(new LaplaceDistribution(mu, beta), mods);
    }
}
