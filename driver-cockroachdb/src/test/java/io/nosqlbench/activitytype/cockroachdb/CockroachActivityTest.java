package io.nosqlbench.activitytype.cockroachdb;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class CockroachActivityTest {
    @Test
    public void testErrorNameMapper() {
        ActivityDef activityDef = new ActivityDef(ParameterMap.parseParams("").orElseThrow());
        CockroachActivity activity = new CockroachActivity(activityDef);

        // When the Throwable is a SQLException, the error name should be getSQLState()
        Throwable sqlException = new SQLException("my test exception", "my-test-sql-state");
        assertEquals("SQLException_my-test-sql-state", activity.errorNameMapper(sqlException));

        // See PSQLState to string code mapping at the Github source code website
        // https://github.com/pgjdbc/pgjdbc/blob/master/pgjdbc/src/main/java/org/postgresql/util/PSQLState.java
        Throwable psqlException = new PSQLException("retry transaction", PSQLState.CONNECTION_FAILURE);
        assertEquals("PSQLException_08006", activity.errorNameMapper(psqlException));

        // When SQLState is null or empty, suffix shouldn't be underscore
        Throwable nullSQLState = new PSQLException("my test runtime exception", null);
        assertEquals("PSQLException", activity.errorNameMapper(nullSQLState));

        // When Throwable is not a SQLException, the error name should be the class name
        Throwable runtimeException = new SocketTimeoutException("my test runtime exception");
        assertEquals("SocketTimeoutException", activity.errorNameMapper(runtimeException));
    }
}
