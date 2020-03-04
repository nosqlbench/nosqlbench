package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.rowoperators;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.api.RowCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SaveGlobalVars implements RowCycleOperator {

    ConcurrentHashMap<String, Object> gl_vars = SharedState.gl_ObjectMap;

    @Override
    public int apply(Row row, long cycle) {
        List<ColumnDefinitions.Definition> cdlist = row.getColumnDefinitions().asList();
        for (ColumnDefinitions.Definition definition : cdlist) {
            String name = definition.getName();
            Object object = row.getObject(name);
            if (object == null){
                object = "";
            }
            gl_vars.put(name,object);
        }
        return 0;
    }

}
