package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

public class ClearVars implements D4ResultSetCycleOperator {

    @Override
    public int apply(AsyncResultSet pageInfo, Statement<?> statement, long cycle) {
        SharedState.tl_ObjectMap.get().clear();
        return 0;
    }
}
