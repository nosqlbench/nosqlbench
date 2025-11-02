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
 * Enhanced Metrics command - Comprehensive metrics inventory with session context.
 *
 * Usage: metrics [--keep-labels activity,session] [--condense]
 *
 * Displays a complete metrics report in three sections:
 * 1. Session Metadata - Version, command line, hardware from label_metadata
 * 2. Metrics Summary - Each metric with type, samples, label_sets, and value range
 * 3. Label Set Tree - Advanced tree rendering organized by label sets
 *
 * This combines the best features of the former 'metrics' and 'summary' commands.
 */
public class MetricsCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "metrics";
    }

    @Override
    public String getDescription() {
        return "Display comprehensive metrics inventory with session context and label set tree";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        long startTime = System.currentTimeMillis();

        List<String> columns = new ArrayList<>();
        columns.add("section");
        columns.add("item");
        columns.add("value");
        columns.add("details");

        List<Map<String, Object>> rows = new ArrayList<>();

        // Parse parameters
        String keepLabelsParam = (String) params.getOrDefault("keep-labels", "activity,session");
        Set<String> keepLabels = parseKeepLabels(keepLabelsParam);
        boolean condense = (boolean) params.getOrDefault("condense", true);

        // Section 1: Session Metadata
        addSessionMetadata(conn, rows);

        // Section 2: Metrics Summary
        Map<String, String> commonLabels = findCommonLabels(conn, keepLabels);
        if (!commonLabels.isEmpty()) {
            String commonLabelsStr = formatLabelsAsString(commonLabels);
            String colorizedLabels = AnsiColors.colorizeCommonLabel(commonLabelsStr);
            rows.add(createRow("COMMON LABELS", colorizedLabels, "",
                "These labels are present in all metrics and elided from individual entries"));
        }
        addMetricsSummary(conn, rows);

        // Section 3: Label Set Tree
        addLabelSetTree(conn, rows, commonLabels, condense);

        long executionTime = System.currentTimeMillis() - startTime;

        String sql = "-- Multiple queries for comprehensive metrics inventory";
        return new QueryResult(columns, rows, sql, executionTime);
    }

    /**
     * Add session metadata from label_metadata table (nb.version, nb.commandline, nb.hardware).
     */
    private void addSessionMetadata(Connection conn, List<Map<String, Object>> rows) throws SQLException {
        String sql = """
            SELECT DISTINCT
              lm.metadata_key,
              lm.metadata_value
            FROM label_metadata lm
            WHERE lm.metadata_key IN ('nb.version', 'nb.commandline', 'nb.hardware')
            ORDER BY lm.metadata_key
            """;

        boolean hasMetadata = false;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                if (!hasMetadata) {
                    hasMetadata = true;
                }
                String key = rs.getString("metadata_key");
                String value = rs.getString("metadata_value");

                // Format the key nicely (nb.version -> Version)
                String displayKey = formatMetadataKey(key);
                rows.add(createRow("SESSION", displayKey, value, ""));
            }
        }

        // Add a blank row after session metadata if we had any
        if (hasMetadata) {
            rows.add(createRow("", "", "", ""));
        }
    }

    /**
     * Format metadata key for display (nb.version -> Version, nb.commandline -> Command Line).
     */
    private String formatMetadataKey(String key) {
        if (key.startsWith("nb.")) {
            String stripped = key.substring(3);
            // Convert camelCase or lowercase to Title Case
            if (stripped.equals("commandline")) {
                return "Command Line";
            } else if (stripped.equals("hardware")) {
                return "Hardware";
            } else if (stripped.equals("version")) {
                return "Version";
            }
            // Fallback: capitalize first letter
            return stripped.substring(0, 1).toUpperCase() + stripped.substring(1);
        }
        return key;
    }

    /**
     * Add metrics summary with type, samples, label_sets, and value range.
     */
    private void addMetricsSummary(Connection conn, List<Map<String, Object>> rows) throws SQLException {
        String metricSql = """
            SELECT
              mf.name AS metric_name,
              mf.type AS metric_type,
              mf.unit AS metric_unit,
              COUNT(DISTINCT mi.label_set_id) AS unique_label_sets,
              COUNT(*) AS total_samples,
              MIN(sv.value) AS min_value,
              MAX(sv.value) AS max_value
            FROM metric_family mf
            JOIN sample_name sn ON sn.metric_family_id = mf.id
            JOIN metric_instance mi ON mi.sample_name_id = sn.id
            JOIN sample_value sv ON sv.metric_instance_id = mi.id
            GROUP BY mf.id, mf.name, mf.type, mf.unit
            ORDER BY mf.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(metricSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("metric_name");
                String type = rs.getString("metric_type");
                String unit = rs.getString("metric_unit");
                int labelSets = rs.getInt("unique_label_sets");
                int samples = rs.getInt("total_samples");
                double min = rs.getDouble("min_value");
                double max = rs.getDouble("max_value");

                String unitDisplay = (unit == null || unit.isEmpty()) ? "" : " " + unit;
                String details = String.format("type=%s, samples=%d, label_sets=%d, range=[%.1f, %.1f]%s",
                    type, samples, labelSets, min, max, unitDisplay);

                rows.add(createRow("METRICS", name, "", details));
            }
        }

        // Add a blank row after metrics summary
        rows.add(createRow("", "", "", ""));
    }

    /**
     * Add label set tree from all metrics (like SummaryCommand's addAllMetrics).
     */
    private void addLabelSetTree(Connection conn, List<Map<String, Object>> rows,
                                  Map<String, String> commonLabels, boolean condense) throws SQLException {
        // Build one canonical tree for ALL metrics
        LabelSetTree canonicalTree = new LabelSetTree();

        // Get all metrics
        String metricSql = """
            SELECT DISTINCT mf.name
            FROM metric_family mf
            JOIN sample_name sn ON sn.metric_family_id = mf.id
            JOIN metric_instance mi ON mi.sample_name_id = sn.id
            ORDER BY mf.name
            """;

        try (PreparedStatement ps = conn.prepareStatement(metricSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String metricName = rs.getString("name");
                addMetricToTree(conn, canonicalTree, metricName, commonLabels);
            }
        }

        // Convert to display tree and render
        DisplayTree displayTree = DisplayTree.fromLabelSetTree(canonicalTree, condense);
        List<String> lines = displayTree.render(true); // true for colorized output
        for (String line : lines) {
            rows.add(createRow("LABEL SETS", line, "", ""));
        }
    }

    /**
     * Add a metric and its label sets to the shared canonical tree.
     */
    private void addMetricToTree(Connection conn, LabelSetTree canonicalTree, String metricName,
                                  Map<String, String> commonLabels) throws SQLException {
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
            WHERE ls.id IN (SELECT DISTINCT label_set_id FROM metric_instance)
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

    private Map<String, Object> createRow(String section, String item, String value, String details) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("section", section);
        row.put("item", item);
        row.put("value", value);
        row.put("details", details);
        return row;
    }

    @Override
    public void validate(Map<String, Object> params) throws InvalidQueryException {
        // No required parameters
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # Show comprehensive metrics inventory
              metrics

              # Keep specific labels (don't elide)
              metrics --keep-labels activity,session

              # Keep all labels (no elision)
              metrics --keep-labels '*'

              # Disable sibling condensation
              metrics --no-condense

              # Markdown format for documentation
              metrics --format markdown

              # Use at end of session for complete report
              nb5 run.yaml && nb5 mql metrics
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
