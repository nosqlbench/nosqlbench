package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.rowoperators;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.api.RowCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.List;

public class SaveVars implements RowCycleOperator {

    ThreadLocal<HashMap<String, Object>> tl_objectMap = SharedState.tl_ObjectMap;

    @Override
    public int apply(Row row, long cycle) {
        HashMap<String, Object> tlvars= tl_objectMap.get();
        List<ColumnDefinitions.Definition> cdlist = row.getColumnDefinitions().asList();
        for (ColumnDefinitions.Definition definition : cdlist) {
            String name = definition.getName();
            Object object = row.getObject(name);
            tlvars.put(name,object);
        }
        return 0;
    }

}
