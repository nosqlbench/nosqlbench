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

import io.nosqlbench.nb.mql.cli.IncreaseCLI;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.query.TimeWindowParser;
import io.nosqlbench.nb.mql.schema.MetricsSchema;
import picocli.CommandLine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Increase command - Calculate total increase in counter value over a time window.
 *
 * Usage: increase --metric <counter_metric> --window <duration> [--labels key=val,...]
 *
 * Returns the total increase (max - min) for each label set within the time window.
 * This is the standard PromQL increase() function.
 *
 * Important: Works only with counter metrics (monotonically increasing values).
 */
public class IncreaseCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "increase";
    }

    @Override
    public String getDescription() {
        return "Calculate total increase in counter over time window";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String metric = (String) params.get("metric");
        @SuppressWarnings("unchecked")
        Map<String, String> labels = (Map<String, String>) params.getOrDefault("labels", Map.of());

        long startTime = System.currentTimeMillis();

        // Determine time range
        Long startTimestampMs = null;
        Long endTimestampMs = null;

        if (params.containsKey("window")) {
            String window = (String) params.get("window");
            long windowMs = TimeWindowParser.parseToMillis(window);
            endTimestampMs = getCurrentMaxTimestamp(conn);
            startTimestampMs = endTimestampMs - windowMs;
        } else if (params.containsKey("last")) {
            String last = (String) params.get("last");
            long windowMs = TimeWindowParser.parseToMillis(last);
            endTimestampMs = getCurrentMaxTimestamp(conn);
            startTimestampMs = endTimestampMs - windowMs;
        }

        // Build the query
        String sql = buildQuery(labels, startTimestampMs != null);

        List<String> columns = new ArrayList<>();
        columns.add("increase");
        columns.add("labels");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, metric);

            if (startTimestampMs != null) {
                ps.setLong(paramIndex++, startTimestampMs);
                ps.setLong(paramIndex++, endTimestampMs);
            }

            // Add label filter parameters
            for (Map.Entry<String, String> label : labels.entrySet()) {
                ps.setString(paramIndex++, label.getKey());
                ps.setString(paramIndex++, label.getValue());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("increase", rs.getDouble("increase"));
                    row.put("labels", rs.getString("labels"));
                    rows.add(row);
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return new QueryResult(columns, rows, sql, executionTime);
    }

    @Override
    public void validate(Map<String, Object> params) throws InvalidQueryException {
        if (!params.containsKey("metric")) {
            throw new InvalidQueryException("Missing required parameter: metric");
        }

        Object metric = params.get("metric");
        if (!(metric instanceof String) || ((String) metric).trim().isEmpty()) {
            throw new InvalidQueryException("Parameter 'metric' must be a non-empty string");
        }

        // Check that either window OR last is provided
        boolean hasWindow = params.containsKey("window");
        boolean hasLast = params.containsKey("last");

        if (!hasWindow && !hasLast) {
            throw new InvalidQueryException(
                "Must specify time window using:\n" +
                "  --window <duration> (e.g., 5m, 1h)\n" +
                "  --last <duration> (e.g., 5m, 1h)"
            );
        }

        if (hasWindow && hasLast) {
            throw new InvalidQueryException(
                "Cannot specify both --window and --last. Choose one."
            );
        }

        // Validate labels if present
        if (params.containsKey("labels")) {
            Object labels = params.get("labels");
            if (!(labels instanceof Map)) {
                throw new InvalidQueryException("Parameter 'labels' must be a Map<String, String>");
            }
        }
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # Total requests in last hour
              increase --metric api_requests_total --window 1h

              # Total errors in last 5 minutes
              increase --metric api_requests_total --window 5m --labels status=500

              # Operations completed in last day
              increase --metric activity_ops_total --window 1d
            """;
    }

    private String buildQuery(Map<String, String> labelFilters, boolean hasTimeRange) {
        StringBuilder sql = new StringBuilder();

        sql.append("WITH timeseries AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE).append(",\n");
        sql.append("    GROUP_CONCAT(lk.").append(MetricsSchema.COL_LK_NAME)
           .append(" || '=' || lv.").append(MetricsSchema.COL_LV_VALUE).append(", ', ') AS labels\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv\n");
        sql.append("  JOIN ").append(MetricsSchema.TABLE_SAMPLE_NAME).append(" sn ON sn.")
           .append(MetricsSchema.COL_SN_ID).append(" = sv.").append(MetricsSchema.COL_SV_SAMPLE_NAME_ID).append("\n");
        sql.append("  ").append(MetricsSchema.joinAllLabels()).append("\n");
        sql.append("  WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");

        if (hasTimeRange) {
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" >= ?\n");
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" <= ?\n");
        }

        // Add label filters
        for (String labelKey : labelFilters.keySet()) {
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append(" IN (\n");
            sql.append("      SELECT lsm.").append(MetricsSchema.COL_LSM_LABEL_SET_ID).append("\n");
            sql.append("      FROM ").append(MetricsSchema.TABLE_LABEL_SET_MEMBERSHIP).append(" lsm\n");
            sql.append("      JOIN ").append(MetricsSchema.TABLE_LABEL_KEY).append(" lk ON lk.")
               .append(MetricsSchema.COL_LK_ID).append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_KEY_ID).append("\n");
            sql.append("      JOIN ").append(MetricsSchema.TABLE_LABEL_VALUE).append(" lv ON lv.")
               .append(MetricsSchema.COL_LV_ID).append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_VALUE_ID).append("\n");
            sql.append("      WHERE lk.").append(MetricsSchema.COL_LK_NAME).append(" = ? AND lv.")
               .append(MetricsSchema.COL_LV_VALUE).append(" = ?\n");
            sql.append("    )\n");
        }

        sql.append("  GROUP BY sv.").append(MetricsSchema.COL_SV_ID).append("\n");
        sql.append(")\n");

        // Calculate increase as MAX - MIN within window for each label set
        sql.append("SELECT\n");
        sql.append("  MAX(value) - MIN(value) AS increase,\n");
        sql.append("  labels\n");
        sql.append("FROM timeseries\n");
        sql.append("GROUP BY label_set_id, labels\n");
        sql.append("HAVING MAX(value) > MIN(value)\n");  // Only show increases > 0
        sql.append("ORDER BY labels");

        return sql.toString();
    }

    private long getCurrentMaxTimestamp(Connection conn) throws SQLException {
        String sql = "SELECT MAX(" + MetricsSchema.COL_SV_TIMESTAMP_MS + ") FROM " +
                     MetricsSchema.TABLE_SAMPLE_VALUE;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("No data in database");
        }
    }

    /**
     * Standalone main method for direct execution of increase queries.
     * Delegates to IncreaseCLI for picocli argument parsing.
     *
     * Usage: java -cp ... io.nosqlbench.nb.mql.commands.IncreaseCommand --metric ops_total --window 1h
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new IncreaseCLI()).execute(args);
        System.exit(exitCode);
    }
}
