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
 * Transforms MetricsQL aggregation expressions to SQL with GROUP BY.
 *
 * <p>Handles aggregation functions that group metrics by labels:</p>
 * <ul>
 *   <li>sum() by (label) - Sum values grouped by label</li>
 *   <li>avg() by (label) - Average values grouped by label</li>
 *   <li>min() by (label) - Minimum value grouped by label</li>
 *   <li>max() by (label) - Maximum value grouped by label</li>
 *   <li>count() by (label) - Count samples grouped by label</li>
 *   <li>stddev() by (label) - Standard deviation grouped by label</li>
 * </ul>
 *
 * <p>Also supports 'without' modifier for inverse grouping.</p>
 *
 * <p>Phase 4 Implementation: Aggregation with GROUP BY queries</p>
 */
public class AggregationTransformer {
    private static final Logger logger = LoggerFactory.getLogger(AggregationTransformer.class);

    /**
     * Supported aggregation function types.
     */
    public enum AggregationType {
        SUM,       // Sum of values
        AVG,       // Average of values
        MIN,       // Minimum value
        MAX,       // Maximum value
        COUNT,     // Count of samples
        STDDEV     // Standard deviation
    }

    /**
     * Aggregation modifier type (by or without).
     */
    public enum ModifierType {
        BY,        // Group by specified labels
        WITHOUT,   // Group by all labels except specified ones
        NONE       // No grouping (aggregate all)
    }

    private final SelectorTransformer selectorTransformer = new SelectorTransformer();

    /**
     * Transforms an aggregation expression to SQL.
     *
     * @param aggType The aggregation function type
     * @param metricName The metric name to aggregate
     * @param labelMatchers Label filtering conditions
     * @param modifierType The modifier type (BY, WITHOUT, or NONE)
     * @param groupingLabels Labels to group by (or exclude if WITHOUT)
     * @return SQL fragment with aggregation and grouping
     */
    public SQLFragment transformAggregation(AggregationType aggType,
                                           String metricName,
                                           List<LabelMatcher> labelMatchers,
                                           ModifierType modifierType,
                                           List<String> groupingLabels) {
        return transformAggregation(aggType, metricName, labelMatchers, modifierType, groupingLabels, true);
    }

    /**
     * Transforms an aggregation expression to SQL with control over grouping behavior.
     *
     * @param aggType The aggregation function type
     * @param metricName The metric name to aggregate
     * @param labelMatchers Label filtering conditions
     * @param modifierType The modifier type (BY, WITHOUT, or NONE)
     * @param groupingLabels Labels to group by (or exclude if WITHOUT)
     * @param canonicalGrouping If true, groups only by specified labels (VictoriaMetrics behavior);
     *                          if false, groups by full label set (legacy behavior)
     * @return SQL fragment with aggregation and grouping
     */
    public SQLFragment transformAggregation(AggregationType aggType,
                                           String metricName,
                                           List<LabelMatcher> labelMatchers,
                                           ModifierType modifierType,
                                           List<String> groupingLabels,
                                           boolean canonicalGrouping) {
        logger.debug("Transforming aggregation: type={}, metric={}, modifier={}, labels={}, canonical={}",
            aggType, metricName, modifierType, groupingLabels, canonicalGrouping);

        // Build base data CTE with label_set_id for canonical grouping
        SQLFragment baseDataSQL = buildBaseDataCTE(metricName, labelMatchers);

        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>(baseDataSQL.getParameters());

        sql.append(baseDataSQL.getSql());

        // Build the aggregation query
        if (modifierType == ModifierType.NONE) {
            // No grouping - aggregate all values
            sql.append(buildAggregationWithoutGrouping(aggType));
        } else {
            // Group by labels
            sql.append(buildAggregationWithGrouping(aggType, modifierType, groupingLabels,
                parameters, canonicalGrouping));
        }

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Builds the base data CTE with label_set_id included for label-specific grouping.
     */
    private SQLFragment buildBaseDataCTE(String metricName, List<LabelMatcher> labelMatchers) {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        sql.append("WITH latest_snapshot AS (\n");
        sql.append("  SELECT MAX(").append(MetricsSchema.COL_SV_TIMESTAMP_MS)
           .append(") AS max_ts\n");
        sql.append("  FROM ").append(MetricsSchema.TABLE_SAMPLE_VALUE).append("\n");
        sql.append("),\n");

        sql.append("base_data AS (\n");
        sql.append("  SELECT\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_TIMESTAMP_MS).append(",\n");
        sql.append("    sv.").append(MetricsSchema.COL_SV_VALUE).append(",\n");
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

        return new SQLFragment(sql.toString(), parameters);
    }

    /**
     * Builds aggregation SQL without grouping (aggregates all values).
     */
    private String buildAggregationWithoutGrouping(AggregationType aggType) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT\n");
        sql.append("  datetime(MAX(timestamp_ms) / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  ").append(getAggregateFunction(aggType)).append("(value) AS value,\n");
        sql.append("  '' AS labels\n");  // Empty labels when no grouping
        sql.append("FROM base_data");

        return sql.toString();
    }

    /**
     * Builds aggregation SQL with label grouping.
     */
    private String buildAggregationWithGrouping(AggregationType aggType,
                                                ModifierType modifierType,
                                                List<String> groupingLabels,
                                                List<Object> parameters,
                                                boolean canonicalGrouping) {
        StringBuilder sql = new StringBuilder();

        // We need to extract individual label values from the labels string
        // Since labels are concatenated as "key1=value1, key2=value2", we need to parse them

        if (modifierType == ModifierType.BY) {
            // Group by specific labels
            sql.append(buildGroupBySpecificLabels(aggType, groupingLabels, parameters, canonicalGrouping));
        } else {
            // WITHOUT - group by all labels except specified ones
            // This is more complex and less commonly used, so we'll implement a simplified version
            throw new UnsupportedOperationException(
                "WITHOUT modifier not yet implemented. Use BY modifier instead.");
        }

        return sql.toString();
    }

    /**
     * Builds SQL for grouping by specific labels.
     *
     * @param canonicalGrouping If true, extracts and groups only by specified labels (VictoriaMetrics);
     *                          if false, groups by full label set (legacy)
     */
    private String buildGroupBySpecificLabels(AggregationType aggType,
                                             List<String> groupingLabels,
                                             List<Object> parameters,
                                             boolean canonicalGrouping) {
        StringBuilder sql = new StringBuilder();

        if (!canonicalGrouping || groupingLabels == null || groupingLabels.isEmpty()) {
            // Legacy behavior: group by full label set
            sql.append("SELECT\n");
            sql.append("  datetime(MAX(timestamp_ms) / 1000, 'unixepoch') AS timestamp,\n");
            sql.append("  ").append(getAggregateFunction(aggType)).append("(value) AS value,\n");
            sql.append("  labels\n");
            sql.append("FROM base_data\n");
            sql.append("GROUP BY labels\n");
            sql.append("ORDER BY labels");
            return sql.toString();
        }

        // Canonical VictoriaMetrics behavior: extract and group only by specified labels
        sql.append(",\nextracted_labels AS (\n");
        sql.append("  SELECT\n");
        sql.append("    bd.timestamp_ms,\n");
        sql.append("    bd.value,\n");
        sql.append("    bd.label_set_id,\n");

        // Extract each specified label value
        for (int i = 0; i < groupingLabels.size(); i++) {
            String labelName = groupingLabels.get(i);
            sql.append("    (SELECT lv.").append(MetricsSchema.COL_LV_VALUE).append("\n");
            sql.append("     FROM ").append(MetricsSchema.TABLE_LABEL_SET_MEMBERSHIP).append(" lsm\n");
            sql.append("     JOIN ").append(MetricsSchema.TABLE_LABEL_KEY).append(" lk ON\n");
            sql.append("       lk.").append(MetricsSchema.COL_LK_ID)
               .append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_KEY_ID).append("\n");
            sql.append("     JOIN ").append(MetricsSchema.TABLE_LABEL_VALUE).append(" lv ON\n");
            sql.append("       lv.").append(MetricsSchema.COL_LV_ID)
               .append(" = lsm.").append(MetricsSchema.COL_LSM_LABEL_VALUE_ID).append("\n");
            sql.append("     WHERE lsm.").append(MetricsSchema.COL_LSM_LABEL_SET_ID)
               .append(" = bd.label_set_id\n");
            sql.append("       AND lk.").append(MetricsSchema.COL_LK_NAME).append(" = ?\n");
            parameters.add(labelName);
            sql.append("     LIMIT 1) AS label_").append(i);
            if (i < groupingLabels.size() - 1) {
                sql.append(",\n");
            } else {
                sql.append("\n");
            }
        }

        sql.append("  FROM base_data bd\n");
        sql.append("),\n");

        // Build the grouped label string for each combination
        sql.append("grouped_data AS (\n");
        sql.append("  SELECT\n");
        sql.append("    timestamp_ms,\n");
        sql.append("    value,\n");

        // Build label string with only specified labels
        if (groupingLabels.size() == 1) {
            sql.append("    COALESCE('").append(groupingLabels.get(0)).append("=' || label_0, '') AS group_labels\n");
        } else {
            sql.append("    ");
            for (int i = 0; i < groupingLabels.size(); i++) {
                if (i > 0) {
                    sql.append(" || ', ' || ");
                }
                sql.append("COALESCE('").append(groupingLabels.get(i)).append("=' || label_").append(i).append(", '')");
            }
            sql.append(" AS group_labels\n");
        }

        sql.append("  FROM extracted_labels\n");
        sql.append(")\n");

        // Final aggregation
        sql.append("SELECT\n");
        sql.append("  datetime(MAX(timestamp_ms) / 1000, 'unixepoch') AS timestamp,\n");
        sql.append("  ").append(getAggregateFunction(aggType)).append("(value) AS value,\n");
        sql.append("  group_labels AS labels\n");
        sql.append("FROM grouped_data\n");
        sql.append("GROUP BY group_labels\n");
        sql.append("ORDER BY group_labels");

        return sql.toString();
    }

    /**
     * Returns the SQL aggregate function name for the given type.
     */
    private String getAggregateFunction(AggregationType aggType) {
        return switch (aggType) {
            case SUM -> "SUM";
            case AVG -> "AVG";
            case MIN -> "MIN";
            case MAX -> "MAX";
            case COUNT -> "COUNT";
            case STDDEV -> "STDDEV";  // Note: SQLite doesn't have STDDEV, may need custom implementation
        };
    }
}
