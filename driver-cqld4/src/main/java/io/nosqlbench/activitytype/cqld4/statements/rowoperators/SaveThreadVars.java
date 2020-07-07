package io.nosqlbench.activitytype.cqld4.statements.rowoperators;

import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.List;

/**
 * Saves all the values in this row to the thread-local object map,
 * with the field names as keys.
 */
public class SaveThreadVars implements RowCycleOperator {

    ThreadLocal<HashMap<String, Object>> tl_objectMap = SharedState.tl_ObjectMap;

    @Override
    public int apply(Row row, long cycle) {
        HashMap<String, Object> tlvars= tl_objectMap.get();
        for (ColumnDefinition cd : row.getColumnDefinitions()) {
            String name = cd.getName().toString();
            Object object = row.getObject(name);
            tlvars.put(name,object);
        }
        return 0;
    }

}
