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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MeterSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.MetricFamily;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.PointSample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.RateStatistics;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.Sample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummarySample;
import io.nosqlbench.nb.api.engine.metrics.view.MetricsView.SummaryStatistics;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.HdrHistogram.EncodableHistogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * A SQLite reporter that writes metrics using an OpenMetrics-aligned schema.
 * This implementation:
 * <ul>
 *     <li>Consumes immutable {@link MetricsView} snapshots and buffers inserts.</li>
 *     <li>Normalises metric families, sample names, and label sets so they can be reused via
 *     reference tables (metric_names, sample_names, label_sets, label_values).</li>
 *     <li>Stores samples in a compact form that allows efficient selection of time-series data using
 *     simple joins keyed by metric or label identifiers.</li>
 * </ul>
 */
public class SqliteSnapshotReporter extends MetricsSnapshotReporterBase {

    private static final Logger logger = LogManager.getLogger(SqliteSnapshotReporter.class);

    private final String jdbcUrl;
    private final Connection connection;
    private final MetricInstanceFilter filter;
    private final boolean includeHistograms;

    private final PreparedStatement insertMetricFamily;
    private final PreparedStatement insertSampleFamily;
    private final PreparedStatement insertSample;
    private final PreparedStatement insertSampleQuantile;
    private final PreparedStatement insertSampleRate;
    private final PreparedStatement insertSampleHistogram;
    private final PreparedStatement insertLabel;
    private final PreparedStatement insertLabelValue;
    private final PreparedStatement insertLabelSet;
    private final PreparedStatement insertLabelSetMembership;
    private final PreparedStatement selectMetricFamilyByName;
    private final PreparedStatement selectSampleNameByFamilyAndSample;
    private final PreparedStatement selectLabelSetByHash;
    private final PreparedStatement selectLabelKeyByName;
    private final PreparedStatement selectLabelValueByValue;
    private final PreparedStatement selectSampleValueByNaturalKey;

    private final Map<String, Integer> metricFamilyCache = new LinkedHashMap<>();
    private final Map<String, Integer> sampleNameCache = new LinkedHashMap<>();
    private final Map<String, Integer> labelKeyCache = new LinkedHashMap<>();
    private final Map<String, Integer> labelValueCache = new LinkedHashMap<>();
    private final Map<Map<String, String>, Integer> labelSetCache = new LinkedHashMap<>();

    public SqliteSnapshotReporter(NBComponent parent,
                                  String jdbcUrl,
                                  long intervalMillis,
                                  MetricInstanceFilter filter,
                                  NBLabels extraLabels) {
        this(parent, jdbcUrl, intervalMillis, filter, extraLabels, false);
    }

    public SqliteSnapshotReporter(NBComponent parent,
                                  String jdbcUrl,
                                  long intervalMillis,
                                  MetricInstanceFilter filter,
                                  NBLabels extraLabels,
                                  boolean includeHistograms) {
        super(parent, extraLabels, intervalMillis);
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl);
        this.filter = (filter != null) ? filter : new MetricInstanceFilter();
        this.includeHistograms = includeHistograms;
        try {
            this.connection = DriverManager.getConnection(jdbcUrl);
            this.connection.setAutoCommit(false);
            initialiseSchema();
            this.insertMetricFamily = connection.prepareStatement("""
                INSERT OR IGNORE INTO metric_family(name, help, unit, type)
                VALUES (?, ?, ?, ?)
            """, Statement.RETURN_GENERATED_KEYS);
            this.insertSampleFamily = connection.prepareStatement("""
                INSERT OR IGNORE INTO sample_name(metric_family_id, sample)
                VALUES (?, ?)
            """, Statement.RETURN_GENERATED_KEYS);
            this.insertSample = connection.prepareStatement("""
                INSERT INTO sample_value(sample_name_id, label_set_id, timestamp_ms, value, count, sum, min, max, mean, stddev)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """);
            this.insertSampleQuantile = connection.prepareStatement("""
                INSERT INTO sample_quantile(sample_value_id, quantile, quantile_value)
                VALUES (?, ?, ?)
            """);
            this.insertSampleRate = connection.prepareStatement("""
                INSERT INTO sample_rate(sample_value_id, rate_type, rate_value)
                VALUES (?, ?, ?)
            """);
            this.insertSampleHistogram = includeHistograms
                ? connection.prepareStatement("""
                    INSERT OR REPLACE INTO sample_histogram(
                        sample_value_id,
                        start_seconds,
                        interval_seconds,
                        max_value,
                        histogram_base64
                    ) VALUES (?, ?, ?, ?, ?)
                """)
                : null;
            this.insertLabel = connection.prepareStatement("""
                INSERT OR IGNORE INTO label_key(name) VALUES (?)
            """, Statement.RETURN_GENERATED_KEYS);
            this.insertLabelValue = connection.prepareStatement("""
                INSERT OR IGNORE INTO label_value(value) VALUES (?)
            """, Statement.RETURN_GENERATED_KEYS);
            this.insertLabelSet = connection.prepareStatement("""
                INSERT OR IGNORE INTO label_set(hash) VALUES (?)
            """, Statement.RETURN_GENERATED_KEYS);
            this.insertLabelSetMembership = connection.prepareStatement("""
                INSERT OR IGNORE INTO label_set_membership(label_set_id, label_key_id, label_value_id)
                VALUES (?, ?, ?)
            """);
            this.selectMetricFamilyByName = connection.prepareStatement("""
                SELECT id FROM metric_family WHERE name=?
            """);
            this.selectSampleNameByFamilyAndSample = connection.prepareStatement("""
                SELECT id FROM sample_name WHERE metric_family_id=? AND sample=?
            """);
            this.selectLabelSetByHash = connection.prepareStatement("""
                SELECT id FROM label_set WHERE hash=?
            """);
            this.selectLabelKeyByName = connection.prepareStatement("""
                SELECT id FROM label_key WHERE name=?
            """);
            this.selectLabelValueByValue = connection.prepareStatement("""
                SELECT id FROM label_value WHERE value=?
            """);
            this.selectSampleValueByNaturalKey = connection.prepareStatement("""
                SELECT id FROM sample_value WHERE sample_name_id=? AND label_set_id=? AND timestamp_ms=?
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialise SQLite snapshot reporter.", e);
        }
    }

    public SqliteSnapshotReporter(NBComponent parent,
                                  String jdbcUrl,
                                  long intervalMillis) {
        this(parent, jdbcUrl, intervalMillis, null, null, false);
    }

    public SqliteSnapshotReporter(NBComponent parent,
                                  String jdbcUrl,
                                  long intervalMillis,
                                  MetricInstanceFilter filter) {
        this(parent, jdbcUrl, intervalMillis, filter, null, false);
    }

    public SqliteSnapshotReporter(NBComponent parent,
                                  String jdbcUrl,
                                  long intervalMillis,
                                  NBLabels extraLabels) {
        this(parent, jdbcUrl, intervalMillis, null, extraLabels, false);
    }

    public SqliteSnapshotReporter(NBComponent parent,
                                  String jdbcUrl,
                                  long intervalMillis,
                                  MetricInstanceFilter filter,
                                  boolean includeHistograms) {
        this(parent, jdbcUrl, intervalMillis, filter, null, includeHistograms);
    }

    public SqliteSnapshotReporter(NBComponent parent,
                                  String jdbcUrl,
                                  long intervalMillis,
                                  NBLabels extraLabels,
                                  boolean includeHistograms) {
        this(parent, jdbcUrl, intervalMillis, null, extraLabels, includeHistograms);
    }

    private void initialiseSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS metric_family (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    help TEXT,
                    unit TEXT,
                    type TEXT NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sample_name (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    metric_family_id INTEGER NOT NULL,
                    sample TEXT NOT NULL,
                    UNIQUE(metric_family_id, sample),
                    FOREIGN KEY(metric_family_id) REFERENCES metric_family(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS label_key (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS label_value (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT UNIQUE
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS label_set (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    hash TEXT UNIQUE NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS label_set_membership (
                    label_set_id INTEGER NOT NULL,
                    label_key_id INTEGER NOT NULL,
                    label_value_id INTEGER NOT NULL,
                    UNIQUE(label_set_id, label_key_id),
                    FOREIGN KEY(label_set_id) REFERENCES label_set(id),
                    FOREIGN KEY(label_key_id) REFERENCES label_key(id),
                    FOREIGN KEY(label_value_id) REFERENCES label_value(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sample_value (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sample_name_id INTEGER NOT NULL,
                    label_set_id INTEGER NOT NULL,
                    timestamp_ms INTEGER NOT NULL,
                    value REAL,
                    count INTEGER,
                    sum REAL,
                    min REAL,
                    max REAL,
                    mean REAL,
                    stddev REAL,
                    FOREIGN KEY(sample_name_id) REFERENCES sample_name(id),
                    FOREIGN KEY(label_set_id) REFERENCES label_set(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sample_quantile (
                    sample_value_id INTEGER NOT NULL,
                    quantile REAL NOT NULL,
                    quantile_value REAL,
                    PRIMARY KEY(sample_value_id, quantile),
                    FOREIGN KEY(sample_value_id) REFERENCES sample_value(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sample_rate (
                    sample_value_id INTEGER NOT NULL,
                    rate_type TEXT NOT NULL,
                    rate_value REAL,
                    PRIMARY KEY(sample_value_id, rate_type),
                    FOREIGN KEY(sample_value_id) REFERENCES sample_value(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sample_histogram (
                    sample_value_id INTEGER PRIMARY KEY,
                    start_seconds REAL NOT NULL,
                    interval_seconds REAL NOT NULL,
                    max_value REAL,
                    histogram_base64 TEXT NOT NULL,
                    FOREIGN KEY(sample_value_id) REFERENCES sample_value(id)
                )
            """);
        }
        connection.commit();
    }

    @Override
    public void onMetricsSnapshot(MetricsView view) {
        long epochMillis = view.capturedAtEpochMillis();
        try {
            for (MetricFamily family : view.families()) {
                int familyId = resolveMetricFamilyId(family);
                for (Sample sample : family.samples()) {
                    String handle = sample.labels().linearizeAsMetrics();
                    if (!filter.matches(handle, sample.labels())) {
                        continue;
                    }
                    int sampleNameId = resolveSampleNameId(familyId, sample.sampleName());
                    int labelSetId = resolveLabelSetId(sample.labels());
                    long sampleId = insertSampleRow(sampleNameId, labelSetId, epochMillis, sample);
                    if (sample instanceof SummarySample summarySample) {
                        writeSummaryDetails(sampleId, summarySample);
                    } else if (sample instanceof MeterSample meterSample) {
                        writeMeterRates(sampleId, meterSample);
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollback) {
                logger.error("Failed to rollback after error.", rollback);
            }
            throw new RuntimeException("Unable to write metrics snapshot to SQLite.", e);
        }
    }

    private int resolveMetricFamilyId(MetricFamily family) throws SQLException {
        return metricFamilyCache.computeIfAbsent(family.familyName(), key -> {
            try {
                insertMetricFamily.setString(1, family.familyName());
                insertMetricFamily.setString(2, family.help());
                insertMetricFamily.setString(3, family.unit());
                insertMetricFamily.setString(4, family.type().name());
                insertMetricFamily.executeUpdate();
                Integer id = selectLastInsertedId(insertMetricFamily);
                if (id == null) {
                    selectMetricFamilyByName.setString(1, family.familyName());
                    try (ResultSet rs = selectMetricFamilyByName.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                    throw new RuntimeException("Unable to resolve metric family id for " + family.familyName());
                }
                return id;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int resolveSampleNameId(int familyId, String sampleName) throws SQLException {
        String cacheKey = familyId + ":" + sampleName;
        return sampleNameCache.computeIfAbsent(cacheKey, key -> {
            try {
                insertSampleFamily.setInt(1, familyId);
                insertSampleFamily.setString(2, sampleName);
                insertSampleFamily.executeUpdate();
                Integer id = selectLastInsertedId(insertSampleFamily);
                if (id == null) {
                    selectSampleNameByFamilyAndSample.setInt(1, familyId);
                    selectSampleNameByFamilyAndSample.setString(2, sampleName);
                    try (ResultSet rs = selectSampleNameByFamilyAndSample.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                    throw new RuntimeException("Unable to resolve sample name id for " + sampleName);
                }
                return id;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int resolveLabelSetId(NBLabels labels) throws SQLException {
        Map<String, String> map = new LinkedHashMap<>(labels.asMap());
        map.remove("name");
        map.remove("unit");
        return labelSetCache.computeIfAbsent(map, key -> {
            try {
                String hash = labelSetHash(key);
                insertLabelSet.setString(1, hash);
                insertLabelSet.executeUpdate();
                Integer setId = selectLastInsertedId(insertLabelSet);
                if (setId == null) {
                    selectLabelSetByHash.setString(1, hash);
                    try (ResultSet rs = selectLabelSetByHash.executeQuery()) {
                        if (rs.next()) {
                            setId = rs.getInt(1);
                        }
                    }
                }
                if (setId == null) {
                    throw new RuntimeException("Unable to resolve label set id for " + hash);
                }
                for (Map.Entry<String, String> entry : key.entrySet()) {
                    int keyId = resolveLabelKey(entry.getKey());
                    int valueId = resolveLabelValue(entry.getValue());
                    insertLabelSetMembership.setInt(1, setId);
                    insertLabelSetMembership.setInt(2, keyId);
                    insertLabelSetMembership.setInt(3, valueId);
                    insertLabelSetMembership.executeUpdate();
                }
                return setId;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int resolveLabelKey(String key) throws SQLException {
        return labelKeyCache.computeIfAbsent(key, name -> {
            try {
                insertLabel.setString(1, name);
                insertLabel.executeUpdate();
                Integer id = selectLastInsertedId(insertLabel);
                if (id == null) {
                    selectLabelKeyByName.setString(1, name);
                    try (ResultSet rs = selectLabelKeyByName.executeQuery()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
                if (id == null) {
                    throw new RuntimeException("Unable to resolve label key id for " + name);
                }
                return id;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int resolveLabelValue(String value)	throws SQLException {
        String normalized = (value == null) ? "" : value;
        return labelValueCache.computeIfAbsent(normalized, val -> {
            try {
                insertLabelValue.setString(1, normalized);
                insertLabelValue.executeUpdate();
                Integer id = selectLastInsertedId(insertLabelValue);
                if (id == null) {
                    selectLabelValueByValue.setString(1, normalized);
                    try (ResultSet rs = selectLabelValueByValue.executeQuery()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
                if (id == null) {
                    throw new RuntimeException("Unable to resolve label value id for " + normalized);
                }
                return id;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private long insertSampleRow(int sampleNameId,
                                 int labelSetId,
                                 long epochMillis,
                                 Sample sample) throws SQLException {
        Double primaryValue = primaryValue(sample);
        SummaryStatistics summaryStatistics = (sample instanceof SummarySample ss) ? ss.statistics() : null;

        insertSample.setInt(1, sampleNameId);
        insertSample.setInt(2, labelSetId);
        insertSample.setLong(3, epochMillis);
        insertSample.setObject(4, primaryValue);
        insertSample.setObject(5, (summaryStatistics != null) ? summaryStatistics.count() : null);
        insertSample.setObject(6, (sample instanceof SummarySample ss) ? ss.sum() : null);
        insertSample.setObject(7, (summaryStatistics != null) ? summaryStatistics.min() : null);
        insertSample.setObject(8, (summaryStatistics != null) ? summaryStatistics.max() : null);
        insertSample.setObject(9, (summaryStatistics != null) ? summaryStatistics.mean() : null);
        insertSample.setObject(10, (summaryStatistics != null) ? summaryStatistics.stddev() : null);
        insertSample.executeUpdate();

        long id;
        try (ResultSet rs = insertSample.getGeneratedKeys()) {
            if (rs.next()) {
                id = rs.getLong(1);
            } else {
                selectSampleValueByNaturalKey.setInt(1, sampleNameId);
                selectSampleValueByNaturalKey.setInt(2, labelSetId);
                selectSampleValueByNaturalKey.setLong(3, epochMillis);
                try (ResultSet data = selectSampleValueByNaturalKey.executeQuery()) {
                    if (data.next()) {
                        id = data.getLong(1);
                    } else {
                        throw new RuntimeException("Unable to resolve sample_value id.");
                    }
                }
            }
        }
        return id;
    }

    private void writeSummaryDetails(long sampleId, SummarySample sample)	throws SQLException {
        writeHistogramEncoding(sampleId, sample);
        for (Map.Entry<Double, Double> entry : sample.quantiles().entrySet()) {
            insertSampleQuantile.setLong(1, sampleId);
            insertSampleQuantile.setDouble(2, entry.getKey());
            insertSampleQuantile.setDouble(3, entry.getValue());
            insertSampleQuantile.executeUpdate();
        }
        RateStatistics rates = sample.rates();
        if (rates != null) {
            insertSampleRate.setLong(1, sampleId);
            insertSampleRate.setString(2, "mean");
            insertSampleRate.setDouble(3, rates.mean());
            insertSampleRate.executeUpdate();

            insertSampleRate.setLong(1, sampleId);
            insertSampleRate.setString(2, "m1");
            insertSampleRate.setDouble(3, rates.oneMinute());
            insertSampleRate.executeUpdate();

            insertSampleRate.setLong(1, sampleId);
            insertSampleRate.setString(2, "m5");
            insertSampleRate.setDouble(3, rates.fiveMinute());
            insertSampleRate.executeUpdate();

            insertSampleRate.setLong(1, sampleId);
            insertSampleRate.setString(2, "m15");
            insertSampleRate.setDouble(3, rates.fifteenMinute());
            insertSampleRate.executeUpdate();
        }
    }

    private void writeHistogramEncoding(long sampleId, SummarySample sample) throws SQLException {
        if (!includeHistograms || insertSampleHistogram == null) {
            return;
        }
        Optional<EncodableHistogram> histogram = sample.snapshot().asEncodableHistogram();
        if (histogram.isEmpty()) {
            return;
        }
        EncodedHistogram encoded = encodeHistogram(histogram.get());
        insertSampleHistogram.setLong(1, sampleId);
        insertSampleHistogram.setDouble(2, encoded.startSeconds());
        insertSampleHistogram.setDouble(3, encoded.intervalSeconds());
        insertSampleHistogram.setDouble(4, encoded.maxValue());
        insertSampleHistogram.setString(5, encoded.base64());
        insertSampleHistogram.executeUpdate();
    }

    private EncodedHistogram encodeHistogram(EncodableHistogram histogram) {
        int capacity = histogram.getNeededByteBufferCapacity();
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        int length = histogram.encodeIntoCompressedByteBuffer(buffer, 0);
        buffer.position(0);
        buffer.limit(length);
        byte[] encodedBytes = new byte[length];
        buffer.get(encodedBytes);
        String base64 = Base64.getEncoder().encodeToString(encodedBytes);
        double startSeconds = histogram.getStartTimeStamp() / 1000.0d;
        double intervalSeconds = Math.max(0.0d, (histogram.getEndTimeStamp() - histogram.getStartTimeStamp()) / 1000.0d);
        double maxValue = histogram.getMaxValueAsDouble();
        return new EncodedHistogram(startSeconds, intervalSeconds, maxValue, base64);
    }

    private void writeMeterRates(long sampleId, MeterSample meterSample)	throws SQLException {
        insertSampleRate.setLong(1, sampleId);
        insertSampleRate.setString(2, "mean");
        insertSampleRate.setDouble(3, meterSample.meanRate());
        insertSampleRate.executeUpdate();

        insertSampleRate.setLong(1, sampleId);
        insertSampleRate.setString(2, "m1");
        insertSampleRate.setDouble(3, meterSample.oneMinuteRate());
        insertSampleRate.executeUpdate();

        insertSampleRate.setLong(1, sampleId);
        insertSampleRate.setString(2, "m5");
        insertSampleRate.setDouble(3, meterSample.fiveMinuteRate());
        insertSampleRate.executeUpdate();

        insertSampleRate.setLong(1, sampleId);
        insertSampleRate.setString(2, "m15");
        insertSampleRate.setDouble(3, meterSample.fifteenMinuteRate());
        insertSampleRate.executeUpdate();
    }

    private Double primaryValue(Sample sample) {
        if (sample instanceof PointSample pointSample) {
            return pointSample.value();
        } else if (sample instanceof MeterSample meterSample) {
            return (double) meterSample.count();
        } else if (sample instanceof SummarySample summarySample) {
            return (double) summarySample.statistics().count();
        }
        return null;
    }

    private String labelSetHash(Map<String, String> labels) {
        if (labels.isEmpty()) {
            return "{}";
        }
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        labels.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> joiner.add(entry.getKey() + "=" + entry.getValue()));
        return joiner.toString();
    }

    private Integer selectLastInsertedId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    @Override
    protected void teardown() {
        try {
            insertMetricFamily.close();
            insertSampleFamily.close();
            insertSample.close();
            insertSampleQuantile.close();
            insertSampleRate.close();
            if (insertSampleHistogram != null) {
                insertSampleHistogram.close();
            }
            selectMetricFamilyByName.close();
            selectSampleNameByFamilyAndSample.close();
            selectLabelSetByHash.close();
            selectLabelKeyByName.close();
            selectLabelValueByValue.close();
            selectSampleValueByNaturalKey.close();
            insertLabel.close();
            insertLabelValue.close();
            insertLabelSet.close();
            insertLabelSetMembership.close();
            connection.close();
        } catch (SQLException e) {
            logger.warn("Error closing SQLite snapshot reporter resources.", e);
        }
        super.teardown();
    }

    private record EncodedHistogram(double startSeconds, double intervalSeconds, double maxValue, String base64) {
    }
}
