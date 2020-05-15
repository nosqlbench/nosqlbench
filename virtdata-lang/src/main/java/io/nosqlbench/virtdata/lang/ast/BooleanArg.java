package io.nosqlbench.virtdata.lang.ast;

public class BooleanArg implements ArgType {

    private final boolean boolValue;

    public BooleanArg(boolean boolValue) {
        this.boolValue = boolValue;
    }

    public boolean getBooleanValue() {
        return boolValue;
    }

    @Override
    public String toString() {
        return String.valueOf(boolValue);
    }
}
