package io.nosqlbench.activitytype.cqld4.api;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;

/**
 * An operator interface for performing a modular action on CQL ResultSets per-cycle.
 */
public interface D4ResultSetCycleOperator {
    /**
     * Perform an action on a result set for a specific cycle.
     * @param pageInfo The ResultSet for the given cycle
     * @param statement The statement for the given cycle
     * @param cycle The cycle for which the statement was submitted
     * @return A value, only meaningful when used with aggregated operators
     */
    int apply(AsyncResultSet pageInfo, Statement<?> statement, long cycle);
}
