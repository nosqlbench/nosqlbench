package io.nosqlbench.activitytype.cql.ebdrivers.cql.statements.binders;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Statement;
import io.nosqlbench.virtdata.api.ValuesArrayBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This is now the main binder again, but if there are any exceptions, it delegates to the diagnostic
 * one in order to explain what happened. This is to allow for higher performance in the general
 * case, but with better user support when something goes wrong.
 *
 * If you want to force the client to use the array passing method of initializing a statement,
 * use this one, known as 'directarray'. This does give up the benefit of allowing unset values
 * to be modeled, and at no clear benefit. Thus the {@link CqlBinderTypes#unset_aware} one
 * will become the default.
 */
public class DirectArrayValuesBinder implements ValuesArrayBinder<PreparedStatement, Statement> {
    public final static Logger logger = LoggerFactory.getLogger(DirectArrayValuesBinder.class);

    @Override
    public Statement bindValues(PreparedStatement preparedStatement, Object[] objects) {
        try {
            return preparedStatement.bind(objects);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error binding objects to prepared statement directly, falling back to diagnostic binding layer:");
            sb.append(Arrays.toString(objects));
            logger.warn(sb.toString(),e);
            DiagnosticPreparedBinder diag = new DiagnosticPreparedBinder();
            return diag.bindValues(preparedStatement, objects);
        }
    }
}
