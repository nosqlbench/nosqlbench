package io.nosqlbench.activitytype.cql.statements.binders;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;

/**
 * This binder is not meant to be used with anything but DDL or statements
 * which should not be trying to parameterize values in general.
 * Parametrized values are still possible through parametrized constructor parameter.
 * This binder should be avoided in favor of binders returning PreparedStatement
 */
public class SimpleStatementValuesBinder
    implements ValuesArrayBinder<SimpleStatement, Statement> {

    private final boolean parametrized;

    public SimpleStatementValuesBinder(boolean parametrized){
        this.parametrized = parametrized;
    }

    @Override
    public Statement bindValues(SimpleStatement context, Object[] values) {
        String query = context.getQueryString();
        if(parametrized) {
            String[] splits = query.split("\\?");
            assert splits.length == values.length+1;
            StringBuilder sb = new StringBuilder();
            sb.append(splits[0]);
            for(int i = 1; i < splits.length; i++) {
                sb.append(values[i - 1]);
                sb.append(splits[i]);
            }
            query = sb.toString();
            System.out.println(query);

        }
        SimpleStatement simpleStatement = new SimpleStatement(query);
        ConsistencyLevel cl = context.getConsistencyLevel();
        if(cl != null){
            simpleStatement.setConsistencyLevel(context.getConsistencyLevel());
        }
        //Does it really makes senses?
        ConsistencyLevel serial_cl = context.getSerialConsistencyLevel();
        if(serial_cl != null){
            simpleStatement.setSerialConsistencyLevel(context.getSerialConsistencyLevel());
        }
        Boolean idempotent = context.isIdempotent();
        if(idempotent != null){
            simpleStatement.setIdempotent(idempotent);
        }
        return simpleStatement;
    }
}
