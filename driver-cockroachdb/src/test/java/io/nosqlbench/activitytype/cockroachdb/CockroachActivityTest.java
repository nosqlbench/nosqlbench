/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.activitytype.cockroachdb;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.net.SocketTimeoutException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
