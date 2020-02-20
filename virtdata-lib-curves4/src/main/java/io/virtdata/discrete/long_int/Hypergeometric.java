package io.virtdata.discrete.long_int;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.HypergeometricDistribution;

@ThreadSafeMapper
public class Hypergeometric extends LongToIntDiscreteCurve {
    public Hypergeometric(int populationSize, int numberOfSuccesses, int sampleSize, String... modslist) {
        super(new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize), modslist);
    }
}
