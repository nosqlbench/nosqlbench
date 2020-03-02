package io.nosqlbench.activitytype.cql.ebdrivers.cql.api;

import com.datastax.driver.core.Statement;

public interface StatementFilter {
    boolean matches(Statement statement);
}
