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
 */
public class MetricsSchema {

    // Table names
    public static final String TABLE_METRIC_FAMILY = "metric_family";
    public static final String TABLE_SAMPLE_NAME = "sample_name";
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

    // sample_value columns
    public static final String COL_SV_ID = "id";
    public static final String COL_SV_SAMPLE_NAME_ID = "sample_name_id";
    public static final String COL_SV_LABEL_SET_ID = "label_set_id";
    public static final String COL_SV_TIMESTAMP_MS = "timestamp_ms";
    public static final String COL_SV_VALUE = "value";
    public static final String COL_SV_COUNT = "count";
    public static final String COL_SV_SUM = "sum";
    public static final String COL_SV_MIN = "min";
    public static final String COL_SV_MAX = "max";
    public static final String COL_SV_MEAN = "mean";
    public static final String COL_SV_STDDEV = "stddev";

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

    // Common SQL fragments for building queries

    /**
     * SQL fragment to join sample_value with sample_name.
     * Usage: FROM sample_value sv JOIN sample_name sn ON ...
     */
    public static String joinSampleName() {
        return String.format("%s.%s = %s.%s",
            "sn", COL_SN_ID,
            "sv", COL_SV_SAMPLE_NAME_ID);
    }

    /**
     * SQL fragment to join sample_value with label_set.
     * Usage: FROM sample_value sv JOIN label_set ls ON ...
     */
    public static String joinLabelSet() {
        return String.format("%s.%s = %s.%s",
            "ls", COL_LS_ID,
            "sv", COL_SV_LABEL_SET_ID);
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
     * Complete SQL fragment for joining all label-related tables.
     * Produces: JOIN label_set ls ON ... LEFT JOIN label_set_membership lsm ON ... etc.
     */
    public static String joinAllLabels() {
        return String.format(
            "JOIN %s ls ON %s " +
            "LEFT JOIN %s lsm ON %s " +
            "LEFT JOIN %s lk ON %s " +
            "LEFT JOIN %s lv ON %s",
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
