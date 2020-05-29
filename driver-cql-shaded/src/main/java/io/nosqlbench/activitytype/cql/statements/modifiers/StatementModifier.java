package io.nosqlbench.activitytype.cql.statements.modifiers;

import com.datastax.driver.core.Statement;

/**
 * Provides a modular way for any CQL activities to modify statements before execution.
 * Each active modifier returns a statement in turn.
 */
public interface StatementModifier {
    Statement modify(Statement unmodified, long cycleNum);
}
