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
import org.junit.jupiter.api.Tag;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
@Tag("unit")
class SummaryCommandTest {

    @Test
    void testSummaryExamplesDb() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SummaryCommand command = new SummaryCommand();

            QueryResult result = command.execute(conn, Map.of());

            // Should have multiple sections
            assertTrue(result.rowCount() > 0, "Should have summary rows");

            // Verify columns
            assertEquals(4, result.columns().size());
            assertTrue(result.columns().contains("section"));
            assertTrue(result.columns().contains("metric"));
            assertTrue(result.columns().contains("value"));
            assertTrue(result.columns().contains("details"));

            // Should have SESSION section
            boolean hasSession = result.rows().stream()
                .anyMatch(row -> "SESSION".equals(row.get("section")));
            assertTrue(hasSession, "Should have SESSION section");

            System.out.println("\n=== Summary: Examples DB ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSummarySimpleCounter() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SummaryCommand command = new SummaryCommand();

            QueryResult result = command.execute(conn, Map.of());

            assertTrue(result.rowCount() > 0, "Should have summary rows");

            System.out.println("\n=== Summary: Simple Counter DB ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSummaryMultiDimensional() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SummaryCommand command = new SummaryCommand();

            QueryResult result = command.execute(conn, Map.of());

            assertTrue(result.rowCount() > 0, "Should have summary rows");

            System.out.println("\n=== Summary: Multi-Dimensional DB ===");
            System.out.println(new TableFormatter().format(result));
        }
    }
}
