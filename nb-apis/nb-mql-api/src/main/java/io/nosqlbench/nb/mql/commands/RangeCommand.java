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

import io.nosqlbench.nb.mql.cli.RangeCLI;
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
 * Range query command - retrieves time-series data over a time window.
 *
 * Usage: range --metric <metric_name> --window <duration> [--labels key=val,...]
 *        range --metric <metric_name> --start <timestamp> --end <timestamp> [--labels key=val,...]
 *
 * Returns all data points within the specified time range for each label set.
 */
public class RangeCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "range";
    }

    @Override
    public String getDescription() {
        return "Retrieve time-series data over a time window";
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
            // Use --window to query last N duration from now
            String window = (String) params.get("window");
            long windowMs = TimeWindowParser.parseToMillis(window);
            endTimestampMs = getCurrentMaxTimestamp(conn);
            startTimestampMs = endTimestampMs - windowMs;
        } else if (params.containsKey("start") && params.containsKey("end")) {
            // Use explicit start/end timestamps (epoch millis)
            startTimestampMs = (Long) params.get("start");
            endTimestampMs = (Long) params.get("end");
        } else if (params.containsKey("last")) {
            // Alternative: --last 5m
            String last = (String) params.get("last");
            long windowMs = TimeWindowParser.parseToMillis(last);
            endTimestampMs = getCurrentMaxTimestamp(conn);
            startTimestampMs = endTimestampMs - windowMs;
        }

        // Build the query
        String sql = buildQuery(labels, startTimestampMs != null);

        List<String> columns = new ArrayList<>();
        columns.add("timestamp");
        columns.add("value");
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
                    row.put("value", rs.getDouble("value"));
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

        // Check that either window OR (start AND end) OR last is provided
        boolean hasWindow = params.containsKey("window");
        boolean hasStartEnd = params.containsKey("start") && params.containsKey("end");
        boolean hasLast = params.containsKey("last");

        if (!hasWindow && !hasStartEnd && !hasLast) {
            throw new InvalidQueryException(
                "Must specify time range using one of:\n" +
                "  --window <duration> (e.g., 5m, 1h)\n" +
                "  --last <duration> (e.g., 5m, 1h)\n" +
                "  --start <ms> --end <ms> (epoch milliseconds)"
            );
        }

        if ((hasWindow && hasStartEnd) || (hasWindow && hasLast) || (hasStartEnd && hasLast)) {
            throw new InvalidQueryException(
                "Cannot specify multiple time range methods. Choose one of: --window, --last, or --start/--end"
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
              # Get last 5 minutes of data
              range --metric activity_ops_total --window 5m

              # Alternative syntax
              range --metric activity_ops_total --last 5m

              # Get data between specific timestamps
              range --metric activity_ops_total --start 1729681200000 --end 1729681500000

              # With label filters
              range --metric activity_ops_total --window 10m --labels activity=read
            """;
    }

    private String buildQuery(Map<String, String> labelFilters, boolean hasTimeRange) {
        StringBuilder sql = new StringBuilder();

        sql.append("WITH labeled_samples AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE).append(",\n");
        sql.append("    GROUP_CONCAT(lk.").append(MetricsSchema.COL_LK_NAME)
           .append(" || '=' || lv.").append(MetricsSchema.COL_LV_VALUE).append(", ', ') AS labels,\n");
        sql.append("    mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append("\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv\n");
        sql.append("  JOIN ").append(MetricsSchema.TABLE_SAMPLE_NAME).append(" sn ON sn.")
           .append(MetricsSchema.COL_SN_ID).append(" = mi.").append(MetricsSchema.COL_MI_SAMPLE_NAME_ID).append("\n");
        sql.append("  ").append(MetricsSchema.joinAllLabels()).append("\n");
        sql.append("  WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");

        if (hasTimeRange) {
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" >= ?\n");
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" <= ?\n");
        }

        // Add label filters
        for (String labelKey : labelFilters.keySet()) {
            sql.append("    AND mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append(" IN (\n");
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

        sql.append("SELECT\n");
        sql.append("  datetime(timestamp_ms / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  value,\n");
        sql.append("  labels\n");
        sql.append("FROM labeled_samples\n");
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
     * Standalone main method for direct execution of range queries.
     * Delegates to RangeCLI for picocli argument parsing.
     *
     * Usage: java -cp ... io.nosqlbench.nb.mql.commands.RangeCommand --metric ops_total --window 5m
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new RangeCLI()).execute(args);
        System.exit(exitCode);
    }
}
