package io.virtdata.discrete.int_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.HypergeometricDistribution;

/**
 * @see <a href="http://en.wikipedia.org/wiki/Hypergeometric_distribution">Wikipedia: Hypergeometric distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/HypergeometricDistribution.html">Commons JavaDoc: HypergeometricDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Hypergeometric extends IntToIntDiscreteCurve {
    public Hypergeometric(int populationSize, int numberOfSuccesses, int sampleSize, String... modslist) {
        super(new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize), modslist);
    }
}
