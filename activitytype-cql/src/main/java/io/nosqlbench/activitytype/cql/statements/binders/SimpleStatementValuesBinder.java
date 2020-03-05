package io.nosqlbench.activitytype.cql.statements.binders;

import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import io.nosqlbench.virtdata.api.ValuesArrayBinder;

/**
 * This binder is not meant to be used with anything but DDL or statements
 * which should not be trying to parameterize values in general. If this changes,
 * support will be added for parameterized values here.
 */
public class SimpleStatementValuesBinder
        implements ValuesArrayBinder<SimpleStatement, Statement> {

    @Override
    public Statement bindValues(SimpleStatement context, Object[] values) {
        return new SimpleStatement(context.getQueryString(), values);
    }
}
