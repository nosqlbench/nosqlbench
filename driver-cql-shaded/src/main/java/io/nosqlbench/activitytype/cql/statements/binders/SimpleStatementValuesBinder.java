package io.nosqlbench.activitytype.cql.statements.binders;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This binder is not meant to be used with anything but DDL or statements
 * which should not be trying to parameterize values in general.
 * Parametrized values are still possible through parameterized constructor parameter.
 * This binder should be avoided in favor of binders returning PreparedStatement
 */
public class SimpleStatementValuesBinder
    implements ValuesArrayBinder<SimpleStatement, Statement> {

    private final static Logger logger = LogManager.getLogger(SimpleStatementValuesBinder.class);
    private final boolean parameterized;

    public SimpleStatementValuesBinder(boolean parameterized) {
        this.parameterized = parameterized;
    }

    private final static Pattern anchorPattern = Pattern.compile("(?<name>\\?([a-zA-Z_][-a-zA-Z_.-])*|\\{[a-zA-Z_][-a-zA-Z_.-]*})");

    @Override
    public Statement bindValues(SimpleStatement context, Object[] values) {

        String query = context.getQueryString();

        SimpleStatement simpleStatement = null;
        if (parameterized) {
            Matcher matcher = anchorPattern.matcher(query);
            StringBuilder sb = new StringBuilder();
            List<String> anchors = new ArrayList<>();
            while (matcher.find()) {
                String name = matcher.group("name");
                anchors.add(name == null ? "_unnamed_" : name);
                matcher.appendReplacement(sb, "?");
            }
            matcher.appendTail(sb);
            assert (anchors.size() == values.length);
            query = sb.toString();
            simpleStatement = new SimpleStatement(query, values);
        } else {
            simpleStatement = new SimpleStatement(query);
        }

        ConsistencyLevel cl = context.getConsistencyLevel();
        if (cl != null) {
            simpleStatement.setConsistencyLevel(context.getConsistencyLevel());
        }
        //Does it really makes senses?
        ConsistencyLevel serial_cl = context.getSerialConsistencyLevel();
        if (serial_cl != null) {
            simpleStatement.setSerialConsistencyLevel(context.getSerialConsistencyLevel());
        }
        Boolean idempotent = context.isIdempotent();
        if (idempotent != null) {
            simpleStatement.setIdempotent(idempotent);
        }
        return simpleStatement;
    }
}
