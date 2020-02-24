package io.nosqlbench.virtdata.library.curves4.continuous.common;

import org.apache.commons.statistics.distribution.ContinuousDistribution;

import java.util.function.DoubleUnaryOperator;

public class RealDistributionICDSource implements DoubleUnaryOperator {

    private ContinuousDistribution realDistribution;

    public RealDistributionICDSource(ContinuousDistribution realDistribution) {
        this.realDistribution = realDistribution;
    }

    @Override
    public double applyAsDouble(double operand) {
        return realDistribution.inverseCumulativeProbability(operand);
    }
}
