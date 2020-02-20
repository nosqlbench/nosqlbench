package io.virtdata.ast;

public class FloatArg implements ArgType {

    private final float floatValue;

    public FloatArg(float floatValue) {
        this.floatValue = floatValue;
    }

    public double getFloatValue() {
        return floatValue;
    }

    @Override
    public String toString() {
        return String.valueOf(floatValue);
    }
}
