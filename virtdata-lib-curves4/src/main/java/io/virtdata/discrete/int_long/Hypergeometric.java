package io.virtdata.discrete.int_long;

import io.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.HypergeometricDistribution;

@ThreadSafeMapper
public class Hypergeometric extends IntToLongDiscreteCurve {
    public Hypergeometric(int populationSize, int numberOfSuccesses, int sampleSize, String... modslist) {
        super(new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize), modslist);
    }
}
