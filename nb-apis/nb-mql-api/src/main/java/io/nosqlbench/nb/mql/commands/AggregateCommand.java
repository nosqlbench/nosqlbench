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

import io.nosqlbench.nb.mql.cli.AggregateCLI;
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
import java.util.*;

/**
 * Aggregate command - Perform aggregations (sum, avg, max, min, count) on metrics.
 *
 * Usage: agg --metric <name> --func <sum|avg|max|min|count> [--by <label,...>] [--window <duration>] [--labels key=val,...]
 *
 * Aggregates metric values using SQL aggregate functions, optionally grouping by label dimensions.
 * This is the PromQL aggregation operator (sum by, avg by, etc.).
 */
public class AggregateCommand implements MetricsQueryCommand {

    private static final Set<String> VALID_FUNCTIONS = Set.of("sum", "avg", "max", "min", "count");

    @Override
    public String getName() {
        return "agg";
    }

    @Override
    public String getDescription() {
        return "Perform aggregations (sum, avg, max, min, count) on metrics";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String metric = (String) params.get("metric");
        String function = ((String) params.get("function")).toLowerCase();
        @SuppressWarnings("unchecked")
        List<String> groupByLabels = (List<String>) params.getOrDefault("by", List.of());
        @SuppressWarnings("unchecked")
        Map<String, String> labels = (Map<String, String>) params.getOrDefault("labels", Map.of());

        long startTime = System.currentTimeMillis();

        // Determine time range (optional - defaults to latest snapshot)
        Long startTimestampMs = null;
        Long endTimestampMs = null;
        boolean useLatestOnly = !params.containsKey("window") && !params.containsKey("last");

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
        String sql = buildQuery(metric, function, groupByLabels, labels, startTimestampMs, endTimestampMs, useLatestOnly);

        // Build columns dynamically based on grouping
        List<String> columns = new ArrayList<>();
        columns.addAll(groupByLabels);
        columns.add(function);

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, metric);

            if (!useLatestOnly && startTimestampMs != null) {
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

                    // Add grouped label values
                    for (String labelName : groupByLabels) {
                        row.put(labelName, rs.getString(labelName));
                    }

                    // Add aggregate result
                    row.put(function, rs.getDouble(function));

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

        if (!params.containsKey("function")) {
            throw new InvalidQueryException("Missing required parameter: function (sum, avg, max, min, count)");
        }

        Object metric = params.get("metric");
        if (!(metric instanceof String) || ((String) metric).trim().isEmpty()) {
            throw new InvalidQueryException("Parameter 'metric' must be a non-empty string");
        }

        Object function = params.get("function");
        if (!(function instanceof String)) {
            throw new InvalidQueryException("Parameter 'function' must be a string");
        }

        String func = ((String) function).toLowerCase();
        if (!VALID_FUNCTIONS.contains(func)) {
            throw new InvalidQueryException(
                "Invalid function: " + function + "\n" +
                "Valid functions: " + String.join(", ", VALID_FUNCTIONS)
            );
        }

        // Validate 'by' parameter if present
        if (params.containsKey("by")) {
            Object by = params.get("by");
            if (!(by instanceof List)) {
                throw new InvalidQueryException("Parameter 'by' must be a List<String>");
            }
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
              # Sum all requests
              agg --metric api_requests_total --func sum

              # Average by endpoint
              agg --metric api_requests_total --func avg --by endpoint

              # Max latency by operation
              agg --metric operation_latency --func max --by operation

              # Count by status code and method
              agg --metric api_requests_total --func count --by status,method

              # Sum over last 5 minutes
              agg --metric requests_total --func sum --window 5m
            """;
    }

    private String buildQuery(String metric, String function, List<String> groupByLabels,
                             Map<String, String> labelFilters, Long startTs, Long endTs, boolean useLatestOnly) {
        StringBuilder sql = new StringBuilder();

        sql.append("WITH labeled_values AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE);

        // Add individual label columns for grouping
        if (!groupByLabels.isEmpty()) {
            sql.append(",\n");
            for (int i = 0; i < groupByLabels.size(); i++) {
                sql.append("    lk").append(i).append(".").append(MetricsSchema.COL_LK_NAME).append(" AS label_key_").append(i).append(",\n");
                sql.append("    lv").append(i).append(".").append(MetricsSchema.COL_LV_VALUE).append(" AS label_value_").append(i);
                if (i < groupByLabels.size() - 1) {
                    sql.append(",\n");
                }
            }
        }
        sql.append("\n");

        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv\n");
        sql.append("  JOIN ").append(MetricsSchema.TABLE_SAMPLE_NAME).append(" sn ON sn.")
           .append(MetricsSchema.COL_SN_ID).append(" = sv.").append(MetricsSchema.COL_SV_SAMPLE_NAME_ID).append("\n");

        if (useLatestOnly) {
            sql.append("  CROSS JOIN (SELECT MAX(").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(") AS max_ts FROM ")
               .append(MetricsSchema.TABLE_SAMPLE_VALUE).append(") latest\n");
        }

        // Join with label tables for grouping
        for (int i = 0; i < groupByLabels.size(); i++) {
            String labelName = groupByLabels.get(i);
            sql.append("  JOIN ").append(MetricsSchema.TABLE_LABEL_SET_MEMBERSHIP).append(" lsm").append(i)
               .append(" ON lsm").append(i).append(".").append(MetricsSchema.COL_LSM_LABEL_SET_ID)
               .append(" = sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append("\n");
            sql.append("  JOIN ").append(MetricsSchema.TABLE_LABEL_KEY).append(" lk").append(i)
               .append(" ON lk").append(i).append(".").append(MetricsSchema.COL_LK_ID)
               .append(" = lsm").append(i).append(".").append(MetricsSchema.COL_LSM_LABEL_KEY_ID)
               .append(" AND lk").append(i).append(".").append(MetricsSchema.COL_LK_NAME).append(" = '").append(labelName).append("'\n");
            sql.append("  JOIN ").append(MetricsSchema.TABLE_LABEL_VALUE).append(" lv").append(i)
               .append(" ON lv").append(i).append(".").append(MetricsSchema.COL_LV_ID)
               .append(" = lsm").append(i).append(".").append(MetricsSchema.COL_LSM_LABEL_VALUE_ID).append("\n");
        }

        sql.append("  WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");

        if (useLatestOnly) {
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" = latest.max_ts\n");
        } else if (startTs != null) {
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" >= ?\n");
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" <= ?\n");
        }

        // Add label filters
        int filterIdx = 0;
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
            filterIdx++;
        }

        sql.append(")\n");

        // Aggregation query
        sql.append("SELECT\n");
        for (int i = 0; i < groupByLabels.size(); i++) {
            sql.append("  label_value_").append(i).append(" AS ").append(groupByLabels.get(i)).append(",\n");
        }
        sql.append("  ").append(function.toUpperCase()).append("(value) AS ").append(function).append("\n");
        sql.append("FROM labeled_values\n");

        if (!groupByLabels.isEmpty()) {
            sql.append("GROUP BY ");
            for (int i = 0; i < groupByLabels.size(); i++) {
                sql.append("label_value_").append(i);
                if (i < groupByLabels.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append("\n");
        }

        sql.append("ORDER BY ").append(function);
        if (function.equals("count") || function.equals("sum")) {
            sql.append(" DESC");  // Descending for counts and sums
        }

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
     * Standalone main method for direct execution.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new AggregateCLI()).execute(args);
        System.exit(exitCode);
    }
}
