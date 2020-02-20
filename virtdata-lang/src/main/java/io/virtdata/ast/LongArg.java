package io.virtdata.ast;

public class LongArg implements ArgType {
    private final long longValue;

    public LongArg(Long longValue) {
        this.longValue = longValue;
    }

    public long getLongValue() {
        return longValue;
    }

    @Override
    public String toString() {
        return String.valueOf(longValue);
    }
}
