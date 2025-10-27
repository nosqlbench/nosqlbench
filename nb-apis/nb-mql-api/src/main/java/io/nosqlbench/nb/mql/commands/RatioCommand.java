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

import io.nosqlbench.nb.mql.cli.RatioCLI;
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
 * Ratio command - Calculate ratio between two metrics (e.g., error rate).
 *
 * Usage: ratio --numerator <metric> --denominator <metric> [--window <duration>] [--labels key=val,...]
 *
 * Divides numerator metric by denominator metric for matched label sets.
 * Useful for calculating error rates, cache hit ratios, etc.
 */
public class RatioCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "ratio";
    }

    @Override
    public String getDescription() {
        return "Calculate ratio between two metrics (numerator / denominator)";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String numerator = (String) params.get("numerator");
        String denominator = (String) params.get("denominator");
        @SuppressWarnings("unchecked")
        Map<String, String> labels = (Map<String, String>) params.getOrDefault("labels", Map.of());

        long startTime = System.currentTimeMillis();

        // Determine time range (optional - defaults to latest)
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

        String sql = buildQuery(numerator, denominator, labels, startTimestampMs, endTimestampMs, useLatestOnly);

        List<String> columns = new ArrayList<>();
        if (!useLatestOnly) {
            columns.add("timestamp");
        }
        columns.add("ratio");
        columns.add("numerator_value");
        columns.add("denominator_value");
        columns.add("labels");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, numerator);
            ps.setString(paramIndex++, denominator);

            if (!useLatestOnly && startTimestampMs != null) {
                ps.setLong(paramIndex++, startTimestampMs);
                ps.setLong(paramIndex++, endTimestampMs);
            }

            // Add label filter parameters (twice - once for each metric)
            for (int i = 0; i < 2; i++) {
                for (Map.Entry<String, String> label : labels.entrySet()) {
                    ps.setString(paramIndex++, label.getKey());
                    ps.setString(paramIndex++, label.getValue());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    if (!useLatestOnly) {
                        row.put("timestamp", rs.getTimestamp("timestamp"));
                    }
                    row.put("ratio", rs.getDouble("ratio"));
                    row.put("numerator_value", rs.getDouble("numerator_value"));
                    row.put("denominator_value", rs.getDouble("denominator_value"));
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
        if (!params.containsKey("numerator")) {
            throw new InvalidQueryException("Missing required parameter: numerator");
        }

        if (!params.containsKey("denominator")) {
            throw new InvalidQueryException("Missing required parameter: denominator");
        }

        Object numerator = params.get("numerator");
        if (!(numerator instanceof String) || ((String) numerator).trim().isEmpty()) {
            throw new InvalidQueryException("Parameter 'numerator' must be a non-empty string");
        }

        Object denominator = params.get("denominator");
        if (!(denominator instanceof String) || ((String) denominator).trim().isEmpty()) {
            throw new InvalidQueryException("Parameter 'denominator' must be a non-empty string");
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
              # Error rate (errors / total requests)
              ratio --numerator errors_total --denominator requests_total

              # Cache hit rate
              ratio --numerator cache_hits --denominator cache_total

              # Success rate over time
              ratio --numerator success_requests --denominator total_requests --window 5m

              # With label filters
              ratio --numerator errors --denominator requests --labels endpoint=/api/users
            """;
    }

    private String buildQuery(String numerator, String denominator, Map<String, String> labelFilters,
                             Long startTs, Long endTs, boolean useLatestOnly) {
        StringBuilder sql = new StringBuilder();

        // Build CTEs for numerator and denominator
        sql.append("WITH num_data AS (\n");
        sql.append(buildMetricCTE(numerator, "num", labelFilters, startTs, endTs, useLatestOnly));
        sql.append("),\n");
        sql.append("den_data AS (\n");
        sql.append(buildMetricCTE(denominator, "den", labelFilters, startTs, endTs, useLatestOnly));
        sql.append(")\n");

        // Join and calculate ratio
        sql.append("SELECT\n");
        if (!useLatestOnly) {
            sql.append("  num_data.timestamp,\n");
        }
        sql.append("  CASE\n");
        sql.append("    WHEN den_data.value = 0 THEN NULL\n");
        sql.append("    ELSE num_data.value / den_data.value\n");
        sql.append("  END AS ratio,\n");
        sql.append("  num_data.value AS numerator_value,\n");
        sql.append("  den_data.value AS denominator_value,\n");
        sql.append("  num_data.labels\n");
        sql.append("FROM num_data\n");
        sql.append("JOIN den_data ON num_data.label_set_id = den_data.label_set_id");

        if (!useLatestOnly) {
            sql.append(" AND num_data.timestamp_ms = den_data.timestamp_ms");
        }

        sql.append("\nORDER BY ");
        if (!useLatestOnly) {
            sql.append("num_data.timestamp_ms, ");
        }
        sql.append("num_data.labels");

        return sql.toString();
    }

    private String buildMetricCTE(String metric, String alias, Map<String, String> labelFilters,
                                 Long startTs, Long endTs, boolean useLatestOnly) {
        StringBuilder sql = new StringBuilder();

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

        if (useLatestOnly) {
            sql.append("  CROSS JOIN (SELECT MAX(").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(") AS max_ts FROM ")
               .append(MetricsSchema.TABLE_SAMPLE_VALUE).append(") latest\n");
        }

        sql.append("  WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");

        if (useLatestOnly) {
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" = latest.max_ts\n");
        } else if (startTs != null) {
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

        sql.append("  GROUP BY sv.").append(MetricsSchema.COL_SV_ID);

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
        int exitCode = new CommandLine(new RatioCLI()).execute(args);
        System.exit(exitCode);
    }
}
