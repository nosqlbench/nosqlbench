package io.nosqlbench.activitytype.cql.api;

import com.datastax.driver.core.Statement;

public interface StatementFilter {
    boolean matches(Statement statement);
}
