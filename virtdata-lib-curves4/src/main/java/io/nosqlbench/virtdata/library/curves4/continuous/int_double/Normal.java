package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.NormalDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Normal_distribution">Wikipedia: Normal distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/NormalDistribution.html">Commons JavaDoc: NormalDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Normal extends IntToDoubleContinuousCurve {
    public Normal(double mean, double sd, String... mods) {
        super(new NormalDistribution(mean, sd), mods);
    }
}
