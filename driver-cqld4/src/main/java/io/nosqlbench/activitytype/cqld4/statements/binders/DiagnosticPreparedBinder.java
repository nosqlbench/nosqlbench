package io.nosqlbench.activitytype.cqld4.statements.binders;

import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.type.DataType;
import io.nosqlbench.activitytype.cqld4.core.CQLBindHelper;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This binder is not meant to be used primarily by default. It gives detailed
 * diagnostics, but in order to do so by default it does lots of processing.
 * Other binders will call to this one in an exception handler when needed in
 * order to explain in more detail what is happening for users.
 */
public class DiagnosticPreparedBinder implements ValuesArrayBinder<PreparedStatement, Statement<?>> {
    public static final Logger logger = LoggerFactory.getLogger(DiagnosticPreparedBinder.class);
    @Override
    public Statement<?> bindValues(PreparedStatement prepared, Object[] values) {
        ColumnDefinitions columnDefinitions = prepared.getVariableDefinitions();
        BoundStatement bound = prepared.bind();

        List<ColumnDefinition> columnDefList = new ArrayList<>();
        prepared.getVariableDefinitions().forEach(columnDefList::add);

        if (columnDefList.size() == values.length) {
            columnDefList = columnDefinitions.asList();
        } else {
            throw new RuntimeException("The number of named anchors in your statement does not match the number of bindings provided.");
        }

        int i = 0;
        for (Object value : values) {
            if (columnDefList.size() <= i) {
                logger.error("what gives?");
            }
            ColumnDefinition columnDef = columnDefList.get(i);
            String colName = columnDef.getName().toString();
            DataType type =columnDef.getType();
            try {
                bound = CQLBindHelper.bindStatement(bound, colName, value, type);
            } catch (ClassCastException e) {
                logger.error(String.format("Unable to bind column %s to cql type %s with value %s", colName, type, value));
                throw e;
            }
            i++;
        }
        return bound;
    }
}
