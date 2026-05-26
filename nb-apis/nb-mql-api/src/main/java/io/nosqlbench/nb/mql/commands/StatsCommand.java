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

import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.MetricsQueryCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsSchema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// MQL command exposing the per-sample summary statistics — `min`, `max`, `mean`, `stddev` —
/// persisted by the SQLite snapshot reporter into the `sample_statistics` table.
///
/// Without this command, c-stress-style consumers had no way to retrieve `mean` and `max`
/// latencies from a SQLite session without re-enabling full HDR histogram storage. With it,
/// the SQLite and CSV sources can deliver equivalent reports for those fields.
///
/// **Usage**:
/// ```
/// params = Map.of("metric", "cycles_servicetime", "labels", Map.of("op", "read"));
/// QueryResult r = new StatsCommand().execute(conn, params);
/// // columns: timestamp, min_value, max_value, mean_value, stddev_value, labels
/// ```
///
/// Mirrors [QuantileCommand]'s shape (label filter, optional time window) so that both
/// commands compose naturally for building latency reports.
public class StatsCommand implements MetricsQueryCommand {

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "Extract min/max/mean/stddev statistics from timer/summary metrics";
    }

    @Override
    public QueryResult execute(Connection conn, Map<String, Object> params)
            throws SQLException, InvalidQueryException {

        validate(params);

        String metric = (String) params.get("metric");
        @SuppressWarnings("unchecked")
        Map<String, String> labels = (Map<String, String>) params.getOrDefault("labels", Map.of());

        long startTime = System.currentTimeMillis();

        String sql = buildQuery(labels);

        List<String> columns = new ArrayList<>();
        columns.add("timestamp");
        columns.add("min_value");
        columns.add("max_value");
        columns.add("mean_value");
        columns.add("stddev_value");
        columns.add("labels");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, metric);
            for (Map.Entry<String, String> label : labels.entrySet()) {
                ps.setString(idx++, label.getKey());
                ps.setString(idx++, label.getValue());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("timestamp", rs.getTimestamp("timestamp"));
                    row.put("min_value", rs.getDouble("min_value"));
                    row.put("max_value", rs.getDouble("max_value"));
                    row.put("mean_value", rs.getDouble("mean_value"));
                    row.put("stddev_value", rs.getDouble("stddev_value"));
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
        if (params.containsKey("labels") && !(params.get("labels") instanceof Map)) {
            throw new InvalidQueryException("Parameter 'labels' must be a Map<String, String>");
        }
    }

    @Override
    public String getUsageExamples() {
        return """
            Examples:
              # Get min/max/mean/stddev for all op label combinations of a timer
              stats --metric cycles_servicetime

              # Filter by op
              stats --metric cycles_servicetime --labels op=read
            """;
    }

    /// Build a query that returns the latest per-instance statistics row for the given metric,
    /// joined with the `GROUP_CONCAT`'d labels string (same convention as [InstantCommand] and
    /// [QuantileCommand]).
    private String buildQuery(Map<String, String> labelFilters) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT\n");
        sql.append("  datetime(sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(" / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  ss.").append(MetricsSchema.COL_SS_MIN_VALUE).append(",\n");
        sql.append("  ss.").append(MetricsSchema.COL_SS_MAX_VALUE).append(",\n");
        sql.append("  ss.").append(MetricsSchema.COL_SS_MEAN_VALUE).append(",\n");
        sql.append("  ss.").append(MetricsSchema.COL_SS_STDDEV_VALUE).append(",\n");
        sql.append("  GROUP_CONCAT(lk.").append(MetricsSchema.COL_LK_NAME)
           .append(" || '=' || lv.").append(MetricsSchema.COL_LV_VALUE).append(", ', ') AS labels\n");
        sql.append("FROM ").append(MetricsSchema.TABLE_SAMPLE_STATISTICS).append(" ss\n");
        sql.append("JOIN ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv ON ss.")
           .append(MetricsSchema.COL_SS_SAMPLE_VALUE_ID).append(" = sv.").append(MetricsSchema.COL_SV_ID).append("\n");
        sql.append(MetricsSchema.joinAllLabelsWithSampleName()).append("\n");
        sql.append("WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");
        // Per-metric-instance latest timestamp, not a global MAX — the latter would miss
        // metrics captured at earlier snapshots when a later snapshot wrote different metrics
        // (e.g. activity-close trigger writes everything, then a session-teardown flush
        // writes only system gauges with a newer timestamp).
        sql.append("  AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS)
           .append(" = (SELECT MAX(sv2.").append(MetricsSchema.COL_SV_TIMESTAMP_MS)
           .append(") FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv2")
           .append(" WHERE sv2.metric_instance_id = sv.metric_instance_id)\n");
        // label filters
        for (String labelKey : labelFilters.keySet()) {
            sql.append("  AND mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append(" IN (\n");
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
        sql.append("GROUP BY sv.").append(MetricsSchema.COL_SV_ID).append(", ss.")
           .append(MetricsSchema.COL_SS_SAMPLE_VALUE_ID).append("\n");
        sql.append("ORDER BY labels");
        return sql.toString();
    }
}
