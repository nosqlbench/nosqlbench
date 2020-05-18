package io.nosqlbench.activitytype.cql.statements.rsoperators;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import io.nosqlbench.activitytype.cql.api.ResultSetCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;

public class PopVars implements ResultSetCycleOperator {

    @Override
    public int apply(ResultSet resultSet, Statement statement, long cycle) {
        HashMap<String, Object> stringObjectHashMap = SharedState.tl_ObjectMap.get();
        Object o = SharedState.tl_ObjectStack.get().pollLast();
        if (o != null && o instanceof HashMap) {
            SharedState.tl_ObjectMap.set((HashMap) o);
            return 0;
        } else {
            throw new RuntimeException("Tried to pop thread local data from stack, but there was none.");
        }
    }
}
