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

package io.nosqlbench.nb.mql.integration;

import io.nosqlbench.nb.mql.commands.InstantCommand;
import io.nosqlbench.nb.mql.commands.RangeCommand;
import io.nosqlbench.nb.mql.format.TableFormatter;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that the examples in EXAMPLES.md produce the expected output.
 * This test parses the markdown file, executes the commands, and verifies
 * the results match the documented expectations.
 */
class ExampleDocumentationTest {

    private static Path examplesDb;
    private static List<Example> examples;

    @BeforeAll
    static void loadExamples() throws IOException {
        // Load the examples database
        examplesDb = TestDatabaseLoader.getDatabase("examples.db");

        // Parse all example markdown files from classpath
        examples = new ArrayList<>();

        String[] exampleFiles = {
            "mql-instant-queries.md",
            "mql-range-queries.md"
            // Add more as they are created:
            // "mql-rate-queries.md",
            // "mql-aggregation-queries.md",
            // etc.
        };

        for (String exampleFile : exampleFiles) {
            String content = new String(
                ExampleDocumentationTest.class.getClassLoader()
                    .getResourceAsStream(exampleFile)
                    .readAllBytes()
            );
            examples.addAll(parseExamples(content));
            System.out.println("Loaded examples from " + exampleFile);
        }

        System.out.println("Total: " + examples.size() + " examples loaded");
    }

    @Test
    void testExample1_InstantQueryAllLabels() throws Exception {
        Example example = findExample("Example 1");
        assertNotNull(example, "Example 1 should exist");

        try (Connection conn = MetricsDatabaseReader.connect(examplesDb)) {
            QueryResult result = executeCommand(conn, example.command);

            // Verify row count
            assertEquals(5, result.rowCount(), "Example 1 should return 5 label sets");

            // Verify all values are present in the result
            Map<String, Double> expectedValues = Map.of(
                "endpoint=/users, method=GET, status=200", 11000.0,
                "endpoint=/products, method=GET, status=200", 8500.0,
                "endpoint=/users, method=POST, status=200", 1500.0,
                "endpoint=/users, method=GET, status=404", 75.0,
                "endpoint=/users, method=GET, status=500", 15.0
            );

            for (Map<String, Object> row : result.rows()) {
                String labels = (String) row.get("labels");
                double value = (double) row.get("value");

                boolean matched = false;
                for (Map.Entry<String, Double> expected : expectedValues.entrySet()) {
                    if (labelsMatch(labels, expected.getKey())) {
                        assertEquals(expected.getValue(), value, 0.01,
                            "Value mismatch for labels: " + labels);
                        matched = true;
                        break;
                    }
                }
                assertTrue(matched, "Unexpected label set: " + labels);
            }

            System.out.println("Example 1 output:");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testExample2_InstantQueryWithFilter() throws Exception {
        Example example = findExample("Example 2");
        assertNotNull(example, "Example 2 should exist");

        try (Connection conn = MetricsDatabaseReader.connect(examplesDb)) {
            QueryResult result = executeCommand(conn, example.command);

            // Should return exactly 1 row
            assertEquals(1, result.rowCount(), "Filtered query should return 1 row");

            Map<String, Object> row = result.rows().get(0);
            double value = (double) row.get("value");
            String labels = (String) row.get("labels");

            assertEquals(11000.0, value, 0.01);
            assertTrue(labels.contains("method=GET"));
            assertTrue(labels.contains("endpoint=/users"));
            assertTrue(labels.contains("status=200"));

            System.out.println("Example 2 output:");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testExample3_InstantQueryErrors() throws Exception {
        Example example = findExample("Example 3");
        assertNotNull(example, "Example 3 should exist");

        try (Connection conn = MetricsDatabaseReader.connect(examplesDb)) {
            QueryResult result = executeCommand(conn, example.command);

            assertEquals(1, result.rowCount(), "Should return 1 error metric");

            Map<String, Object> row = result.rows().get(0);
            double value = (double) row.get("value");
            String labels = (String) row.get("labels");

            assertEquals(15.0, value, 0.01);
            assertTrue(labels.contains("status=500"));

            System.out.println("Example 3 output:");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testExample4_InstantQuerySuccessful() throws Exception {
        Example example = findExample("Example 4");
        assertNotNull(example, "Example 4 should exist");

        try (Connection conn = MetricsDatabaseReader.connect(examplesDb)) {
            QueryResult result = executeCommand(conn, example.command);

            assertEquals(3, result.rowCount(), "Should return 3 successful request types");

            // Verify all have status=200
            for (Map<String, Object> row : result.rows()) {
                String labels = (String) row.get("labels");
                assertTrue(labels.contains("status=200"), "All results should have status=200");
            }

            System.out.println("Example 4 output:");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testExample5_RangeQueryTimeSeries() throws Exception {
        Example example = findExample("Example 5");
        assertNotNull(example, "Example 5 should exist");

        try (Connection conn = MetricsDatabaseReader.connect(examplesDb)) {
            QueryResult result = executeCommand(conn, example.command);

            // Should return 5 snapshots for the single label set
            assertEquals(5, result.rowCount(), "Should return 5 time-series points");

            // Verify values are: 0, 1000, 2200, 5500, 11000
            List<Double> values = result.rows().stream()
                .map(row -> (Double) row.get("value"))
                .toList();

            assertEquals(0.0, values.get(0), 0.01);
            assertEquals(1000.0, values.get(1), 0.01);
            assertEquals(2200.0, values.get(2), 0.01);
            assertEquals(5500.0, values.get(3), 0.01);
            assertEquals(11000.0, values.get(4), 0.01);

            System.out.println("Example 5 output:");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testExample6_RangeQueryErrors() throws Exception {
        Example example = findExample("Example 6");
        assertNotNull(example, "Example 6 should exist");

        try (Connection conn = MetricsDatabaseReader.connect(examplesDb)) {
            QueryResult result = executeCommand(conn, example.command);

            // Should return 5 snapshots for error metric
            assertEquals(5, result.rowCount(), "Should return 5 time-series points for errors");

            // Verify error values: 0, 2, 4, 8, 15
            List<Double> values = result.rows().stream()
                .map(row -> (Double) row.get("value"))
                .toList();

            assertEquals(0.0, values.get(0), 0.01);
            assertEquals(2.0, values.get(1), 0.01);
            assertEquals(4.0, values.get(2), 0.01);
            assertEquals(8.0, values.get(3), 0.01);
            assertEquals(15.0, values.get(4), 0.01);

            System.out.println("Example 6 output:");
            System.out.println(new TableFormatter().format(result));
        }
    }

    // Helper methods

    private static QueryResult executeCommand(Connection conn, String commandStr) throws Exception {
        // Parse command string: "instant --metric <name> [--labels key=val,...]"
        Map<String, Object> params = parseCommandParams(commandStr);

        if (commandStr.startsWith("instant")) {
            InstantCommand command = new InstantCommand();
            return command.execute(conn, params);
        } else if (commandStr.startsWith("range")) {
            RangeCommand command = new RangeCommand();
            return command.execute(conn, params);
        }

        throw new UnsupportedOperationException("Command not yet implemented: " + commandStr);
    }

    private static Map<String, Object> parseCommandParams(String commandStr) {
        Map<String, Object> params = new LinkedHashMap<>();

        // Extract --metric value
        Pattern metricPattern = Pattern.compile("--metric\\s+(\\S+)");
        Matcher metricMatcher = metricPattern.matcher(commandStr);
        if (metricMatcher.find()) {
            params.put("metric", metricMatcher.group(1));
        }

        // Extract --window value
        Pattern windowPattern = Pattern.compile("--window\\s+(\\S+)");
        Matcher windowMatcher = windowPattern.matcher(commandStr);
        if (windowMatcher.find()) {
            params.put("window", windowMatcher.group(1));
        }

        // Extract --last value
        Pattern lastPattern = Pattern.compile("--last\\s+(\\S+)");
        Matcher lastMatcher = lastPattern.matcher(commandStr);
        if (lastMatcher.find()) {
            params.put("last", lastMatcher.group(1));
        }

        // Extract --labels key=val,key=val
        Pattern labelsPattern = Pattern.compile("--labels\\s+(\\S+)");
        Matcher labelsMatcher = labelsPattern.matcher(commandStr);
        if (labelsMatcher.find()) {
            String labelsStr = labelsMatcher.group(1);
            Map<String, String> labels = new LinkedHashMap<>();
            for (String pair : labelsStr.split(",")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    labels.put(kv[0], kv[1]);
                }
            }
            params.put("labels", labels);
        }

        return params;
    }

    private static boolean labelsMatch(String actual, String expected) {
        // Normalize: remove spaces, sort key=value pairs
        String[] actualPairs = actual.replace(" ", "").split(",");
        String[] expectedPairs = expected.replace(" ", "").split(",");

        if (actualPairs.length != expectedPairs.length) {
            return false;
        }

        java.util.Arrays.sort(actualPairs);
        java.util.Arrays.sort(expectedPairs);

        return java.util.Arrays.equals(actualPairs, expectedPairs);
    }

    private static Example findExample(String exampleName) {
        for (Example example : examples) {
            if (example.name.contains(exampleName)) {
                return example;
            }
        }
        return null;
    }

    private static List<Example> parseExamples(String markdown) {
        List<Example> result = new ArrayList<>();

        // Pattern to match example sections
        Pattern examplePattern = Pattern.compile(
            "##\\s+(Example \\d+:[^\\n]+).*?" +
            "\\*\\*Command:\\*\\*\\s*```([^`]+)```",
            Pattern.DOTALL
        );

        Matcher matcher = examplePattern.matcher(markdown);
        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String command = matcher.group(2).trim();
            result.add(new Example(name, command));
        }

        return result;
    }

    private record Example(String name, String command) {}
}
