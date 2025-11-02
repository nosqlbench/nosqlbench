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

import io.nosqlbench.nb.mql.cli.MetricsCLI;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.util.AnsiColors;
import io.nosqlbench.nb.mql.util.LabelSetTree;
import io.nosqlbench.nb.mql.util.DisplayTree;
import picocli.CommandLine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Metrics command - List all metrics with their label sets in tree view.
 *
 * Usage: metrics [--group-by name|labelset]
 *
 * Displays metrics inventory with two view options:
 * - name (default): Group by metric name, showing label sets under each metric
 * - labelset: Group by label set, showing which metrics use each label combination
 */
public class MetricsCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "metrics";
    }

    @Override
    public String getDescription() {
        return "List all metrics with their label sets in tree view";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String groupBy = (String) params.getOrDefault("group-by", "name");

        long startTime = System.currentTimeMillis();

        List<String> columns = new ArrayList<>();
        columns.add("group");
        columns.add("item");
        columns.add("details");

        List<Map<String, Object>> rows = new ArrayList<>();

        // Parse keep-labels parameter
        String keepLabelsParam = (String) params.getOrDefault("keep-labels", "activity,session");
        Set<String> keepLabels = parseKeepLabels(keepLabelsParam);

        // Parse condense parameter (default true)
        boolean condense = (boolean) params.getOrDefault("condense", true);

        // First, collect all label sets to find common labels (excluding kept labels)
        Map<String, String> commonLabels = findCommonLabels(conn, keepLabels);

        // Add common labels header if any exist
        if (!commonLabels.isEmpty()) {
            String commonLabelsStr = formatLabelsAsString(commonLabels);
            String colorizedLabels = AnsiColors.colorizeCommonLabel(commonLabelsStr);
            rows.add(createRow("COMMON LABELS", colorizedLabels, "These labels are present in all metrics and elided from individual entries"));
        }

        if ("name".equals(groupBy)) {
            addMetricsWithLabelSets(conn, rows, commonLabels);
        } else if ("labelset".equals(groupBy)) {
            addLabelSetsWithMetrics(conn, rows, commonLabels, condense);
        }

        long executionTime = System.currentTimeMillis() - startTime;

        String sql = "-- Multiple queries for metrics inventory (group-by=" + groupBy + ")";
        return new QueryResult(columns, rows, sql, executionTime);
    }

    private void addMetricsWithLabelSets(Connection conn, List<Map<String, Object>> rows, Map<String, String> commonLabels) throws SQLException {
        // Get all metrics
        String metricsSql = """
            SELECT
              mf.name, mf.type,
              COUNT(DISTINCT mi.label_set_id) as label_set_count,
              COUNT(DISTINCT mi.id) as sample_count
            FROM metric_family mf
            JOIN sample_name sn ON sn.metric_family_id = mf.id
            JOIN metric_instance mi ON mi.sample_name_id = sn.id
            GROUP BY mf.id
            ORDER BY mf.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(metricsSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                int labelSetCount = rs.getInt("label_set_count");
                int sampleCount = rs.getInt("sample_count");

                String details = String.format("type=%s, samples=%d, label_sets=%d",
                    type, sampleCount, labelSetCount);

                rows.add(createRow(name, "METRIC", details));

                // Get label sets for this metric
                getLabelSetsForMetric(conn, rows, name, commonLabels);
            }
        }
    }

    private void getLabelSetsForMetric(Connection conn, List<Map<String, Object>> rows, String metricName, Map<String, String> commonLabels) throws SQLException {
        String labelSetSql = """
            WITH metric_label_sets AS (
              SELECT DISTINCT ls.id, ls.hash
              FROM metric_family mf
              JOIN sample_name sn ON sn.metric_family_id = mf.id
              JOIN metric_instance mi ON mi.sample_name_id = sn.id
              JOIN label_set ls ON mi.label_set_id = ls.id
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
            ORDER BY labels
            """;

        try (PreparedStatement ps = conn.prepareStatement(labelSetSql)) {
            ps.setString(1, metricName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String labels = rs.getString("labels");

                    // Format in MetricsQL/PromQL syntax: metric_name{label="value",label2="value2"}
                    String metricsqlFormat = formatAsMetricsQL(metricName, labels, commonLabels);

                    rows.add(createRow(metricName, metricsqlFormat, ""));
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

    private void addLabelSetsWithMetrics(Connection conn, List<Map<String, Object>> rows, Map<String, String> commonLabels, boolean condense) throws SQLException {
        // Get all unique label sets
        String labelSetsSql = """
            SELECT DISTINCT
              ls.id, ls.hash,
              GROUP_CONCAT(lk.name || '=' || lv.value, ', ') as labels
            FROM label_set ls
            LEFT JOIN label_set_membership lsm ON lsm.label_set_id = ls.id
            LEFT JOIN label_key lk ON lk.id = lsm.label_key_id
            LEFT JOIN label_value lv ON lv.id = lsm.label_value_id
            WHERE ls.id IN (SELECT DISTINCT label_set_id FROM sample_value)
            GROUP BY ls.id, ls.hash
            """;

        // Build canonical label set tree
        LabelSetTree canonicalTree = new LabelSetTree();

        try (PreparedStatement ps = conn.prepareStatement(labelSetsSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int labelSetId = rs.getInt("id");
                String labels = rs.getString("labels");

                // Parse labels to map
                Map<String, String> labelMap = parseLabelsToMap(labels);

                // Elide common labels
                labelMap.entrySet().removeIf(entry ->
                    commonLabels.containsKey(entry.getKey()) &&
                    commonLabels.get(entry.getKey()).equals(entry.getValue())
                );

                // Collect metrics for this label set
                List<String> metrics = getMetricsForLabelSetList(conn, labelSetId);

                // Add to canonical tree
                canonicalTree.addLabelSet(labelMap, metrics);
            }
        }

        // Convert to display tree for rendering
        DisplayTree displayTree = DisplayTree.fromLabelSetTree(canonicalTree, condense);

        // Render the display tree
        List<String> lines = displayTree.render(true); // true for colorized output
        for (String line : lines) {
            rows.add(createRow(line, "", ""));
        }
    }


    private List<String> getMetricsForLabelSetList(Connection conn, int labelSetId) throws SQLException {
        List<String> metrics = new ArrayList<>();
        String metricsSql = """
            SELECT DISTINCT mf.name
            FROM metric_instance mi
            JOIN sample_name sn ON mi.sample_name_id = sn.id
            JOIN metric_family mf ON sn.metric_family_id = mf.id
            WHERE mi.label_set_id = ?
            ORDER BY mf.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(metricsSql)) {
            ps.setInt(1, labelSetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    metrics.add(rs.getString("name"));
                }
            }
        }
        return metrics;
    }

    private void getMetricsForLabelSet(Connection conn, List<Map<String, Object>> rows,
                                      int labelSetId, String labelSetName) throws SQLException {
        String metricsSql = """
            SELECT DISTINCT mf.name
            FROM metric_instance mi
            JOIN sample_name sn ON mi.sample_name_id = sn.id
            JOIN metric_family mf ON sn.metric_family_id = mf.id
            WHERE mi.label_set_id = ?
            ORDER BY mf.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(metricsSql)) {
            ps.setInt(1, labelSetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String metricName = rs.getString("name");
                    // Use empty group to indicate this is a child of the label set
                    rows.add(createRow("", metricName, ""));
                }
            }
        }
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

    private Map<String, Object> createRow(String group, String item, String details) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("group", group);
        row.put("item", item);
        row.put("details", details);
        return row;
    }

    @Override
    public void validate(Map<String, Object> params) throws InvalidQueryException {
        if (params.containsKey("group-by")) {
            String groupBy = (String) params.get("group-by");
            if (!"name".equals(groupBy) && !"labelset".equals(groupBy)) {
                throw new InvalidQueryException(
                    "Invalid group-by value: " + groupBy + "\n" +
                    "Valid values: name, labelset"
                );
            }
        }
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # List metrics grouped by name (default)
              metrics

              # List metrics grouped by label set
              metrics --group-by labelset

              # Markdown format for reports
              metrics --format markdown
            """;
    }

    /**
     * Standalone main method for direct execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MetricsCLI()).execute(args);
        System.exit(exitCode);
    }
}
