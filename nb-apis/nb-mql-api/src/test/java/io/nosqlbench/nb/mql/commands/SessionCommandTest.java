/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.commands;

import io.nosqlbench.nb.mql.format.TableFormatter;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SessionCommandTest {

    @Test
    void testSessionInfo() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SessionCommand command = new SessionCommand();

            QueryResult result = command.execute(conn, Map.of());

            // Should return 1 row with session info
            assertEquals(1, result.rowCount());

            // Verify columns
            assertEquals(6, result.columns().size());
            assertTrue(result.columns().contains("first_snapshot"));
            assertTrue(result.columns().contains("last_snapshot"));
            assertTrue(result.columns().contains("duration"));
            assertTrue(result.columns().contains("total_snapshots"));
            assertTrue(result.columns().contains("total_samples"));
            assertTrue(result.columns().contains("avg_interval"));

            Map<String, Object> row = result.rows().get(0);

            // Verify we have 5 snapshots
            int snapshots = (int) row.get("total_snapshots");
            assertEquals(5, snapshots, "Should have 5 snapshots");

            // Should have 135 total samples (27 metrics × 5 snapshots)
            int samples = (int) row.get("total_samples");
            assertEquals(135, samples, "Should have 135 samples");

            System.out.println("\n=== Session Command ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSessionInfoSimpleCounter() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SessionCommand command = new SessionCommand();

            QueryResult result = command.execute(conn, Map.of());

            assertEquals(1, result.rowCount());

            Map<String, Object> row = result.rows().get(0);
            int snapshots = (int) row.get("total_snapshots");
            int samples = (int) row.get("total_samples");

            assertEquals(10, snapshots, "Should have 10 snapshots");
            assertEquals(20, samples, "Should have 20 samples");

            System.out.println("\n=== Session Info: Simple Counter DB ===");
            System.out.println(new TableFormatter().format(result));
        }
    }
}
