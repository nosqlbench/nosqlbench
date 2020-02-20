package io.virtdata.ast;

public class Expression {

    private Assignment assignment;
    private FunctionCall call;

    public Expression() {}

    public Expression(Assignment assignment, FunctionCall call) {
        this.assignment = assignment;
        this.call = call;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public FunctionCall getCall() {
        return call;
    }

    public void setCall(FunctionCall call) {
        this.call = call;
    }
}
