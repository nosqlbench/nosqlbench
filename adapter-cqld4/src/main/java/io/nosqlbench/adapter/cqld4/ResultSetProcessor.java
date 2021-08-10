package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.engine.api.activityimpl.uniform.ResultProcessor;

public interface ResultSetProcessor extends ResultProcessor<ResultSet, Row> {
}
