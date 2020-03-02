package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.rsoperators;

import io.nosqlbench.activitytype.cql.ebdrivers.cql.api.ResultSetCycleOperator;

public enum ResultSetCycleOperators {

    pushvars(PushVars.class),
    popvars(PopVars.class),
    clearvars(ClearVars.class),

    trace(TraceLogger.class),
    log(CqlResultSetLogger.class),
    assert_singlerow(AssertSingleRowResultSet.class),

    print(Print.class);

    private final Class<? extends ResultSetCycleOperator> implClass;

    ResultSetCycleOperators(Class<? extends ResultSetCycleOperator> traceLoggerClass) {
        this.implClass = traceLoggerClass;
    }


    public Class<? extends ResultSetCycleOperator> getImplementation() {
        return implClass;
    }

    public ResultSetCycleOperator getInstance() {
        try {
            return getImplementation().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultSetCycleOperator newOperator(String name) {
        return ResultSetCycleOperators.valueOf(name).getInstance();
    }

}
