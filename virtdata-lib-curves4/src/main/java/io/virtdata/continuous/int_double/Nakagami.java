package io.virtdata.continuous.int_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.NakagamiDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Nakagami_distribution">Wikipedia: Nakagami distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/NakagamiDistribution.html">Commons JavaDoc: NakagamiDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Nakagami extends IntToDoubleContinuousCurve {
    public Nakagami(double mu, double omega, String... mods) {
        super(new NakagamiDistribution(mu, omega), mods);
    }
}
