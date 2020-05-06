package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.api.ResultSetCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;

public class PushVars implements ResultSetCycleOperator {

    @Override
    public int apply(ResultSet resultSet, Statement statement, long cycle) {
        HashMap<String, Object> existingVars = SharedState.tl_ObjectMap.get();
        HashMap<String, Object> topush = new HashMap<>(existingVars);

        SharedState.tl_ObjectStack.get().addLast(topush);
        return 0;
    }
}
