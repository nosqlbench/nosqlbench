package io.nosqlbench.activitytype.cql.statements.rowoperators;

import com.datastax.driver.core.Row;
import io.nosqlbench.activitytype.cql.api.RowCycleOperator;

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
