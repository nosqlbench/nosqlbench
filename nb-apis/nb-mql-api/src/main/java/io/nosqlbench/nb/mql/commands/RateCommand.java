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

import io.nosqlbench.nb.mql.cli.RateCLI;
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
 * Rate command - Calculate per-second rate of change for counter metrics.
 *
 * Usage: rate --metric <counter_metric> --window <duration> [--labels key=val,...]
 *
 * Calculates the per-second rate by taking the difference between consecutive data points
 * and dividing by the time interval between them. This is the standard PromQL rate() function.
 *
 * Important: Works only with counter metrics (monotonically increasing values).
 */
public class RateCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "rate";
    }

    @Override
    public String getDescription() {
        return "Calculate per-second rate of change for counters";
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
        columns.add("timestamp");
        columns.add("rate_per_sec");
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
                    row.put("timestamp", rs.getTimestamp("timestamp"));
                    row.put("rate_per_sec", rs.getDouble("rate_per_sec"));
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

        // Check that either window OR last is provided (rate needs a time context)
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
              # Calculate request rate over last 5 minutes
              rate --metric api_requests_total --window 5m

              # Alternative syntax
              rate --metric api_requests_total --last 5m

              # With label filters
              rate --metric api_requests_total --window 5m --labels method=GET,status=200

              # Longer window for smoother rate
              rate --metric activity_ops_total --window 1h
            """;
    }

    private String buildQuery(Map<String, String> labelFilters, boolean hasTimeRange) {
        StringBuilder sql = new StringBuilder();

        // Use window functions to calculate rate between consecutive points
        sql.append("WITH timeseries AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append(",\n");
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
        sql.append("  ORDER BY sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append(", sv.")
           .append(MetricsSchema.COL_SV_TIMESTAMP_MS).append("\n");
        sql.append("),\n");

        // Calculate rates using LAG window function
        sql.append("rates AS (\n");
        sql.append("  SELECT\n");
        sql.append("    timestamp_ms,\n");
        sql.append("    label_set_id,\n");
        sql.append("    labels,\n");
        sql.append("    value,\n");
        sql.append("    LAG(value) OVER (PARTITION BY label_set_id ORDER BY timestamp_ms) AS prev_value,\n");
        sql.append("    LAG(timestamp_ms) OVER (PARTITION BY label_set_id ORDER BY timestamp_ms) AS prev_timestamp\n");
        sql.append("  FROM timeseries\n");
        sql.append(")\n");

        sql.append("SELECT\n");
        sql.append("  datetime(timestamp_ms / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  CASE\n");
        sql.append("    WHEN prev_value IS NULL OR prev_timestamp IS NULL THEN NULL\n");
        sql.append("    WHEN timestamp_ms = prev_timestamp THEN 0.0\n");
        sql.append("    ELSE (value - prev_value) / ((timestamp_ms - prev_timestamp) / 1000.0)\n");
        sql.append("  END AS rate_per_sec,\n");
        sql.append("  labels\n");
        sql.append("FROM rates\n");
        sql.append("WHERE prev_value IS NOT NULL\n");  // Skip first point (no previous value)
        sql.append("ORDER BY timestamp_ms, labels");

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
     * Standalone main method for direct execution of rate queries.
     * Delegates to RateCLI for picocli argument parsing.
     *
     * Usage: java -cp ... io.nosqlbench.nb.mql.commands.RateCommand --metric ops_total --window 5m
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new RateCLI()).execute(args);
        System.exit(exitCode);
    }
}
