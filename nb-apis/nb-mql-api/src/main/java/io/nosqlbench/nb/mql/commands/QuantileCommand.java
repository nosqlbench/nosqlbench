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

import io.nosqlbench.nb.mql.cli.QuantileCLI;
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
 * Quantile command - Extract percentile values from timer/summary metrics.
 *
 * Usage: quantile --metric <timer_metric> --p <0.0-1.0> [--window <duration>] [--labels key=val,...]
 *
 * Retrieves specific quantile values (e.g., p50, p95, p99) from timer or summary metrics.
 * Useful for latency analysis and SLA monitoring.
 */
public class QuantileCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "quantile";
    }

    @Override
    public String getDescription() {
        return "Extract percentile values from timer/summary metrics";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String metric = (String) params.get("metric");
        Double quantile = (Double) params.get("quantile");
        @SuppressWarnings("unchecked")
        Map<String, String> labels = (Map<String, String>) params.getOrDefault("labels", Map.of());

        long startTime = System.currentTimeMillis();

        // Determine time range (optional for quantile)
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
        String sql = buildQuery(labels, quantile, startTimestampMs != null);

        List<String> columns = new ArrayList<>();
        columns.add("timestamp");
        columns.add("quantile");
        columns.add("quantile_value");
        columns.add("labels");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, metric);
            ps.setDouble(paramIndex++, quantile);

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
                    row.put("quantile", rs.getDouble("quantile"));
                    row.put("quantile_value", rs.getDouble("quantile_value"));
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

        if (!params.containsKey("quantile")) {
            throw new InvalidQueryException("Missing required parameter: quantile (e.g., 0.95 for p95)");
        }

        Object quantile = params.get("quantile");
        if (!(quantile instanceof Double)) {
            throw new InvalidQueryException("Parameter 'quantile' must be a number between 0.0 and 1.0");
        }

        double q = (Double) quantile;
        if (q < 0.0 || q > 1.0) {
            throw new InvalidQueryException("Parameter 'quantile' must be between 0.0 and 1.0, got: " + q);
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
              # Get p95 latency for all operations
              quantile --metric operation_latency_ms --p 0.95

              # Get p99 for specific operation
              quantile --metric api_latency_ms --p 0.99 --labels operation=select

              # p50 (median) over last 5 minutes
              quantile --metric latency_ms --p 0.50 --window 5m

              # Common percentiles
              --p 0.50  # p50 (median)
              --p 0.95  # p95
              --p 0.99  # p99
              --p 0.999 # p999
            """;
    }

    private String buildQuery(Map<String, String> labelFilters, double quantile, boolean hasTimeRange) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT\n");
        sql.append("  datetime(sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  sq.").append(MetricsSchema.COL_SQ_QUANTILE).append(",\n");
        sql.append("  sq.").append(MetricsSchema.COL_SQ_QUANTILE_VALUE).append(",\n");
        sql.append("  GROUP_CONCAT(lk.").append(MetricsSchema.COL_LK_NAME)
           .append(" || '=' || lv.").append(MetricsSchema.COL_LV_VALUE).append(", ', ') AS labels\n");
        sql.append("FROM ").append(MetricsSchema.TABLE_SAMPLE_QUANTILE).append(" sq\n");
        sql.append("JOIN ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv ON sq.")
           .append(MetricsSchema.COL_SQ_SAMPLE_VALUE_ID).append(" = sv.").append(MetricsSchema.COL_SV_ID).append("\n");
        sql.append("JOIN ").append(MetricsSchema.TABLE_SAMPLE_NAME).append(" sn ON sv.")
           .append(MetricsSchema.COL_SV_SAMPLE_NAME_ID).append(" = sn.").append(MetricsSchema.COL_SN_ID).append("\n");
        sql.append("").append(MetricsSchema.joinAllLabels()).append("\n");
        sql.append("WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");
        sql.append("  AND sq.").append(MetricsSchema.COL_SQ_QUANTILE).append(" = ?\n");

        if (hasTimeRange) {
            sql.append("  AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" >= ?\n");
            sql.append("  AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" <= ?\n");
        }

        // Add label filters
        for (String labelKey : labelFilters.keySet()) {
            sql.append("  AND sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append(" IN (\n");
            sql.append("    SELECT lsm.").append(MetricsSchema.COL_LSM_LABEL_SET_ID).append("\n");
            sql.append("    FROM ").append(MetricsSchema.TABLE_LABEL_SET_MEMBERSHIP).append(" lsm\n");
            sql.append("    JOIN ").append(MetricsSchema.TABLE_LABEL_KEY).append(" lk ON lk.")
               .append(MetricsSchema.COL_LK_ID).append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_KEY_ID).append("\n");
            sql.append("    JOIN ").append(MetricsSchema.TABLE_LABEL_VALUE).append(" lv ON lv.")
               .append(MetricsSchema.COL_LV_ID).append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_VALUE_ID).append("\n");
            sql.append("    WHERE lk.").append(MetricsSchema.COL_LK_NAME).append(" = ? AND lv.")
               .append(MetricsSchema.COL_LV_VALUE).append(" = ?\n");
            sql.append("  )\n");
        }

        sql.append("GROUP BY sv.").append(MetricsSchema.COL_SV_ID).append(", sq.")
           .append(MetricsSchema.COL_SQ_QUANTILE).append(", sq.").append(MetricsSchema.COL_SQ_QUANTILE_VALUE).append("\n");
        sql.append("ORDER BY sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(", labels");

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
     * Standalone main method for direct execution of quantile queries.
     * Delegates to QuantileCLI for picocli argument parsing.
     *
     * Usage: java -cp ... io.nosqlbench.nb.mql.commands.QuantileCommand --metric latency_ms --p 0.95
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new QuantileCLI()).execute(args);
        System.exit(exitCode);
    }
}
