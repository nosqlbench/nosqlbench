package io.nosqlbench.activitytype.cqld4.core;

import com.datastax.oss.driver.api.core.cql.Statement;

/**
 * Provides a modular way for any CQL activities to modify statements before execution.
 * Each active modifier returns a statement in turn.
 */
public interface StatementModifier {
    Statement<?> modify(Statement<?> unmodified, long cycleNum);
}
