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

import io.nosqlbench.nb.mql.cli.SummaryCLI;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsSchema;
import io.nosqlbench.nb.mql.util.AnsiColors;
import io.nosqlbench.nb.mql.util.LabelSetTree;
import io.nosqlbench.nb.mql.util.DisplayTree;
import picocli.CommandLine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Summary command - Display comprehensive end-of-session metrics report.
 *
 * Usage: summary
 *
 * Provides an elaborate session summary focusing on NoSQLBench core metrics:
 * - Session timeline and duration
 * - Core operation metrics (cycles, result, tries, errors)
 * - Activity breakdowns
 * - Error analysis
 * - Performance indicators
 *
 * Designed to give a complete picture of the session at a glance.
 */
public class SummaryCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "summary";
    }

    @Override
    public String getDescription() {
        return "Display comprehensive end-of-session metrics report";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        long startTime = System.currentTimeMillis();

        // Build comprehensive summary with multiple sections
        List<String> columns = new ArrayList<>();
        columns.add("section");
        columns.add("metric");
        columns.add("value");
        columns.add("details");

        List<Map<String, Object>> rows = new ArrayList<>();

        // Section 1: Session Overview
        addSessionOverview(conn, rows);

        // Section 2: Core Metrics Summary (cycles, result, tries, errors)
        addCoreMetricsSummary(conn, rows);

        // Section 3: Activity Breakdown
        addActivityBreakdown(conn, rows);

        // Section 4: Error Analysis
        addErrorAnalysis(conn, rows);

        // Section 5: All Available Metrics
        // Parse keep-labels parameter
        String keepLabelsParam = (String) params.getOrDefault("keep-labels", "activity,session");
        Set<String> keepLabels = parseKeepLabels(keepLabelsParam);

        // First, find common labels to elide from display (excluding kept labels)
        Map<String, String> commonLabels = findCommonLabels(conn, keepLabels);
        addAllMetrics(conn, rows, commonLabels);

        long executionTime = System.currentTimeMillis() - startTime;

        // Build a single SQL representing all queries (for reference)
        String combinedSql = "-- Multiple queries executed for comprehensive summary\n" +
                           "-- See SummaryCommand.java for details";

        return new QueryResult(columns, rows, combinedSql, executionTime);
    }

    private void addSessionOverview(Connection conn, List<Map<String, Object>> rows) throws SQLException {
        String sql = """
            SELECT
              MIN(timestamp_ms) as first_ts,
              MAX(timestamp_ms) as last_ts,
              COUNT(DISTINCT timestamp_ms) as snapshots,
              COUNT(*) as total_samples
            FROM sample_value
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                long firstMs = rs.getLong("first_ts");
                long lastMs = rs.getLong("last_ts");
                int snapshots = rs.getInt("snapshots");
                int totalSamples = rs.getInt("total_samples");
                long durationMs = lastMs - firstMs;

                rows.add(createRow("SESSION", "Time Range",
                    new Timestamp(firstMs) + " to " + new Timestamp(lastMs),
                    formatDuration(durationMs)));

                rows.add(createRow("SESSION", "Snapshots", String.valueOf(snapshots),
                    "Total samples: " + totalSamples));

                if (snapshots > 1) {
                    long avgInterval = durationMs / (snapshots - 1);
                    rows.add(createRow("SESSION", "Avg Interval", formatDuration(avgInterval),
                        "~" + (avgInterval / 1000) + "s cadence"));
                }
            }
        }
    }

    private void addCoreMetricsSummary(Connection conn, List<Map<String, Object>> rows) throws SQLException {
        // Query for core NoSQLBench metrics: cycles, result, tries, errors
        String[] coreMetrics = {"cycles", "result", "tries", "errors"};

        for (String metricBase : coreMetrics) {
            String sql = """
                SELECT
                  SUM(sv.value) as total_value,
                  COUNT(DISTINCT sv.label_set_id) as label_sets
                FROM sample_value sv
                JOIN sample_name sn ON sv.sample_name_id = sn.id
                WHERE sn.sample LIKE ?
                  AND sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
                """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + metricBase + "%");

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double total = rs.getDouble("total_value");
                        int labelSets = rs.getInt("label_sets");

                        if (labelSets > 0) {
                            rows.add(createRow("CORE METRICS", metricBase.toUpperCase(),
                                String.format("%.0f", total),
                                labelSets + " label set(s)"));
                        }
                    }
                }
            }
        }

        // Calculate error rate if we have both result and errors
        calculateErrorRate(conn, rows);
    }

    private void calculateErrorRate(Connection conn, List<Map<String, Object>> rows) throws SQLException {
        String sql = """
            SELECT
              SUM(CASE WHEN sn.sample LIKE '%result%' THEN sv.value ELSE 0 END) as results,
              SUM(CASE WHEN sn.sample LIKE '%error%' THEN sv.value ELSE 0 END) as errors
            FROM sample_value sv
            JOIN sample_name sn ON sv.sample_name_id = sn.id
            WHERE sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double results = rs.getDouble("results");
                double errors = rs.getDouble("errors");

                if (results > 0) {
                    double errorRate = (errors / results) * 100.0;
                    rows.add(createRow("CORE METRICS", "Error Rate",
                        String.format("%.2f%%", errorRate),
                        String.format("%.0f errors / %.0f results", errors, results)));
                }
            }
        }
    }

    private void addActivityBreakdown(Connection conn, List<Map<String, Object>> rows) throws SQLException {
        // Group by activity label if it exists
        String sql = """
            SELECT
              lv.value as activity,
              SUM(sv.value) as total
            FROM sample_value sv
            JOIN sample_name sn ON sv.sample_name_id = sn.id
            JOIN label_set_membership lsm ON sv.label_set_id = lsm.label_set_id
            JOIN label_key lk ON lsm.label_key_id = lk.id
            JOIN label_value lv ON lsm.label_value_id = lv.id
            WHERE lk.name = 'activity'
              AND sn.sample LIKE '%ops%'
              AND sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
            GROUP BY lv.value
            ORDER BY total DESC
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String activity = rs.getString("activity");
                double total = rs.getDouble("total");

                rows.add(createRow("ACTIVITY", activity,
                    String.format("%.0f", total), "operations"));
            }
        }
    }

    private void addErrorAnalysis(Connection conn, List<Map<String, Object>> rows) throws SQLException {
        String sql = """
            SELECT
              sn.sample,
              SUM(sv.value) as total_errors
            FROM sample_value sv
            JOIN sample_name sn ON sv.sample_name_id = sn.id
            WHERE sn.sample LIKE '%error%'
              AND sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
            GROUP BY sn.sample
            HAVING total_errors > 0
            ORDER BY total_errors DESC
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String sample = rs.getString("sample");
                double errors = rs.getDouble("total_errors");

                rows.add(createRow("ERRORS", sample,
                    String.format("%.0f", errors), "total errors"));
            }
        }
    }

    private void addAllMetrics(Connection conn, List<Map<String, Object>> rows, Map<String, String> commonLabels) throws SQLException {
        // Add common labels header if any exist
        if (!commonLabels.isEmpty()) {
            String commonLabelsStr = formatLabelsAsString(commonLabels);
            String colorizedLabels = AnsiColors.colorizeCommonLabel(commonLabelsStr);
            rows.add(createRow("ALL METRICS", "Common Labels", colorizedLabels,
                "These labels are present in all metrics and elided from individual entries"));
        }
        // First, get basic metric info
        String metricSql = """
            SELECT
              mf.name AS metric_name,
              mf.type AS metric_type,
              COUNT(DISTINCT sv.label_set_id) AS unique_label_sets,
              COUNT(*) AS total_samples,
              MIN(sv.value) AS min_value,
              MAX(sv.value) AS max_value
            FROM metric_family mf
            JOIN sample_name sn ON sn.metric_family_id = mf.id
            JOIN sample_value sv ON sv.sample_name_id = sn.id
            GROUP BY mf.id, mf.name, mf.type
            ORDER BY mf.name
            """;

        // Build one canonical tree for ALL metrics
        LabelSetTree canonicalTree = new LabelSetTree();

        try (PreparedStatement ps = conn.prepareStatement(metricSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("metric_name");
                String type = rs.getString("metric_type");
                int labelSets = rs.getInt("unique_label_sets");
                int samples = rs.getInt("total_samples");
                double min = rs.getDouble("min_value");
                double max = rs.getDouble("max_value");

                String details = String.format("type=%s, samples=%d, label_sets=%d, range=[%.1f, %.1f]",
                    type, samples, labelSets, min, max);

                rows.add(createRow("ALL METRICS", name, "", details));

                // Add this metric's label sets to the shared tree
                addMetricToTree(conn, canonicalTree, name, commonLabels);
            }
        }

        // Convert to display tree and render once for all metrics
        DisplayTree displayTree = DisplayTree.fromLabelSetTree(canonicalTree);
        List<String> lines = displayTree.render(true);
        for (String line : lines) {
            rows.add(createRow("LABEL SET", "", line, ""));
        }
    }

    /**
     * Add a metric and its label sets to the shared canonical tree.
     */
    private void addMetricToTree(Connection conn, LabelSetTree canonicalTree, String metricName, Map<String, String> commonLabels) throws SQLException {
        String labelSetSql = """
            WITH metric_label_sets AS (
              SELECT DISTINCT ls.id, ls.hash
              FROM metric_family mf
              JOIN sample_name sn ON sn.metric_family_id = mf.id
              JOIN sample_value sv ON sv.sample_name_id = sn.id
              JOIN label_set ls ON sv.label_set_id = ls.id
              WHERE mf.name = ?
            )
            SELECT
              mls.hash,
              GROUP_CONCAT(lk.name || '=' || lv.value, ', ') as labels
            FROM metric_label_sets mls
            LEFT JOIN label_set_membership lsm ON lsm.label_set_id = mls.id
            LEFT JOIN label_key lk ON lk.id = lsm.label_key_id
            LEFT JOIN label_value lv ON lv.id = lsm.label_value_id
            GROUP BY mls.id, mls.hash
            """;

        try (PreparedStatement ps = conn.prepareStatement(labelSetSql)) {
            ps.setString(1, metricName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String labels = rs.getString("labels");

                    // Parse labels to map
                    Map<String, String> labelMap = parseLabelsToMap(labels);

                    // Elide common labels
                    labelMap.entrySet().removeIf(entry ->
                        commonLabels.containsKey(entry.getKey()) &&
                        commonLabels.get(entry.getKey()).equals(entry.getValue())
                    );

                    // Add to shared canonical tree with metric name
                    canonicalTree.addLabelSet(labelMap, List.of(metricName));
                }
            }
        }
    }


    /**
     * Format metric with labels in PromQL/MetricsQL syntax, eliding common labels.
     * Examples:
     *   api_requests_total{method="GET",endpoint="/users",status="200"}
     *   api_latency{endpoint="/api"}
     *   simple_counter{}
     */
    private String formatAsMetricsQL(String metricName, String labels, Map<String, String> commonLabels) {
        if (labels == null || labels.isEmpty()) {
            return metricName + "{}";
        }

        // Convert "key=value, key2=value2" to 'key="value",key2="value2"'
        // But elide any labels that are common
        String[] pairs = labels.split(", ");
        StringBuilder sb = new StringBuilder();
        sb.append(metricName).append("{");

        boolean first = true;
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0];
                String value = kv[1];

                // Skip if this label is in common labels
                if (commonLabels.containsKey(key) && commonLabels.get(key).equals(value)) {
                    continue;
                }

                if (!first) {
                    sb.append(",");
                }
                sb.append(key).append("=\"").append(value).append("\"");
                first = false;
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Parse the keep-labels parameter into a set of label names to keep.
     * Handles '*' for keeping all labels, and comma/tab-separated label names.
     */
    private Set<String> parseKeepLabels(String keepLabelsParam) {
        if (keepLabelsParam == null || keepLabelsParam.isEmpty()) {
            return new LinkedHashSet<>();
        }

        // Special case: '*' means keep all labels (don't elide any)
        if ("*".equals(keepLabelsParam.trim())) {
            return null; // null indicates keep all
        }

        // Split by comma or tab
        String[] labels = keepLabelsParam.split("[,\\t]+");
        Set<String> result = new LinkedHashSet<>();
        for (String label : labels) {
            String trimmed = label.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Find labels that are common to ALL label sets across all metrics.
     * Only labels with identical key-value pairs in every label set are considered common.
     * Labels in keepLabels will be excluded from the result (not elided).
     *
     * @param keepLabels Set of label names to keep (not elide), or null to keep all labels
     */
    private Map<String, String> findCommonLabels(Connection conn, Set<String> keepLabels) throws SQLException {
        // If keepLabels is null (meaning '*'), return empty map (keep all labels)
        if (keepLabels == null) {
            return new LinkedHashMap<>();
        }
        String sql = """
            SELECT
              ls.id,
              GROUP_CONCAT(lk.name || '=' || lv.value, ', ') as labels
            FROM label_set ls
            LEFT JOIN label_set_membership lsm ON lsm.label_set_id = ls.id
            LEFT JOIN label_key lk ON lk.id = lsm.label_key_id
            LEFT JOIN label_value lv ON lv.id = lsm.label_value_id
            WHERE ls.id IN (SELECT DISTINCT label_set_id FROM sample_value)
            GROUP BY ls.id
            """;

        Map<String, String> commonLabels = null;
        int labelSetCount = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                labelSetCount++;
                String labels = rs.getString("labels");
                Map<String, String> labelMap = parseLabelsToMap(labels);

                if (commonLabels == null) {
                    // First label set - all its labels are potentially common
                    commonLabels = new LinkedHashMap<>(labelMap);
                } else {
                    // Intersect with existing common labels
                    commonLabels.entrySet().removeIf(entry ->
                        !labelMap.containsKey(entry.getKey()) ||
                        !labelMap.get(entry.getKey()).equals(entry.getValue())
                    );
                }

                // Early exit if no common labels remain
                if (commonLabels != null && commonLabels.isEmpty()) {
                    break;
                }
            }
        }

        // Return empty map if no label sets or no common labels
        if (commonLabels == null || labelSetCount < 2) {
            return new LinkedHashMap<>();
        }

        // Filter out labels that should be kept (not elided)
        commonLabels.keySet().removeIf(keepLabels::contains);

        return commonLabels;
    }

    /**
     * Parse "key=value, key2=value2" format into a Map.
     */
    private Map<String, String> parseLabelsToMap(String labels) {
        Map<String, String> map = new LinkedHashMap<>();
        if (labels == null || labels.isEmpty()) {
            return map;
        }

        String[] pairs = labels.split(", ");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    /**
     * Format labels map as a comma-separated string.
     */
    private String formatLabelsAsString(Map<String, String> labels) {
        if (labels.isEmpty()) {
            return "{}";
        }

        return labels.entrySet().stream()
            .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
            .reduce((a, b) -> a + ", " + b)
            .orElse("{}");
    }

    /**
     * Remove common labels from a label string and return the remaining labels.
     */
    private String elideCommonLabels(String labels, Map<String, String> commonLabels) {
        if (labels == null || labels.isEmpty() || commonLabels.isEmpty()) {
            return labels;
        }

        Map<String, String> labelMap = parseLabelsToMap(labels);

        // Remove common labels
        commonLabels.forEach((key, value) -> {
            if (labelMap.containsKey(key) && labelMap.get(key).equals(value)) {
                labelMap.remove(key);
            }
        });

        // Format remaining labels
        if (labelMap.isEmpty()) {
            return "";
        }

        return labelMap.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    /**
     * Format labels in PromQL style with curly braces and quoted values.
     * Input: "key=value, key2=value2" or empty/null
     * Output: {key="value",key2="value2"} or {}
     */
    private String formatLabelsAsPromQL(String labels) {
        if (labels == null || labels.isEmpty()) {
            return "{}";
        }

        // Parse "key=value, key2=value2" format
        String[] pairs = labels.split(", ");
        StringBuilder sb = new StringBuilder("{");

        boolean first = true;
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(kv[0]).append("=\"").append(kv[1]).append("\"");
                first = false;
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Count the number of labels in a label string.
     * Empty or null strings return 0.
     */
    private int countLabels(String labels) {
        if (labels == null || labels.isEmpty()) {
            return 0;
        }
        // Split by ", " to count label pairs
        return labels.split(", ").length;
    }

    private Map<String, Object> createRow(String section, String metric, String value, String details) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("section", section);
        row.put("metric", metric);
        row.put("value", value);
        row.put("details", details);
        return row;
    }

    private String formatDuration(long millis) {
        if (millis == 0) {
            return "0s";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public void validate(Map<String, Object> params) throws InvalidQueryException {
        // No parameters required for summary command
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # Show comprehensive session summary
              summary

              # JSON format for scripting
              summary --format json

              # Markdown for reports
              summary --format markdown

              # Use at end of session
              nb5 run.yaml && nb5 mql summary
            """;
    }

    /**
     * Standalone main method for direct execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new SummaryCLI()).execute(args);
        System.exit(exitCode);
    }
}
