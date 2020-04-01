package io.nosqlbench.virtdata.library.curves4.continuous.long_double;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.TriangularDistribution;

@ThreadSafeMapper
public class Triangular extends LongToDoubleContinuousCurve {
    public Triangular(double a, double c, double b, String... mods) {
        super(new TriangularDistribution(a,c,b), mods);
    }
}
