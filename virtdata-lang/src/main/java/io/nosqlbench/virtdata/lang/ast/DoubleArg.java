package io.nosqlbench.virtdata.lang.ast;

public class DoubleArg implements ArgType {

    private final double doubleValue;

    public DoubleArg(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    @Override
    public String toString() {
        return String.valueOf(doubleValue);
    }
}
