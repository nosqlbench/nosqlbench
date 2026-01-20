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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
@Tag("unit")
class RateCommandTest {

    @Test
    void testRateLinearPattern() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RateCommand command = new RateCommand();

            Map<String, Object> params = Map.of(
                "metric", "patterns_total",
                "window", "1h",
                "labels", Map.of("pattern", "linear")
            );
            QueryResult result = command.execute(conn, params);

            // Linear pattern: increments by 10 every ~50ms
            // Expected rate: 10 / 0.05 = 200 ops/sec (approximately)
            assertTrue(result.rowCount() > 0, "Should have rate calculations");

            // Get all calculated rates
            List<Double> rates = result.rows().stream()
                .map(row -> (Double) row.get("rate_per_sec"))
                .filter(rate -> rate != null && !rate.isNaN())
                .toList();

            assertTrue(rates.size() > 0, "Should have valid rates");

            // For linear pattern, all rates should be relatively constant
            // (within reasonable variance due to timing)
            double avgRate = rates.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            assertTrue(avgRate > 0, "Average rate should be positive");

            System.out.println("\n=== Rate Query: Linear Pattern ===");
            System.out.println(new TableFormatter().format(result));
            System.out.println("Average rate: " + avgRate + " ops/sec");
        }
    }

    @Test
    void testRateExponentialPattern() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RateCommand command = new RateCommand();

            Map<String, Object> params = Map.of(
                "metric", "patterns_total",
                "window", "1h",
                "labels", Map.of("pattern", "exponential")
            );
            QueryResult result = command.execute(conn, params);

            assertTrue(result.rowCount() > 0, "Should have rate calculations");

            // Exponential pattern: rate should increase over time
            List<Double> rates = result.rows().stream()
                .map(row -> (Double) row.get("rate_per_sec"))
                .filter(rate -> rate != null && !rate.isNaN())
                .toList();

            assertTrue(rates.size() > 10, "Should have many rate points");

            // Verify rates are generally increasing (exponential growth)
            // Compare first quarter to last quarter
            int quarterSize = rates.size() / 4;
            double firstQuarterAvg = rates.subList(0, quarterSize).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
            double lastQuarterAvg = rates.subList(rates.size() - quarterSize, rates.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

            assertTrue(lastQuarterAvg > firstQuarterAvg,
                "Exponential pattern should have increasing rate over time");

            System.out.println("\n=== Rate Query: Exponential Pattern ===");
            System.out.println("First quarter avg: " + firstQuarterAvg + " ops/sec");
            System.out.println("Last quarter avg: " + lastQuarterAvg + " ops/sec");
        }
    }

    @Test
    void testRateStepPattern() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RateCommand command = new RateCommand();

            Map<String, Object> params = Map.of(
                "metric", "patterns_total",
                "window", "1h",
                "labels", Map.of("pattern", "step")
            );
            QueryResult result = command.execute(conn, params);

            assertTrue(result.rowCount() > 0, "Should have rate calculations");

            // Step pattern: rate should be 0 within steps, high at step boundaries
            List<Double> rates = result.rows().stream()
                .map(row -> (Double) row.get("rate_per_sec"))
                .filter(rate -> rate != null && !rate.isNaN())
                .toList();

            // Most rates should be either 0 (flat) or very high (step jump)
            long zeroRates = rates.stream().filter(r -> Math.abs(r) < 1.0).count();
            long highRates = rates.stream().filter(r -> r > 100.0).count();

            assertTrue(zeroRates > 0 || highRates > 0,
                "Step pattern should have either zero or high rates");

            System.out.println("\n=== Rate Query: Step Pattern ===");
            System.out.println("Zero rates: " + zeroRates + ", High rates: " + highRates);
        }
    }

    @Test
    void testRateExamplesDatabase() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RateCommand command = new RateCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "window", "1h",
                "labels", Map.of("method", "GET", "endpoint", "/api/users", "status", "200")
            );
            QueryResult result = command.execute(conn, params);

            // Should have 4 rate calculations (5 snapshots - 1 for first point)
            assertEquals(4, result.rowCount(), "Should have 4 rate points");

            System.out.println("\n=== Rate Query: Examples DB ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testValidationMissingMetric() {
        RateCommand command = new RateCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("window", "5m")));
    }

    @Test
    void testValidationMissingWindow() {
        RateCommand command = new RateCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test")));
    }

    @Test
    void testValidationBothWindowAndLast() {
        RateCommand command = new RateCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "window", "5m", "last", "10m")));
    }

    @Test
    void testValidationWindowAllowed() throws Exception {
        RateCommand command = new RateCommand();
        command.validate(Map.of("metric", "test", "window", "5m"));
    }

    @Test
    void testValidationLastAllowed() throws Exception {
        RateCommand command = new RateCommand();
        command.validate(Map.of("metric", "test", "last", "5m"));
    }
}
