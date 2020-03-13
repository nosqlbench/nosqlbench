package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

public class MaxTriesExhaustedException extends CqlGenericCycleException {

    private int maxtries;

    public MaxTriesExhaustedException(long cycle, int maxtries) {
        super(cycle);
        this.maxtries = maxtries;
    }

    public int getMaxTries() {
        return maxtries;
    }

    @Override
    public String getMessage() {
        return "Exhausted max tries (" + getMaxTries() + ") on cycle " + getCycle() + ".";
    }
}
