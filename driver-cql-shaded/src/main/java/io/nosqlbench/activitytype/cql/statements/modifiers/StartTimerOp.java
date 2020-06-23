package io.nosqlbench.activitytype.cql.statements.modifiers;

import com.datastax.driver.core.Statement;

public class StartTimerOp implements StatementModifier {

    @Override
    public Statement modify(Statement unmodified, long cycleNum) {

        return unmodified;
    }
}
