package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ParetoDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Pareto_distribution">Wikipedia: Pareto distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ParetoDistribution.html">Commons JavaDoc: ParetoDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Pareto extends IntToDoubleContinuousCurve {
    public Pareto(double scale, double shape, String... mods) {
        super(new ParetoDistribution(scale, shape), mods);
    }
}
