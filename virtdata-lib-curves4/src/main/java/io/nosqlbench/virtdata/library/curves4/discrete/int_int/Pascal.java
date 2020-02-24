package io.nosqlbench.virtdata.library.curves4.discrete.int_int;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.PascalDistribution;

/**
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/PascalDistribution.html">Commons JavaDoc: PascalDistribution</a>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Negative_binomial_distribution">Wikipedia: Negative binomial distribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Pascal extends IntToIntDiscreteCurve {
    public Pascal(int r, double p, String... modslist) {
        super(new PascalDistribution(r, p), modslist);
    }
}
