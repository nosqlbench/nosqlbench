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

package io.nosqlbench.nb.mql.parser;

import io.nosqlbench.nb.mql.query.TimeWindowParser;
import io.nosqlbench.nb.mql.schema.MetricsSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms MetricsQL selectors into SQL queries.
 *
 * <p>Handles:</p>
 * <ul>
 *   <li>Metric name filtering</li>
 *   <li>Label matching (exact, not equal)</li>
 *   <li>Time range filtering (coming in next phase)</li>
 * </ul>
 *
 * <p>Phase 2 Implementation: Basic selectors with exact label matching</p>
 */
public class SelectorTransformer {
    private static final Logger logger = LoggerFactory.getLogger(SelectorTransformer.class);

    /**
     * Transforms a metric selector to SQL that queries the latest values
     *
     * @param metricName The metric name
     * @param labelMatchers Label filtering conditions
     * @param timeRange Optional time range (e.g., "5m")
     * @return SQL fragment with parameterized query
     */
    public SQLFragment transformSelector(String metricName,
                                         List<LabelMatcher> labelMatchers,
                                         String timeRange) {
        logger.debug("Transforming selector: metric={}, labels={}, timeRange={}",
            metricName, labelMatchers, timeRange);

        // Validate metric name
        validateMetricName(metricName);

        // Build the SQL using CTEs for clarity
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        // Start with the latest snapshot CTE
        sql.append("WITH latest_snapshot AS (\n");
        sql.append("  SELECT MAX(").append(MetricsSchema.COL_SV_TIMESTAMP_MS)
           .append(") AS max_ts\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append("\n");
        sql.append("),\n");

        // Main query CTE
        sql.append("labeled_samples AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE).append(",\n");
        sql.append("    sn.").append(MetricsSchema.COL_SN_SAMPLE).append(",\n");
        sql.append("    mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append(",\n");
        sql.append("    GROUP_CONCAT(lk.").append(MetricsSchema.COL_LK_NAME)
           .append(" || '=' || lv.").append(MetricsSchema.COL_LV_VALUE)
           .append(", ', ') AS labels\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv\n");

        // Join with metric_instance and sample_name
        sql.append("  JOIN ").append(MetricsSchema.TABLE_METRIC_INSTANCE).append(" mi ON\n");
        sql.append("    mi.").append(MetricsSchema.COL_MI_ID)
           .append(" = sv.").append(MetricsSchema.COL_SV_METRIC_INSTANCE_ID).append("\n");
        sql.append("  JOIN ").append(MetricsSchema.TABLE_SAMPLE_NAME).append(" sn ON\n");
        sql.append("    sn.").append(MetricsSchema.COL_SN_ID)
           .append(" = mi.").append(MetricsSchema.COL_MI_SAMPLE_NAME_ID).append("\n");

        // Join with label set for label concatenation
        sql.append("  JOIN ").append(MetricsSchema.TABLE_LABEL_SET).append(" ls ON\n");
        sql.append("    ls.").append(MetricsSchema.COL_LS_ID)
           .append(" = mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append("\n");
        sql.append("  LEFT JOIN ").append(MetricsSchema.TABLE_LABEL_SET_MEMBERSHIP).append(" lsm ON\n");
        sql.append("    lsm.").append(MetricsSchema.COL_LSM_LABEL_SET_ID)
           .append(" = ls.").append(MetricsSchema.COL_LS_ID).append("\n");
        sql.append("  LEFT JOIN ").append(MetricsSchema.TABLE_LABEL_KEY).append(" lk ON\n");
        sql.append("    lk.").append(MetricsSchema.COL_LK_ID)
           .append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_KEY_ID).append("\n");
        sql.append("  LEFT JOIN ").append(MetricsSchema.TABLE_LABEL_VALUE).append(" lv ON\n");
        sql.append("    lv.").append(MetricsSchema.COL_LV_ID)
           .append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_VALUE_ID).append("\n");

        // Cross join with latest snapshot
        sql.append("  CROSS JOIN latest_snapshot\n");

        // WHERE clause
        sql.append("  WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");
        parameters.add(metricName);

        sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS)
           .append(" = latest_snapshot.max_ts\n");

        // Add label filters
        for (LabelMatcher matcher : labelMatchers) {
            sql.append("    AND ");
            SQLFragment matcherSQL = matcher.toSQL();
            sql.append(matcherSQL.getSql()).append("\n");
            parameters.addAll(matcherSQL.getParameters());
        }

        // Group by to get label concatenation
        sql.append("  GROUP BY sv.").append(MetricsSchema.COL_SV_ID).append("\n");
        sql.append(")\n");

        // Final SELECT
        sql.append("SELECT\n");
        sql.append("  datetime(timestamp_ms / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  value,\n");
        sql.append("  labels\n");
        sql.append("FROM labeled_samples\n");
        sql.append("ORDER BY labels");

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Validates metric name to prevent SQL injection.
     * Metric names can contain letters, numbers, underscores, and colons.
     */
    private void validateMetricName(String metricName) {
        if (metricName == null || metricName.trim().isEmpty()) {
            throw new IllegalArgumentException("Metric name cannot be null or empty");
        }

        // Metric names follow Prometheus naming conventions
        // Can contain: letters, digits, underscores, colons
        if (!metricName.matches("^[a-zA-Z_:][a-zA-Z0-9_:]*$")) {
            throw new IllegalArgumentException(
                "Invalid metric name: '" + metricName + "'. " +
                "Metric names must start with letter/underscore/colon and contain only " +
                "alphanumeric characters, underscores, or colons."
            );
        }
    }

    /**
     * Transforms a selector with time range support.
     * Phase 3: Implements time window filtering for rollup functions.
     *
     * @param metricName The metric name
     * @param labelMatchers Label filtering conditions
     * @param timeRange Time range duration (e.g., "5m", "1h")
     * @param maxTimestampMs Maximum timestamp (end of range), if null uses latest snapshot
     * @return SQL fragment with time-windowed query
     */
    public SQLFragment transformSelectorWithTimeRange(String metricName,
                                                      List<LabelMatcher> labelMatchers,
                                                      String timeRange,
                                                      Long maxTimestampMs) {
        logger.debug("Transforming selector with time range: metric={}, labels={}, timeRange={}, maxTs={}",
            metricName, labelMatchers, timeRange, maxTimestampMs);

        // Validate metric name
        validateMetricName(metricName);

        // Parse time range duration to milliseconds
        Long durationMs = null;
        if (timeRange != null) {
            try {
                durationMs = TimeWindowParser.parseToMillis(timeRange);
                logger.debug("Parsed duration {} to {}ms", timeRange, durationMs);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Invalid time range duration: '" + timeRange + "'. " +
                    "Expected format: number followed by unit (s, m, h, d, w). " +
                    "Examples: 5m, 1h, 30s", e);
            }
        }

        // Build the SQL using CTEs
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        // If no time range specified, delegate to basic selector (latest snapshot only)
        if (durationMs == null) {
            return transformSelector(metricName, labelMatchers, null);
        }

        // CTE 1: Determine the time window bounds
        sql.append("WITH time_window AS (\n");
        if (maxTimestampMs != null) {
            // Use provided max timestamp
            sql.append("  SELECT ? AS max_ts, ? AS min_ts\n");
            parameters.add(maxTimestampMs);
            parameters.add(maxTimestampMs - durationMs);
        } else {
            // Use latest snapshot as max, subtract duration for min
            sql.append("  SELECT\n");
            sql.append("    MAX(").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(") AS max_ts,\n");
            sql.append("    MAX(").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(") - ? AS min_ts\n");
            sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append("\n");
            parameters.add(durationMs);
        }
        sql.append("),\n");

        // CTE 2: Windowed samples with labels
        sql.append("windowed_samples AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE).append(",\n");
        sql.append("    sn.").append(MetricsSchema.COL_SN_SAMPLE).append(",\n");
        sql.append("    mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append(",\n");
        sql.append("    GROUP_CONCAT(lk.").append(MetricsSchema.COL_LK_NAME)
           .append(" || '=' || lv.").append(MetricsSchema.COL_LV_VALUE)
           .append(", ', ') AS labels\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append(" sv\n");

        // Join with metric_instance and sample_name
        sql.append("  JOIN ").append(MetricsSchema.TABLE_METRIC_INSTANCE).append(" mi ON\n");
        sql.append("    mi.").append(MetricsSchema.COL_MI_ID)
           .append(" = sv.").append(MetricsSchema.COL_SV_METRIC_INSTANCE_ID).append("\n");
        sql.append("  JOIN ").append(MetricsSchema.TABLE_SAMPLE_NAME).append(" sn ON\n");
        sql.append("    sn.").append(MetricsSchema.COL_SN_ID)
           .append(" = mi.").append(MetricsSchema.COL_MI_SAMPLE_NAME_ID).append("\n");

        // Join with label set for label concatenation
        sql.append("  JOIN ").append(MetricsSchema.TABLE_LABEL_SET).append(" ls ON\n");
        sql.append("    ls.").append(MetricsSchema.COL_LS_ID)
           .append(" = mi.").append(MetricsSchema.COL_MI_LABEL_SET_ID).append("\n");
        sql.append("  LEFT JOIN ").append(MetricsSchema.TABLE_LABEL_SET_MEMBERSHIP).append(" lsm ON\n");
        sql.append("    lsm.").append(MetricsSchema.COL_LSM_LABEL_SET_ID)
           .append(" = ls.").append(MetricsSchema.COL_LS_ID).append("\n");
        sql.append("  LEFT JOIN ").append(MetricsSchema.TABLE_LABEL_KEY).append(" lk ON\n");
        sql.append("    lk.").append(MetricsSchema.COL_LK_ID)
           .append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_KEY_ID).append("\n");
        sql.append("  LEFT JOIN ").append(MetricsSchema.TABLE_LABEL_VALUE).append(" lv ON\n");
        sql.append("    lv.").append(MetricsSchema.COL_LV_ID)
           .append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_VALUE_ID).append("\n");

        // Cross join with time window
        sql.append("  CROSS JOIN time_window\n");

        // WHERE clause: metric name and time range
        // Use pre_window_ts (sample before window) if available for better rate accuracy
        sql.append("  WHERE sn.").append(MetricsSchema.COL_SN_SAMPLE).append(" = ?\n");
        parameters.add(metricName);

        // Temporarily disable sample-before-window to debug
        sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS)
           .append(" >= time_window.min_ts\n");
        sql.append("    AND sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS)
           .append(" <= time_window.max_ts\n");

        // Add label filters
        for (LabelMatcher matcher : labelMatchers) {
            sql.append("    AND ");
            SQLFragment matcherSQL = matcher.toSQL();
            sql.append(matcherSQL.getSql()).append("\n");
            parameters.addAll(matcherSQL.getParameters());
        }

        // Group by to get label concatenation
        sql.append("  GROUP BY sv.").append(MetricsSchema.COL_SV_ID).append("\n");
        sql.append(")\n");

        // Final SELECT
        sql.append("SELECT\n");
        sql.append("  datetime(windowed_samples.timestamp_ms / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  windowed_samples.value,\n");
        sql.append("  windowed_samples.labels\n");
        sql.append("FROM windowed_samples\n");
        sql.append("ORDER BY windowed_samples.timestamp_ms, windowed_samples.labels");

        return new SQLFragment(sql.toString(), parameters);
    }
}
