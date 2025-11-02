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

import io.nosqlbench.nb.mql.schema.MetricsSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms MetricsQL rollup functions to SQL with window functions.
 *
 * <p>Handles rollup functions that aggregate over time windows:</p>
 * <ul>
 *   <li>rate() - Per-second rate of change (for counters)</li>
 *   <li>increase() - Total increase over time window (for counters)</li>
 *   <li>avg_over_time() - Average value over time window</li>
 *   <li>sum_over_time() - Sum of values over time window</li>
 *   <li>min_over_time() - Minimum value over time window</li>
 *   <li>max_over_time() - Maximum value over time window</li>
 *   <li>count_over_time() - Count of samples over time window</li>
 * </ul>
 *
 * <p>Phase 3 Implementation: Core rollup functions with window function SQL</p>
 */
public class RollupTransformer {
    private static final Logger logger = LoggerFactory.getLogger(RollupTransformer.class);

    /**
     * Supported rollup function types.
     */
    public enum RollupType {
        RATE,              // Per-second rate calculation
        INCREASE,          // Total increase
        AVG_OVER_TIME,     // Average over time
        SUM_OVER_TIME,     // Sum over time
        MIN_OVER_TIME,     // Minimum over time
        MAX_OVER_TIME,     // Maximum over time
        COUNT_OVER_TIME,   // Count of samples
        QUANTILE_OVER_TIME // Quantile (percentile) over time
    }

    private final SelectorTransformer selectorTransformer = new SelectorTransformer();

    /**
     * Transforms a rollup function to SQL.
     *
     * @param rollupType The type of rollup function
     * @param metricName The metric name
     * @param labelMatchers Label filtering conditions
     * @param timeRange Time window (e.g., "5m")
     * @param maxTimestampMs Maximum timestamp (end of window), if null uses latest
     * @return SQL fragment with rollup calculation
     */
    public SQLFragment transformRollup(RollupType rollupType,
                                       String metricName,
                                       List<LabelMatcher> labelMatchers,
                                       String timeRange,
                                       Long maxTimestampMs) {
        return transformRollup(rollupType, metricName, labelMatchers, timeRange, maxTimestampMs, null);
    }

    /**
     * Transforms a rollup function to SQL with optional quantile parameter.
     *
     * @param rollupType The type of rollup function
     * @param metricName The metric name
     * @param labelMatchers Label filtering conditions
     * @param timeRange Time window (e.g., "5m")
     * @param maxTimestampMs Maximum timestamp (end of window), if null uses latest
     * @param quantile Quantile value (0.0-1.0) for QUANTILE_OVER_TIME, ignored for others
     * @return SQL fragment with rollup calculation
     */
    public SQLFragment transformRollup(RollupType rollupType,
                                       String metricName,
                                       List<LabelMatcher> labelMatchers,
                                       String timeRange,
                                       Long maxTimestampMs,
                                       Double quantile) {
        logger.debug("Transforming rollup: type={}, metric={}, timeRange={}, quantile={}",
            rollupType, metricName, timeRange, quantile);

        if (timeRange == null || timeRange.isEmpty()) {
            throw new IllegalArgumentException(
                "Rollup function " + rollupType + " requires a time range (e.g., [5m])");
        }

        return switch (rollupType) {
            case RATE -> transformRate(metricName, labelMatchers, timeRange, maxTimestampMs);
            case INCREASE -> transformIncrease(metricName, labelMatchers, timeRange, maxTimestampMs);
            case AVG_OVER_TIME -> transformAggregateOverTime("AVG", metricName, labelMatchers, timeRange, maxTimestampMs);
            case SUM_OVER_TIME -> transformAggregateOverTime("SUM", metricName, labelMatchers, timeRange, maxTimestampMs);
            case MIN_OVER_TIME -> transformAggregateOverTime("MIN", metricName, labelMatchers, timeRange, maxTimestampMs);
            case MAX_OVER_TIME -> transformAggregateOverTime("MAX", metricName, labelMatchers, timeRange, maxTimestampMs);
            case COUNT_OVER_TIME -> transformCountOverTime(metricName, labelMatchers, timeRange, maxTimestampMs);
            case QUANTILE_OVER_TIME -> transformQuantileOverTime(metricName, labelMatchers, timeRange, maxTimestampMs, quantile);
        };
    }

    /**
     * Transforms rate() function to SQL.
     * Calculates per-second rate of change using LAG window function.
     */
    private SQLFragment transformRate(String metricName,
                                      List<LabelMatcher> labelMatchers,
                                      String timeRange,
                                      Long maxTimestampMs) {
        // First get windowed samples using SelectorTransformer
        SQLFragment windowedSamples = selectorTransformer.transformSelectorWithTimeRange(
            metricName, labelMatchers, timeRange, maxTimestampMs);

        // Build rate calculation on top of windowed samples
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>(windowedSamples.getParameters());

        sql.append("WITH windowed_data AS (\n");
        sql.append("  ").append(windowedSamples.getSql().replace("\n", "\n  ")).append("\n");
        sql.append("),\n");

        // Add window functions to calculate rate
        sql.append("rate_calc AS (\n");
        sql.append("  SELECT\n");
        sql.append("    timestamp,\n");
        sql.append("    value,\n");
        sql.append("    labels,\n");
        sql.append("    LAG(value) OVER (PARTITION BY labels ORDER BY timestamp) AS prev_value,\n");
        sql.append("    LAG(timestamp) OVER (PARTITION BY labels ORDER BY timestamp) AS prev_timestamp\n");
        sql.append("  FROM windowed_data\n");
        sql.append(")\n");

        // Calculate rate per second with counter reset detection
        sql.append("SELECT\n");
        sql.append("  timestamp,\n");
        sql.append("  CASE\n");
        sql.append("    WHEN prev_value IS NULL THEN 0.0\n");
        sql.append("    WHEN (julianday(timestamp) - julianday(prev_timestamp)) = 0 THEN 0.0\n");
        sql.append("    WHEN value < prev_value THEN\n");
        sql.append("      -- Counter reset detected! Use current value (assume reset from 0)\n");
        sql.append("      value / ((julianday(timestamp) - julianday(prev_timestamp)) * 86400.0)\n");
        sql.append("    ELSE (value - prev_value) / ((julianday(timestamp) - julianday(prev_timestamp)) * 86400.0)\n");
        sql.append("  END AS value,\n");
        sql.append("  labels\n");
        sql.append("FROM rate_calc\n");
        sql.append("WHERE prev_value IS NOT NULL\n");
        sql.append("ORDER BY timestamp, labels");

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Transforms increase() function to SQL.
     * Calculates total increase over the time window with counter reset detection.
     */
    private SQLFragment transformIncrease(String metricName,
                                          List<LabelMatcher> labelMatchers,
                                          String timeRange,
                                          Long maxTimestampMs) {
        // First get windowed samples
        SQLFragment windowedSamples = selectorTransformer.transformSelectorWithTimeRange(
            metricName, labelMatchers, timeRange, maxTimestampMs);

        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>(windowedSamples.getParameters());

        sql.append("WITH windowed_data AS (\n");
        sql.append("  ").append(windowedSamples.getSql().replace("\n", "\n  ")).append("\n");
        sql.append("),\n");

        // Add window functions to calculate increase with reset detection
        sql.append("increase_calc AS (\n");
        sql.append("  SELECT\n");
        sql.append("    timestamp,\n");
        sql.append("    value,\n");
        sql.append("    labels,\n");
        sql.append("    LAG(value) OVER (PARTITION BY labels ORDER BY timestamp) AS prev_value\n");
        sql.append("  FROM windowed_data\n");
        sql.append("),\n");

        // Calculate per-sample increase with reset detection
        sql.append("sample_increases AS (\n");
        sql.append("  SELECT\n");
        sql.append("    timestamp,\n");
        sql.append("    labels,\n");
        sql.append("    CASE\n");
        sql.append("      WHEN prev_value IS NULL THEN 0.0\n");
        sql.append("      WHEN value < prev_value THEN\n");
        sql.append("        -- Counter reset detected! Use current value (assume reset from 0)\n");
        sql.append("        value\n");
        sql.append("      ELSE value - prev_value\n");
        sql.append("    END AS increase\n");
        sql.append("  FROM increase_calc\n");
        sql.append(")\n");

        // Sum all increases per label set
        sql.append("SELECT\n");
        sql.append("  MAX(timestamp) AS timestamp,\n");
        sql.append("  SUM(increase) AS value,\n");
        sql.append("  labels\n");
        sql.append("FROM sample_increases\n");
        sql.append("GROUP BY labels\n");
        sql.append("ORDER BY labels");

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Transforms aggregate_over_time functions (avg, sum, min, max) to SQL.
     */
    private SQLFragment transformAggregateOverTime(String aggregateFunc,
                                                   String metricName,
                                                   List<LabelMatcher> labelMatchers,
                                                   String timeRange,
                                                   Long maxTimestampMs) {
        // First get windowed samples
        SQLFragment windowedSamples = selectorTransformer.transformSelectorWithTimeRange(
            metricName, labelMatchers, timeRange, maxTimestampMs);

        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>(windowedSamples.getParameters());

        sql.append("WITH windowed_data AS (\n");
        sql.append("  ").append(windowedSamples.getSql().replace("\n", "\n  ")).append("\n");
        sql.append(")\n");

        // Apply aggregate function
        sql.append("SELECT\n");
        sql.append("  MAX(timestamp) AS timestamp,\n");
        sql.append("  ").append(aggregateFunc).append("(value) AS value,\n");
        sql.append("  labels\n");
        sql.append("FROM windowed_data\n");
        sql.append("GROUP BY labels\n");
        sql.append("ORDER BY labels");

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Transforms count_over_time() function to SQL.
     */
    private SQLFragment transformCountOverTime(String metricName,
                                               List<LabelMatcher> labelMatchers,
                                               String timeRange,
                                               Long maxTimestampMs) {
        // First get windowed samples
        SQLFragment windowedSamples = selectorTransformer.transformSelectorWithTimeRange(
            metricName, labelMatchers, timeRange, maxTimestampMs);

        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>(windowedSamples.getParameters());

        sql.append("WITH windowed_data AS (\n");
        sql.append("  ").append(windowedSamples.getSql().replace("\n", "\n  ")).append("\n");
        sql.append(")\n");

        // Count samples
        sql.append("SELECT\n");
        sql.append("  MAX(timestamp) AS timestamp,\n");
        sql.append("  COUNT(*) AS value,\n");
        sql.append("  labels\n");
        sql.append("FROM windowed_data\n");
        sql.append("GROUP BY labels\n");
        sql.append("ORDER BY labels");

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Transforms quantile_over_time() function to SQL.
     * Calculates the specified quantile (percentile) over the time window.
     *
     * @param quantile The quantile value (0.0 to 1.0), e.g., 0.95 for 95th percentile
     */
    private SQLFragment transformQuantileOverTime(String metricName,
                                                  List<LabelMatcher> labelMatchers,
                                                  String timeRange,
                                                  Long maxTimestampMs,
                                                  Double quantile) {
        if (quantile == null || quantile < 0.0 || quantile > 1.0) {
            throw new IllegalArgumentException(
                "quantile_over_time requires a quantile value between 0.0 and 1.0. " +
                "Example: quantile_over_time(0.95, metric[5m])");
        }

        // First get windowed samples
        SQLFragment windowedSamples = selectorTransformer.transformSelectorWithTimeRange(
            metricName, labelMatchers, timeRange, maxTimestampMs);

        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>(windowedSamples.getParameters());

        sql.append("WITH windowed_data AS (\n");
        sql.append("  ").append(windowedSamples.getSql().replace("\n", "\n  ")).append("\n");
        sql.append("),\n");

        // Use PERCENT_RANK to calculate quantile
        sql.append("ranked_data AS (\n");
        sql.append("  SELECT\n");
        sql.append("    timestamp,\n");
        sql.append("    value,\n");
        sql.append("    labels,\n");
        sql.append("    PERCENT_RANK() OVER (PARTITION BY labels ORDER BY value) AS percentile\n");
        sql.append("  FROM windowed_data\n");
        sql.append(")\n");

        // Find the value at the specified quantile
        sql.append("SELECT\n");
        sql.append("  MAX(timestamp) AS timestamp,\n");
        sql.append("  MIN(value) AS value,\n");  // MIN of values >= quantile threshold
        sql.append("  labels\n");
        sql.append("FROM ranked_data\n");
        sql.append("WHERE percentile >= ?\n");
        parameters.add(quantile);
        sql.append("GROUP BY labels\n");
        sql.append("ORDER BY labels");

        return new SQLFragment(sql.toString(), parameters);
    }
}
