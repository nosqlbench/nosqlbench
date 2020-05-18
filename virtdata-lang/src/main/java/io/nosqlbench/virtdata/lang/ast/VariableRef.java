package io.nosqlbench.virtdata.lang.ast;

public class VariableRef {

    private final String refName;

    public VariableRef(String refName) {
        this.refName = refName;
    }

    public String getRefName() {
        return this.refName;
    }
}
