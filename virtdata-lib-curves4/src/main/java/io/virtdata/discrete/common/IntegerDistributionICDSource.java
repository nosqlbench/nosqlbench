package io.virtdata.discrete.common;

import org.apache.commons.statistics.distribution.DiscreteDistribution;

import java.util.function.DoubleToIntFunction;

public class IntegerDistributionICDSource implements DoubleToIntFunction {

    private DiscreteDistribution integerDistribution;

    public IntegerDistributionICDSource(DiscreteDistribution integerDistribution) {
        this.integerDistribution = integerDistribution;
    }

    @Override
    public int applyAsInt(double value) {
        return integerDistribution.inverseCumulativeProbability(value);
    }
}
