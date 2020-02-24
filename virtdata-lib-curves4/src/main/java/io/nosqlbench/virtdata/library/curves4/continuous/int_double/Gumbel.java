package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GumbelDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Gumbel_distribution">Wikipedia: Gumbel distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/GumbelDistribution.html">Commons JavaDoc: GumbelDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Gumbel extends IntToDoubleContinuousCurve {
    public Gumbel(double mu, double beta, String... mods) {
        super(new GumbelDistribution(mu, beta), mods);
    }
}
