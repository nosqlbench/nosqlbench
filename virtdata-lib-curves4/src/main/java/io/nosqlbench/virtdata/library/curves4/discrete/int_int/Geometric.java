package io.nosqlbench.virtdata.library.curves4.discrete.int_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
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
