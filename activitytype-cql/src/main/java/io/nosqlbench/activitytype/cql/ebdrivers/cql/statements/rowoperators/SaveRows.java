package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.rowoperators;

import com.datastax.driver.core.Row;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.api.RowCycleOperator;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.rsoperators.PerThreadCQLData;

import java.util.LinkedList;

public class SaveRows implements RowCycleOperator {

    @Override
    public int apply(Row row, long cycle) {
        LinkedList<Row>rows = PerThreadCQLData.rows.get();
        rows.add(row);
        return 0;
    }

}
