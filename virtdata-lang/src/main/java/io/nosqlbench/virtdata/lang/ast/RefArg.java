package io.nosqlbench.virtdata.lang.ast;

public class RefArg implements ArgType {

    private final String refName;

    public RefArg(String refName) {
        this.refName = refName;
    }

    @Override
    public String toString() {
        return "$"+refName;
    }

    public String getRefName() {
        return refName;
    }
}
