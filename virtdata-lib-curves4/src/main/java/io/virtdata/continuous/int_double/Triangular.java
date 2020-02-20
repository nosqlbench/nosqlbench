package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.TriangularDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Triangular_distribution">Wikipedia: Triangular distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/TriangularDistribution.html">Commons JavaDoc: TriangularDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Triangular extends IntToDoubleContinuousCurve {
    public Triangular(double a, double c, double b, String... mods) {
        super(new TriangularDistribution(a,c,b), mods);
    }
}
