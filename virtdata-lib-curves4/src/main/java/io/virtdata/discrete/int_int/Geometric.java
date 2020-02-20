package io.virtdata.discrete.int_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.GeometricDistribution;

/**
 * @see <a href="http://en.wikipedia.org/wiki/Geometric_distribution">Wikipedia: Geometric distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/GeometricDistribution.html">Commons JavaDoc: GeometricDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Geometric extends IntToIntDiscreteCurve {
    public Geometric(double p, String... modslist) {
        super(new GeometricDistribution(p), modslist);
    }
}
