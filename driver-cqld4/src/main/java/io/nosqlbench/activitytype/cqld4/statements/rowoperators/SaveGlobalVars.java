package io.nosqlbench.activitytype.cqld4.statements.rowoperators;

import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the current row into the global object map. Key names are set from the field names. Null values are stored
 * as empty strings.
 */
public class SaveGlobalVars implements RowCycleOperator {

    ConcurrentHashMap<String, Object> gl_vars = SharedState.gl_ObjectMap;

    @Override
    public int apply(Row row, long cycle) {
        for (ColumnDefinition definition : row.getColumnDefinitions()) {
            String name = definition.getName().toString();
            Object object = row.getObject(name);
            if (object == null){
                object = "";
            }
            gl_vars.put(name,object);
        }
        return 0;
    }

}
