package io.nosqlbench.activitytype.cql.statements.rowoperators;

import io.nosqlbench.activitytype.cql.api.RowCycleOperator;

public enum RowCycleOperators {

    saverows(SaveThreadRows.class),
    savevars(SaveThreadVars.class),
    saveglobalvars(SaveGlobalVars.class),
    print(Print.class);

    private final Class<? extends RowCycleOperator> implClass;

    RowCycleOperators(Class<? extends RowCycleOperator> traceLoggerClass) {
        this.implClass = traceLoggerClass;
    }

    public Class<? extends RowCycleOperator> getImplementation() {
        return implClass;
    }

    public RowCycleOperator getInstance() {
        try {
            return getImplementation().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RowCycleOperator newOperator(String name) {
        return RowCycleOperators.valueOf(name).getInstance();
    }

}
