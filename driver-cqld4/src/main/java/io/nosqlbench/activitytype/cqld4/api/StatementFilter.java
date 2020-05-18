package io.nosqlbench.activitytype.cqld4.api;

import com.datastax.oss.driver.api.core.cql.Statement;

public interface StatementFilter {
    boolean matches(Statement<?> statement);
}
