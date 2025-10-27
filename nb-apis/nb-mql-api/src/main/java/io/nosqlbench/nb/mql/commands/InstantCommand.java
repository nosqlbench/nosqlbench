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

import io.nosqlbench.nb.mql.cli.InstantCLI;
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
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
 * Instant query command - retrieves the most recent value for metrics.
 *
 * Usage: instant --metric <metric_name> [--labels key=val,...]
 *
 * Returns the latest snapshot value for each unique label set.
 */
public class InstantCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "instant";
    }

    @Override
    public String getDescription() {
        return "Get the most recent value for a metric";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String metric = (String) params.get("metric");
        @SuppressWarnings("unchecked")
        Map<String, String> labels = (Map<String, String>) params.getOrDefault("labels", Map.of());

        long startTime = System.currentTimeMillis();

        // Build the query
        String sql = buildQuery(labels);

        List<String> columns = new ArrayList<>();
        columns.add("timestamp");
        columns.add("value");
        columns.add("labels");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, metric);

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
              # Get latest value for all label sets
              instant --metric activity_ops_total

              # Get latest value for specific labels
              instant --metric activity_ops_total --labels activity=read,host=server1

              # Get latest latency
              instant --metric operation_latency_ms
            """;
    }

    private String buildQuery(Map<String, String> labelFilters) {
        StringBuilder sql = new StringBuilder();

        sql.append("WITH latest_snapshot AS (\n");
        sql.append("  SELECT MAX(").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(") AS max_ts\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append("\n");
        sql.append("),\n");

        sql.append("labeled_samples AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE).append(",\n");
        sql.append("    GROUP_CONCAT(lk.").append(MetricsSchema.COL_LK_NAME).append(" || '=' || lv.").append(MetricsSchema.COL_LV_VALUE).append(", ', ') AS labels,\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append("\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv\n");
        sql.append("  JOIN ").append(MetricsSchema.TABLE_SAMPLE_NAME).append(" sn ON sn.").append(MetricsSchema.COL_SN_ID).append(" = sv.").append(MetricsSchema.COL_SV_SAMPLE_NAME_ID).append("\n");
        sql.append("  ").append(MetricsSchema.joinAllLabels()).append("\n");
        sql.append("  CROSS JOIN latest_snapshot\n");
        sql.append("  WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");
        sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" = latest_snapshot.max_ts\n");

        // Add label filters
        for (String labelKey : labelFilters.keySet()) {
            sql.append("    AND sv.").append(MetricsSchema.COL_SV_LABEL_SET_ID).append(" IN (\n");
            sql.append("      SELECT lsm.").append(MetricsSchema.COL_LSM_LABEL_SET_ID).append("\n");
            sql.append("      FROM ").append(MetricsSchema.TABLE_LABEL_SET_MEMBERSHIP).append(" lsm\n");
            sql.append("      JOIN ").append(MetricsSchema.TABLE_LABEL_KEY).append(" lk ON lk.").append(MetricsSchema.COL_LK_ID).append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_KEY_ID).append("\n");
            sql.append("      JOIN ").append(MetricsSchema.TABLE_LABEL_VALUE).append(" lv ON lv.").append(MetricsSchema.COL_LV_ID).append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_VALUE_ID).append("\n");
            sql.append("      WHERE lk.").append(MetricsSchema.COL_LK_NAME).append(" = ? AND lv.").append(MetricsSchema.COL_LV_VALUE).append(" = ?\n");
            sql.append("    )\n");
        }

        sql.append("  GROUP BY sv.").append(MetricsSchema.COL_SV_ID).append("\n");
        sql.append(")\n");

        sql.append("SELECT\n");
        sql.append("  datetime(timestamp_ms / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  value,\n");
        sql.append("  labels\n");
        sql.append("FROM labeled_samples\n");
        sql.append("ORDER BY labels");

        return sql.toString();
    }

    /**
     * Standalone main method for direct execution of instant queries.
     * Delegates to InstantCLI for picocli argument parsing.
     *
     * Usage: java -cp ... io.nosqlbench.nb.mql.commands.InstantCommand --metric ops_total
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new InstantCLI()).execute(args);
        System.exit(exitCode);
    }
}
