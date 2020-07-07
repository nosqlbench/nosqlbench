package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;

public enum ResultSetCycleOperators {

    pushvars(PushVars.class),
    popvars(PopVars.class),
    clearvars(ClearVars.class),

    trace(TraceLogger.class),
    log(CqlD4ResultSetLogger.class),
    assert_singlerow(AssertSingleRowD4ResultSet.class),

    print(Print.class);

    private final Class<? extends D4ResultSetCycleOperator> implClass;

    ResultSetCycleOperators(Class<? extends D4ResultSetCycleOperator> traceLoggerClass) {
        this.implClass = traceLoggerClass;
    }


    public Class<? extends D4ResultSetCycleOperator> getImplementation() {
        return implClass;
    }

    public D4ResultSetCycleOperator getInstance() {
        try {
            return getImplementation().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static D4ResultSetCycleOperator newOperator(String name) {
        return ResultSetCycleOperators.valueOf(name).getInstance();
    }

}
