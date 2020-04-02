package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.LevyDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/L%C3%A9vy_distribution">Wikipedia: Lévy distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LevyDistribution.html">Commons JavaDoc: LevyDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Levy extends IntToDoubleContinuousCurve { public Levy(double mu, double c, String... mods) { super(new LevyDistribution(mu,c), mods);
    }
}
