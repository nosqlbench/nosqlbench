package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;

import java.util.LinkedList;

public class RowCapture implements D4ResultSetCycleOperator {
    @Override
    public int apply(AsyncResultSet resultSet, Statement<?> statement, long cycle) {
        ThreadLocal<LinkedList<Row>> rows = PerThreadCQLData.rows;
        return 0;
    }
}
