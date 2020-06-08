package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;

public class PushVars implements D4ResultSetCycleOperator {

    @Override
    public int apply(AsyncResultSet resultSet, Statement<?> statement, long cycle) {
        HashMap<String, Object> existingVars = SharedState.tl_ObjectMap.get();
        HashMap<String, Object> topush = new HashMap<>(existingVars);

        SharedState.tl_ObjectStack.get().addLast(topush);
        return 0;
    }
}
