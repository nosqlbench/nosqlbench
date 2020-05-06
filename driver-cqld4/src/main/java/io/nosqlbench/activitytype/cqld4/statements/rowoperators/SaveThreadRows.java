package io.nosqlbench.activitytype.cqld4.statements.rowoperators;

import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.activitytype.cqld4.statements.rsoperators.PerThreadCQLData;

import java.util.LinkedList;

/**
 * Adds the current row to the per-thread row cache.
 */
public class SaveThreadRows implements RowCycleOperator {

    @Override
    public int apply(Row row, long cycle) {
        LinkedList<Row>rows = PerThreadCQLData.rows.get();
        rows.add(row);
        return 0;
    }

}
