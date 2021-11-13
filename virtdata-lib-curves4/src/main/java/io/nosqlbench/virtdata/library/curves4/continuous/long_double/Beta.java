package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.BetaDistribution;

@Categories({Category.distributions})
@ThreadSafeMapper
public class Beta extends LongToDoubleContinuousCurve {
    public Beta(double alpha, double beta, String... mods) {
        super(new BetaDistribution(alpha, beta), mods);
    }
}
