package io.nosqlbench.virtdata.lang.ast;

public class Assignment {

    private final String variableName;

    public Assignment(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
}
