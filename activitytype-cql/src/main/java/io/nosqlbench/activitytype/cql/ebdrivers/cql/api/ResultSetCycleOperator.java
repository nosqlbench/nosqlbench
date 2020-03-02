package io.nosqlbench.activitytype.cql.ebdrivers.cql.api;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;

/**
 * An operator interface for performing a modular action on CQL ResultSets per-cycle.
 */
public interface ResultSetCycleOperator {
    /**
     * Perform an action on a result set for a specific cycle.
     * @param resultSet The ResultSet for the given cycle
     * @param statement The statement for the given cycle
     * @param cycle The cycle for which the statement was submitted
     * @return A value, only meaningful when used with aggregated operators
     */
    int apply(ResultSet resultSet, Statement statement, long cycle);
}
