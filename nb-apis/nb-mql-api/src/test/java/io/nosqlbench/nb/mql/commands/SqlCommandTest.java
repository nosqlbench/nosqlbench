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
import io.nosqlbench.nb.mql.query.InvalidQueryException;
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
class SqlCommandTest {

    @Test
    void testSqlQueryMetricFamilies() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SqlCommand command = new SqlCommand();

            Map<String, Object> params = Map.of(
                "query", "SELECT name, type FROM metric_family ORDER BY name"
            );
            QueryResult result = command.execute(conn, params);

            // Should have at least 1 metric family
            assertTrue(result.rowCount() >= 1, "Should have metric families");

            // Verify columns
            assertEquals(2, result.columns().size());
            assertTrue(result.columns().contains("name"));
            assertTrue(result.columns().contains("type"));

            System.out.println("\n=== SQL Query: Metric Families ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSqlQuerySampleCounts() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SqlCommand command = new SqlCommand();

            Map<String, Object> params = Map.of(
                "query", "SELECT COUNT(*) as total_samples FROM sample_value"
            );
            QueryResult result = command.execute(conn, params);

            assertEquals(1, result.rowCount());
            assertEquals(1, result.columns().size());
            assertEquals("total_samples", result.columns().get(0));

            Map<String, Object> row = result.rows().get(0);
            long count = ((Number) row.get("total_samples")).longValue();
            assertEquals(20, count, "Should have 20 samples");

            System.out.println("\n=== SQL Query: Sample Count ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSqlQueryWithCTE() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SqlCommand command = new SqlCommand();

            String sql = """
                WITH latest AS (
                  SELECT MAX(timestamp_ms) as max_ts FROM sample_value
                )
                SELECT COUNT(*) as latest_count
                FROM sample_value sv, latest
                WHERE sv.timestamp_ms = latest.max_ts
                """;

            Map<String, Object> params = Map.of("query", sql);
            QueryResult result = command.execute(conn, params);

            assertEquals(1, result.rowCount());
            long count = ((Number) result.rows().get(0).get("latest_count")).longValue();
            assertEquals(2, count, "Should have 2 samples at latest timestamp");
        }
    }

    @Test
    void testSqlQueryPragma() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SqlCommand command = new SqlCommand();

            Map<String, Object> params = Map.of(
                "query", "PRAGMA table_info(metric_family)"
            );
            QueryResult result = command.execute(conn, params);

            // Should return schema information
            assertTrue(result.rowCount() > 0, "Should return table info");

            System.out.println("\n=== SQL Query: Table Schema ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testValidationMissingQuery() {
        SqlCommand command = new SqlCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of()));
    }

    @Test
    void testValidationEmptyQuery() {
        SqlCommand command = new SqlCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("query", "")));
    }

    @Test
    void testValidationNonSelectQuery() {
        SqlCommand command = new SqlCommand();

        // INSERT should be blocked
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("query", "INSERT INTO metric_family ...")));

        // UPDATE should be blocked
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("query", "UPDATE sample_value SET ...")));

        // DELETE should be blocked
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("query", "DELETE FROM sample_value")));

        // DROP should be blocked
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("query", "DROP TABLE metric_family")));
    }

    @Test
    void testValidationAllowedQueries() throws Exception {
        SqlCommand command = new SqlCommand();

        // SELECT should be allowed
        command.validate(Map.of("query", "SELECT * FROM metric_family"));

        // WITH (CTE) should be allowed
        command.validate(Map.of("query", "WITH cte AS (SELECT 1) SELECT * FROM cte"));

        // PRAGMA should be allowed
        command.validate(Map.of("query", "PRAGMA table_info(metric_family)"));
    }

    @Test
    void testSqlQueryExamples() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            SqlCommand command = new SqlCommand();

            // Example: Get all metrics with their latest values
            String sql = """
                SELECT
                  sn.sample,
                  sv.value,
                  datetime(sv.timestamp_ms/1000, 'unixepoch') as captured_at
                FROM metric_instance mi
                JOIN sample_name sn ON mi.sample_name_id = sn.id
                JOIN sample_value sv ON sv.metric_instance_id = mi.id
                WHERE sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
                ORDER BY sn.sample, sv.value DESC
                """;

            Map<String, Object> params = Map.of("query", sql);
            QueryResult result = command.execute(conn, params);

            assertTrue(result.rowCount() > 0, "Should return results");

            System.out.println("\n=== SQL Query: All Latest Values ===");
            System.out.println(new TableFormatter().format(result));
        }
    }
}
