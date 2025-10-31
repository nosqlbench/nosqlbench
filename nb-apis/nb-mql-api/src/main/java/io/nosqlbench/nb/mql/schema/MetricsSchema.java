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

package io.nosqlbench.nb.mql.schema;

/**
 * Schema constants for NoSQLBench SQLite metrics database.
 * This schema is defined and created by SqliteSnapshotReporter in nb-api.
 * These constants provide a read-only view for query operations.
 *
 * <h2>Schema Overview</h2>
 * <ul>
 *   <li><b>metric_family</b> - Top-level metric families (e.g., "patterns")</li>
 *   <li><b>sample_name</b> - Individual sample names (e.g., "patterns_total")</li>
 *   <li><b>metric_instance</b> - Unique combinations of sample_name + label_set</li>
 *   <li><b>sample_value</b> - Time-series data points</li>
 *   <li><b>label_set</b> - Normalized label sets with hash-based deduplication</li>
 *   <li><b>label_key/label_value</b> - Normalized label keys and values</li>
 *   <li><b>label_set_membership</b> - Many-to-many: label_set → label_key/value pairs</li>
 *   <li><b>label_metadata</b> - Session metadata associated with label sets (version, command-line, hardware)</li>
 * </ul>
 *
 * <h2>Session Metadata</h2>
 * The label_metadata table stores textual metadata associated with label sets.
 * NoSQLBench automatically stores session information:
 * <ul>
 *   <li><b>nb.version</b> - NoSQLBench version</li>
 *   <li><b>nb.commandline</b> - Full command-line invocation</li>
 *   <li><b>nb.hardware</b> - Hardware/system summary</li>
 * </ul>
 *
 * <h2>Example Query: Get Session Metadata</h2>
 * <pre>
 * SELECT
 *   ls.hash AS label_set,
 *   lm.metadata_key,
 *   lm.metadata_value
 * FROM label_metadata lm
 * JOIN label_set ls ON ls.id = lm.label_set_id
 * ORDER BY ls.hash, lm.metadata_key
 * </pre>
 */
public class MetricsSchema {

    // Table names
    public static final String TABLE_METRIC_FAMILY = "metric_family";
    public static final String TABLE_SAMPLE_NAME = "sample_name";
    public static final String TABLE_METRIC_INSTANCE = "metric_instance";
    public static final String TABLE_SAMPLE_VALUE = "sample_value";
    public static final String TABLE_SAMPLE_QUANTILE = "sample_quantile";
    public static final String TABLE_SAMPLE_RATE = "sample_rate";
    public static final String TABLE_SAMPLE_HISTOGRAM = "sample_histogram";
    public static final String TABLE_LABEL_KEY = "label_key";
    public static final String TABLE_LABEL_VALUE = "label_value";
    public static final String TABLE_LABEL_SET = "label_set";
    public static final String TABLE_LABEL_SET_MEMBERSHIP = "label_set_membership";

    // metric_family columns
    public static final String COL_MF_ID = "id";
    public static final String COL_MF_NAME = "name";
    public static final String COL_MF_HELP = "help";
    public static final String COL_MF_UNIT = "unit";
    public static final String COL_MF_TYPE = "type";

    // sample_name columns
    public static final String COL_SN_ID = "id";
    public static final String COL_SN_METRIC_FAMILY_ID = "metric_family_id";
    public static final String COL_SN_SAMPLE = "sample";

    // metric_instance columns
    public static final String COL_MI_ID = "id";
    public static final String COL_MI_SAMPLE_NAME_ID = "sample_name_id";
    public static final String COL_MI_LABEL_SET_ID = "label_set_id";
    public static final String COL_MI_SPEC = "spec";

    // sample_value columns
    public static final String COL_SV_ID = "id";
    public static final String COL_SV_METRIC_INSTANCE_ID = "metric_instance_id";
    public static final String COL_SV_TIMESTAMP_MS = "timestamp_ms";
    public static final String COL_SV_VALUE = "value";

    // sample_quantile columns
    public static final String COL_SQ_SAMPLE_VALUE_ID = "sample_value_id";
    public static final String COL_SQ_QUANTILE = "quantile";
    public static final String COL_SQ_QUANTILE_VALUE = "quantile_value";

    // sample_rate columns
    public static final String COL_SR_SAMPLE_VALUE_ID = "sample_value_id";
    public static final String COL_SR_RATE_TYPE = "rate_type";
    public static final String COL_SR_RATE_VALUE = "rate_value";

    // sample_histogram columns
    public static final String COL_SH_SAMPLE_VALUE_ID = "sample_value_id";
    public static final String COL_SH_START_SECONDS = "start_seconds";
    public static final String COL_SH_INTERVAL_SECONDS = "interval_seconds";
    public static final String COL_SH_MAX_VALUE = "max_value";
    public static final String COL_SH_HISTOGRAM_BASE64 = "histogram_base64";

    // label_key columns
    public static final String COL_LK_ID = "id";
    public static final String COL_LK_NAME = "name";

    // label_value columns
    public static final String COL_LV_ID = "id";
    public static final String COL_LV_VALUE = "value";

    // label_set columns
    public static final String COL_LS_ID = "id";
    public static final String COL_LS_HASH = "hash";

    // label_set_membership columns
    public static final String COL_LSM_LABEL_SET_ID = "label_set_id";
    public static final String COL_LSM_LABEL_KEY_ID = "label_key_id";
    public static final String COL_LSM_LABEL_VALUE_ID = "label_value_id";

    // label_metadata table (session metadata storage)
    public static final String TABLE_LABEL_METADATA = "label_metadata";
    public static final String COL_LM_ID = "id";
    public static final String COL_LM_LABEL_SET_ID = "label_set_id";
    public static final String COL_LM_METADATA_KEY = "metadata_key";
    public static final String COL_LM_METADATA_VALUE = "metadata_value";

    // Common SQL fragments for building queries

    /**
     * SQL fragment to join sample_value with metric_instance.
     * Usage: FROM sample_value sv JOIN metric_instance mi ON ...
     */
    public static String joinMetricInstance() {
        return String.format("%s.%s = %s.%s",
            "mi", COL_MI_ID,
            "sv", COL_SV_METRIC_INSTANCE_ID);
    }

    /**
     * SQL fragment to join metric_instance with sample_name.
     * Usage: FROM metric_instance mi JOIN sample_name sn ON ...
     */
    public static String joinSampleName() {
        return String.format("%s.%s = %s.%s",
            "sn", COL_SN_ID,
            "mi", COL_MI_SAMPLE_NAME_ID);
    }

    /**
     * SQL fragment to join metric_instance with label_set.
     * Usage: FROM metric_instance mi JOIN label_set ls ON ...
     */
    public static String joinLabelSet() {
        return String.format("%s.%s = %s.%s",
            "ls", COL_LS_ID,
            "mi", COL_MI_LABEL_SET_ID);
    }

    /**
     * SQL fragment to join label_set with label_set_membership.
     * Usage: JOIN label_set_membership lsm ON ...
     */
    public static String joinLabelSetMembership() {
        return String.format("%s.%s = %s.%s",
            "lsm", COL_LSM_LABEL_SET_ID,
            "ls", COL_LS_ID);
    }

    /**
     * SQL fragment to join label_set_membership with label_key.
     * Usage: JOIN label_key lk ON ...
     */
    public static String joinLabelKey() {
        return String.format("%s.%s = %s.%s",
            "lk", COL_LK_ID,
            "lsm", COL_LSM_LABEL_KEY_ID);
    }

    /**
     * SQL fragment to join label_set_membership with label_value.
     * Usage: JOIN label_value lv ON ...
     */
    public static String joinLabelValue() {
        return String.format("%s.%s = %s.%s",
            "lv", COL_LV_ID,
            "lsm", COL_LSM_LABEL_VALUE_ID);
    }

    /**
     * Complete SQL fragment for joining all label-related tables through metric_instance.
     * Produces: JOIN metric_instance mi ON ... JOIN label_set ls ON ... LEFT JOIN label_set_membership lsm ON ... etc.
     * NOTE: Does NOT include sample_name join - add explicitly if needed via joinSampleName().
     */
    public static String joinAllLabels() {
        return String.format(
            "JOIN %s mi ON %s " +
            "JOIN %s ls ON %s " +
            "LEFT JOIN %s lsm ON %s " +
            "LEFT JOIN %s lk ON %s " +
            "LEFT JOIN %s lv ON %s",
            TABLE_METRIC_INSTANCE, joinMetricInstance(),
            TABLE_LABEL_SET, joinLabelSet(),
            TABLE_LABEL_SET_MEMBERSHIP, joinLabelSetMembership(),
            TABLE_LABEL_KEY, joinLabelKey(),
            TABLE_LABEL_VALUE, joinLabelValue()
        );
    }

    /**
     * Complete SQL fragment for joining all label-related tables AND sample_name through metric_instance.
     * Produces: JOIN metric_instance mi ON ... JOIN sample_name sn ON ... JOIN label_set ls ON ... etc.
     */
    public static String joinAllLabelsWithSampleName() {
        return String.format(
            "JOIN %s mi ON %s " +
            "JOIN %s sn ON %s " +
            "JOIN %s ls ON %s " +
            "LEFT JOIN %s lsm ON %s " +
            "LEFT JOIN %s lk ON %s " +
            "LEFT JOIN %s lv ON %s",
            TABLE_METRIC_INSTANCE, joinMetricInstance(),
            TABLE_SAMPLE_NAME, joinSampleName(),
            TABLE_LABEL_SET, joinLabelSet(),
            TABLE_LABEL_SET_MEMBERSHIP, joinLabelSetMembership(),
            TABLE_LABEL_KEY, joinLabelKey(),
            TABLE_LABEL_VALUE, joinLabelValue()
        );
    }

    private MetricsSchema() {
        // Utility class - prevent instantiation
    }
}
