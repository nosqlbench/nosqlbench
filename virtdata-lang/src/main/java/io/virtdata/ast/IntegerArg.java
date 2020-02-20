package io.virtdata.ast;

public class IntegerArg implements ArgType {
    private final int intValue;

    public IntegerArg(Integer integer) {
        this.intValue = integer;
    }

    public int getIntValue() {
        return intValue;
    }

    @Override
    public String toString() {
        return String.valueOf(intValue);
    }
}
