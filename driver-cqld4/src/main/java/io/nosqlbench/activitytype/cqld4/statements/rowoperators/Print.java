package io.nosqlbench.activitytype.cqld4.statements.rowoperators;

import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;

/**
 * Save specific variables to the thread local object map
 */
public class Print implements RowCycleOperator {

    @Override
    public int apply(Row row, long cycle) {
        System.out.println("ROW:" + row);
        return 0;
    }

}
