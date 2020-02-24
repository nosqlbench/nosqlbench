package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.TDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Student's_t-distribution">Wikipedia: Student's t-distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/TDistribution.html">Commons JavaDoc: TDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class T extends IntToDoubleContinuousCurve {
    public T(double degreesOfFreedom, String... mods) {
        super(new TDistribution(degreesOfFreedom), mods);
    }
}
