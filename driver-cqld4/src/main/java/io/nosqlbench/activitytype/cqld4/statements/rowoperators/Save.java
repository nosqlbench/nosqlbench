package io.nosqlbench.activitytype.cqld4.statements.rowoperators;

import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Save specific variables to the thread local object map
 */
public class Save implements RowCycleOperator {
    private final static Logger logger = LogManager.getLogger(Save.class);

    ThreadLocal<HashMap<String, Object>> tl_objectMap = SharedState.tl_ObjectMap;

    private final String[] varnames;

    public Save(String... varnames) {
        this.varnames = varnames;
    }

    @Override
    public int apply(Row row, long cycle) {
        try {
            HashMap<String, Object> tlvars = tl_objectMap.get();
            for (String varname : varnames) {
                Object object = row.getObject(varname);
                tlvars.put(varname, object);
            }
        } catch (Exception e) {
            Stream<ColumnDefinition> stream = StreamSupport.stream(row.getColumnDefinitions().spliterator(), false);
            logger.error("Unable to save '" + Arrays.toString(varnames) + "' from " + stream.map(d -> d.getName().toString())
                .collect(Collectors.joining(",", "[", "]")) + ": " + e.getMessage(), e);
            throw e;
        }
        return 0;
    }

}
