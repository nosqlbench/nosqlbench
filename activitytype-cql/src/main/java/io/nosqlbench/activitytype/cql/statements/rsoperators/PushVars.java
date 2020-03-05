package io.nosqlbench.activitytype.cql.statements.rsoperators;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import io.nosqlbench.activitytype.cql.api.ResultSetCycleOperator;
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
