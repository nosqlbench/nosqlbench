package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.rowoperators;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.api.RowCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Save specific variables to the thread local object map
 */
public class Save implements RowCycleOperator {
    private final static Logger logger = LoggerFactory.getLogger(Save.class);

    ThreadLocal<HashMap<String, Object>> tl_objectMap = SharedState.tl_ObjectMap;

    private String[] varnames;

    public Save(String... varnames) {
        this.varnames = varnames;
    }

    @Override
    public int apply(Row row, long cycle) {
        try {
            HashMap<String, Object> tlvars= tl_objectMap.get();
            for (String varname : varnames) {
                Object object = row.getObject(varname);
                tlvars.put(varname,object);
            }
        } catch (Exception e) {
            List<ColumnDefinitions.Definition> definitions = row.getColumnDefinitions().asList();
            logger.error("Unable to save '" + Arrays.toString(varnames) + "' from " +
                    definitions.stream().map(ColumnDefinitions.Definition::getName)
            .collect(Collectors.joining(",","[","]")) + ": " + e.getMessage(),e);
            throw e;
        }
        return 0;
    }

}
