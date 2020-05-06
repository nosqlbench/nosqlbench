package io.nosqlbench.activitytype.cqld4.api;

import com.datastax.oss.driver.api.core.cql.Row;

/**
 * An operator interface for consuming ResultSets and producing some
 * int that can be used as a status code in activities.
 */
public interface RowCycleOperator {
    int apply(Row row, long cycle);
}
