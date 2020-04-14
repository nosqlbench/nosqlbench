package io.nosqlbench.activitytype.cql.statements.binders;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;

import java.util.Optional;

/**
 * This binder is only meant to be used to directly generate final SimpleStatement query
 * without use of preparedStatement. This should be avoided any time possible since it's far more
 * less optimized than use of PreparedStatement. Only Use it when PreparedStatement is not possible
 */
public class RawValueBinder
        implements ValuesArrayBinder<String[], Statement> {

    private Optional<ConsistencyLevel> cl;
    private Optional<ConsistencyLevel> serial_cl;
    private Optional<Boolean> idempotent;

    public RawValueBinder(Optional<ConsistencyLevel> cl, Optional<ConsistencyLevel> serial_cl, Optional<Boolean> idempotent) {
        this.cl = cl;
        this.serial_cl = serial_cl;
        this.idempotent = idempotent;
    }

    @Override
    public Statement bindValues(String[] context, Object[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append(context[0]);
        for(int i=1; i<context.length; i++){
            sb.append(values[i-1]);
            sb.append(context[i]);
        }
        SimpleStatement simpleStatement = new SimpleStatement(sb.toString());
        cl.ifPresent((conlvl) -> {
            simpleStatement.setConsistencyLevel(conlvl);
        });
        serial_cl.ifPresent((scl) -> {
            simpleStatement.setSerialConsistencyLevel(scl);
        });
        idempotent.ifPresent((i) -> {
            simpleStatement.setIdempotent(i);
        });
        return simpleStatement;
    }
}
