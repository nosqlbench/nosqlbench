package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;

public class Print implements D4ResultSetCycleOperator {

    @Override
    public int apply(AsyncResultSet resultSet, Statement<?> statement, long cycle) {
        System.out.println("RS:"+ resultSet.toString());
        return 0;
    }
}
