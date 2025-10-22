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

package io.nosqlbench.nb.api.engine.metrics.view;

import io.nosqlbench.nb.api.engine.metrics.ConvenientSnapshot;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetric;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricMeter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Immutable snapshot used by the metrics pipeline to serialise live instruments into OpenMetrics-aligned structures.
 *
 * <p>A {@code MetricsView} is produced by {@link MetricsView#capture(Collection, long)} (usually from the
 * {@link MetricsSnapshotScheduler}) and consumed by reporters such as CSV,
 * Prometheus, SQLite, Log4J, etc. It normalises instrumentation into {@link MetricFamily metric families} so that
 * OpenMetrics concepts—names, samples, units, labels, and types—are preserved even if the originating instruments
 * differ in their native representations.</p>
 *
 * <pre>
 * NBMetric instruments (gauges, counters, timers, histograms…)
 *            │
 *            ▼
 *     MetricsView.capture(...)   ──►   MetricFamily (type, help, unit, labels)
 *                                        ├─ Sample: {@link PointSample gauge} / {@link MeterSample meter}
 *                                        └─ Sample: {@link SummarySample summary} with quantiles & rates
 *
 * Reporter (Console, CSV, SQLite, Prometheus, …) consumes the immutable view and renders/export data.
 * </pre>
 *
 * <p>Key properties:</p>
 * <ul>
 *   <li>Immutable – families and samples are defensive copies so reporters cannot mutate live instruments.</li>
 *   <li>OpenMetrics-friendly – names/labels are sanitised, counters get {@code _total} suffixes, timers and histograms
 *       are expressed as summary samples with quantiles and rates.</li>
 *   <li>Aggregation-aware – {@link #combine(Collection)} merges multiple views, preserving sample semantics while
 *       adding capture windows, counts, quantiles, and rate statistics.</li>
 *   <li>Captures timing metadata – each view records {@link #capturedAt()} and the sampling window so cadenced
 *       reporters can compute rates or alignment.</li>
 * </ul>
 */
public final class MetricsView {

    /**
     * Default cache window that is used when a metric does not provide its own caching semantics.
     * Refreshed snapshots within the same window are considered equivalent.
     */
    private static final long DEFAULT_CACHE_WINDOW_MILLIS = 1_000L;
    public static final long DEFAULT_INTERVAL = DEFAULT_CACHE_WINDOW_MILLIS;

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]*$");
    private static final List<Double> DEFAULT_QUANTILES = List.of(
        0.5d, 0.75d, 0.90d, 0.95d, 0.98d, 0.99d, 0.999d
    );
    private final Instant capturedAt;
    private final Instant windowStart;
    private final long intervalMillis;
    private final List<MetricFamily> families;

    private MetricsView(Instant capturedAt, long intervalMillis, List<MetricFamily> families) {
        this.capturedAt = capturedAt;
        this.intervalMillis = intervalMillis;
        if (intervalMillis > 0) {
            this.windowStart = capturedAt.minusMillis(intervalMillis);
        } else {
            this.windowStart = capturedAt;
        }
        this.families = List.copyOf(families);
    }

    static MetricsView forTesting(Instant capturedAt, long intervalMillis, List<MetricFamily> families) {
        return new MetricsView(capturedAt, intervalMillis, families);
    }

    public Instant capturedAt() {
        return capturedAt;
    }

    public long capturedAtEpochMillis() {
        return capturedAt.toEpochMilli();
    }

    public Instant windowStart() {
        return windowStart;
    }

    public long windowStartEpochMillis() {
        return windowStart.toEpochMilli();
    }

    public long intervalMillis() {
        return intervalMillis;
    }

    public List<MetricFamily> families() {
        return families;
    }

    public boolean isEmpty() {
        return families.isEmpty();
    }

    public static MetricsView capture(Collection<? extends NBMetric> metrics, long intervalMillis) {
        if (metrics.isEmpty()) {
            Instant now = Instant.now();
            return new MetricsView(now, intervalMillis, List.of());
        }

        long effectiveWindow = Math.max(intervalMillis, 1L);
        Map<String, MetricFamilyBuilder> builders = new LinkedHashMap<>();
        for (NBMetric metric : metrics) {
            MetricClassification classification = classify(metric);
            String familyName = sanitizeMetricName(classification.baseName(), classification.type());
            String familyKey = classification.type().name() + "#" + familyName;

            MetricFamilyBuilder builder = builders.computeIfAbsent(
                familyKey,
                ignored -> new MetricFamilyBuilder(
                    familyName,
                    classification.originalName(),
                    classification.type(),
                    classification.unit(),
                    classification.description(),
                    classification.categories()
                )
            );

            builder.addSample(createSample(metric, classification, familyName, effectiveWindow));
        }

        List<MetricFamily> families = new ArrayList<>(builders.size());
        builders.values().forEach(builder -> families.add(builder.build()));

        Instant captureTime = Instant.now();
        return new MetricsView(captureTime, intervalMillis, families);
    }

    public static MetricsView combine(Collection<MetricsView> views) {
        if (views.isEmpty()) {
            Instant now = Instant.now();
            return new MetricsView(now, 0L, List.of());
        }

        List<MetricsView> ordered = new ArrayList<>(views);
        ordered.sort(Comparator.comparing(MetricsView::capturedAt));

        Instant captureTime = ordered.get(ordered.size() - 1).capturedAt();
        long totalInterval = ordered.stream()
            .mapToLong(MetricsView::intervalMillis)
            .reduce(0L, Long::sum);

        Map<FamilyKey, FamilyAccumulator> familyAccumulators = new LinkedHashMap<>();
        for (MetricsView view : ordered) {
            long viewInterval = view.intervalMillis();
            for (MetricFamily family : view.families()) {
                FamilyKey familyKey = new FamilyKey(
                    family.familyName(),
                    family.originalName(),
                    family.type(),
                    family.unit(),
                    family.help()
                );
                FamilyAccumulator accumulator = familyAccumulators.computeIfAbsent(
                    familyKey,
                    key -> new FamilyAccumulator(familyKey, family.categories())
                );
                accumulator.addFamily(family, viewInterval);
            }
        }

        List<MetricFamily> aggregatedFamilies = new ArrayList<>(familyAccumulators.size());
        for (FamilyAccumulator accumulator : familyAccumulators.values()) {
            aggregatedFamilies.add(accumulator.build());
        }

        return new MetricsView(captureTime, totalInterval, aggregatedFamilies);
    }

    private static MetricClassification classify(NBMetric metric) {
        NBLabels labels = metric.getLabels();
        String rawName = labels.valueOfOptional("name").orElse(metric.getHandle());
        String description = Optional.ofNullable(metric.getDescription()).orElse("");
        String unit = labels.valueOfOptional("unit").orElse("");
        MetricCategory[] categories = metric.getCategories();
        MetricType type;

        if (metric instanceof NBMetricCounter) {
            type = MetricType.COUNTER;
        } else if (metric instanceof NBMetricGauge) {
            type = MetricType.GAUGE;
        } else if (metric instanceof NBMetricHistogram) {
            type = MetricType.SUMMARY;
        } else if (metric instanceof NBMetricTimer) {
            type = MetricType.SUMMARY;
        } else if (metric instanceof NBMetricMeter) {
            type = MetricType.GAUGE;
        } else {
            type = MetricType.UNKNOWN;
        }

        return new MetricClassification(metric, rawName, rawName, description, unit, type,
            (categories == null) ? List.of() : List.of(categories));
    }

    private static Sample createSample(NBMetric metric,
                                       MetricClassification classification,
                                       String familyName,
                                       long cacheWindowMillis) {
        return switch (classification.type()) {
            case COUNTER -> createCounterSample((NBMetricCounter) metric, classification, familyName);
            case GAUGE -> createGaugeSample(metric, classification, familyName);
            case SUMMARY -> createSummarySample(metric, classification, familyName, cacheWindowMillis);
            case HISTOGRAM -> throw new UnsupportedOperationException("Histogram type is not supported yet");
            case UNKNOWN -> createUnknownSample(metric, classification, familyName);
        };
    }

    private static Sample createCounterSample(NBMetricCounter counter,
                                              MetricClassification classification,
                                              String familyName) {
        NBLabels labels = filteredLabels(counter.getLabels());
        return new PointSample(familyName, labels, counter.getCount());
    }

    private static Sample createGaugeSample(NBMetric metric,
                                            MetricClassification classification,
                                            String familyName) {
        NBLabels labels = filteredLabels(metric.getLabels());
        if (metric instanceof NBMetricGauge gauge) {
            double value = Optional.ofNullable(gauge.getValue()).orElse(Double.NaN);
            return new PointSample(familyName, labels, value);
        } else if (metric instanceof NBMetricMeter meter) {
            return new MeterSample(
                familyName,
                labels,
                meter.getCount(),
                meter.getMeanRate(),
                meter.getOneMinuteRate(),
                meter.getFiveMinuteRate(),
                meter.getFifteenMinuteRate()
            );
        } else {
            throw new IllegalArgumentException("Unsupported gauge metric type: " + metric.getClass().getCanonicalName());
        }
    }

    private static Sample createSummarySample(NBMetric metric,
                                              MetricClassification classification,
                                              String familyName,
                                              long cacheWindowMillis) {
        NBLabels labels = filteredLabels(metric.getLabels());
        ConvenientSnapshot snapshot;
        long observationCount;
        SummaryStatistics stats;
        LinkedHashMap<Double, Double> quantiles = new LinkedHashMap<>(DEFAULT_QUANTILES.size());

        if (metric instanceof NBMetricHistogram histogram) {
            snapshot = histogram.getDeltaSnapshot(cacheWindowMillis);
            observationCount = snapshot.size();
        } else if (metric instanceof NBMetricTimer timer) {
            snapshot = timer.getDeltaSnapshot(cacheWindowMillis);
            observationCount = snapshot.size();
        } else {
            throw new IllegalArgumentException("Summary metrics must be histogram or timer instances.");
        }

        stats = new SummaryStatistics(
            observationCount,
            snapshot.getMin(),
            snapshot.getMax(),
            snapshot.getMean(),
            snapshot.getStdDev()
        );

        DEFAULT_QUANTILES.forEach(q -> quantiles.put(q, snapshot.getValue(q)));
        double sum = stats.count() * stats.mean();

        RateStatistics rates = null;
        if (metric instanceof NBMetricTimer timer) {
            rates = new RateStatistics(
                timer.getMeanRate(),
                timer.getOneMinuteRate(),
                timer.getFiveMinuteRate(),
                timer.getFifteenMinuteRate()
            );
        }

        return new SummarySample(
            familyName,
            labels,
            sum,
            stats,
            quantiles,
            rates,
            snapshot
        );
    }

    private static Sample createUnknownSample(NBMetric metric,
                                              MetricClassification classification,
                                              String familyName) {
        NBLabels labels = filteredLabels(metric.getLabels());
        return new PointSample(familyName, labels, Double.NaN);
    }

    private static NBLabels filteredLabels(NBLabels labels) {
        return labels;
    }

    private static String sanitizeMetricName(String rawName, MetricType type) {
        String sanitized = Optional.ofNullable(rawName).orElse("unnamed");
        sanitized = sanitized.trim();
        if (sanitized.isEmpty()) {
            sanitized = "unnamed_metric";
        }

        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_:]", "_");
        if (!VALID_IDENTIFIER.matcher(sanitized).matches()) {
            if (!sanitized.isEmpty() && Character.isDigit(sanitized.charAt(0))) {
                sanitized = "_" + sanitized;
            }
            sanitized = sanitized.replaceAll("[^a-zA-Z0-9_:]", "_");
        }

        if (type == MetricType.COUNTER && !sanitized.endsWith("_total")) {
            sanitized = sanitized + "_total";
        }

        return sanitized;
    }

    public enum MetricType {
        COUNTER,
        GAUGE,
        SUMMARY,
        HISTOGRAM,
        UNKNOWN
    }

    public record MetricFamily(
        String familyName,
        String originalName,
        MetricType type,
        String unit,
        String help,
        List<MetricCategory> categories,
        List<Sample> samples
    ) {
        public MetricFamily {
            Objects.requireNonNull(familyName, "familyName");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(samples, "samples");
            categories = (categories == null) ? List.of() : List.copyOf(categories);
            samples = List.copyOf(samples);
        }

        public boolean isEmpty() {
            return samples.isEmpty();
        }
    }

    public sealed interface Sample permits PointSample, MeterSample, SummarySample {
        String sampleName();

        NBLabels labels();
    }

    public record PointSample(String sampleName, NBLabels labels, double value) implements Sample {
        public PointSample {
            Objects.requireNonNull(sampleName, "sampleName");
            Objects.requireNonNull(labels, "labels");
        }
    }

    public record MeterSample(
        String sampleName,
        NBLabels labels,
        long count,
        double meanRate,
        double oneMinuteRate,
        double fiveMinuteRate,
        double fifteenMinuteRate
    ) implements Sample {
        public MeterSample {
            Objects.requireNonNull(sampleName, "sampleName");
            Objects.requireNonNull(labels, "labels");
        }
    }

    public record SummarySample(
        String sampleName,
        NBLabels labels,
        double sum,
        SummaryStatistics statistics,
        Map<Double, Double> quantiles,
        RateStatistics rates,
        ConvenientSnapshot snapshot
    ) implements Sample {
        public SummarySample {
            Objects.requireNonNull(sampleName, "sampleName");
            Objects.requireNonNull(labels, "labels");
            Objects.requireNonNull(statistics, "statistics");
            quantiles = Map.copyOf(quantiles);
            Objects.requireNonNull(snapshot, "snapshot");
        }
    }

    public record SummaryStatistics(long count, double min, double max, double mean, double stddev) {
    }

    public record RateStatistics(double mean, double oneMinute, double fiveMinute, double fifteenMinute) {
    }

    private record MetricClassification(
        NBMetric metric,
        String baseName,
        String originalName,
        String description,
        String unit,
        MetricType type,
        List<MetricCategory> categories
    ) {
    }

    private static final class MetricFamilyBuilder {
        private final String familyName;
        private final String originalName;
        private final MetricType type;
        private final String unit;
        private final String help;
        private final List<MetricCategory> categories;
        private final Deque<Sample> samples = new ArrayDeque<>();

        private MetricFamilyBuilder(String familyName,
                                    String originalName,
                                    MetricType type,
                                    String unit,
                                    String help,
                                    List<MetricCategory> categories) {
            this.familyName = familyName;
            this.originalName = originalName;
            this.type = type;
            this.unit = unit;
            this.help = Optional.ofNullable(help).orElse("");
            this.categories = (categories == null) ? List.of() : List.copyOf(categories);
        }

        private void addSample(Sample sample) {
            samples.add(sample);
        }

        private MetricFamily build() {
            return new MetricFamily(
                familyName,
                originalName,
                type,
                unit,
                help,
                categories,
                new ArrayList<>(samples)
            );
        }
    }

    private record FamilyKey(String familyName, String originalName, MetricType type, String unit, String help) {
    }

    private static final class FamilyAccumulator {
        private final FamilyKey key;
        private final List<MetricCategory> categories;
        private final Map<SampleKey, SampleAccumulator> sampleAccumulators = new LinkedHashMap<>();

        private FamilyAccumulator(FamilyKey key, List<MetricCategory> categories) {
            this.key = key;
            this.categories = (categories == null) ? List.of() : List.copyOf(categories);
        }

        private void addFamily(MetricFamily family, long viewInterval) {
            for (Sample sample : family.samples()) {
                SampleKey sampleKey = new SampleKey(sample.sampleName(), sample.labels(), sample.labels().linearizeAsMetrics());
                SampleAccumulator accumulator = sampleAccumulators.computeIfAbsent(
                    sampleKey,
                    key -> SampleAccumulator.create(sample, this.key.type)
                );
                accumulator.addSample(sample, viewInterval);
            }
        }

        private MetricFamily build() {
            List<Sample> combinedSamples = new ArrayList<>(sampleAccumulators.size());
            for (SampleAccumulator accumulator : sampleAccumulators.values()) {
                combinedSamples.add(accumulator.build());
            }
            return new MetricFamily(
                key.familyName(),
                key.originalName(),
                key.type(),
                key.unit(),
                key.help(),
                categories,
                combinedSamples
            );
        }
    }

    private record SampleKey(String sampleName, NBLabels labels, String labelKey) {
        private SampleKey {
            Objects.requireNonNull(sampleName, "sampleName");
            Objects.requireNonNull(labels, "labels");
            String key = (labelKey != null) ? labelKey : labels.linearizeAsMetrics();
            labelKey = key;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SampleKey other)) return false;
            return sampleName.equals(other.sampleName) && labelKey.equals(other.labelKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sampleName, labelKey);
        }
    }

    private interface SampleAccumulator {
        void addSample(Sample sample, long viewInterval);

        Sample build();

        static SampleAccumulator create(Sample sample, MetricType familyType) {
            if (sample instanceof PointSample pointSample) {
                if (familyType == MetricType.COUNTER) {
                    return new CounterSampleAccumulator(pointSample.sampleName(), pointSample.labels());
                } else {
                    return new PointSampleAccumulator(pointSample.sampleName(), pointSample.labels());
                }
            } else if (sample instanceof MeterSample meterSample) {
                return new MeterSampleAccumulator(meterSample.sampleName(), meterSample.labels());
            } else if (sample instanceof SummarySample summarySample) {
                return new SummarySampleAccumulator(summarySample.sampleName(), summarySample.labels());
            } else {
                throw new IllegalArgumentException("Unsupported sample type: " + sample.getClass().getSimpleName());
            }
        }
    }

    private static final class CounterSampleAccumulator implements SampleAccumulator {
        private final String sampleName;
        private final NBLabels labels;
        private double total = 0.0d;

        private CounterSampleAccumulator(String sampleName, NBLabels labels) {
            this.sampleName = sampleName;
            this.labels = labels;
        }

        @Override
        public void addSample(Sample sample, long viewInterval) {
            if (!(sample instanceof PointSample pointSample)) {
                throw new IllegalArgumentException("Expected PointSample but found " + sample.getClass().getSimpleName());
            }
            if (Double.isFinite(pointSample.value())) {
                total += pointSample.value();
            }
        }

        @Override
        public Sample build() {
            return new PointSample(sampleName, labels, total);
        }
    }

    private static final class PointSampleAccumulator implements SampleAccumulator {
        private final String sampleName;
        private final NBLabels labels;
        private double weightedSum = 0.0d;
        private double weightTotal = 0.0d;

        private PointSampleAccumulator(String sampleName, NBLabels labels) {
            this.sampleName = sampleName;
            this.labels = labels;
        }

        @Override
        public void addSample(Sample sample, long viewInterval) {
            if (!(sample instanceof PointSample pointSample)) {
                throw new IllegalArgumentException("Expected PointSample but found " + sample.getClass().getSimpleName());
            }
            double weight = (viewInterval > 0L) ? viewInterval : 1.0d;
            if (Double.isFinite(pointSample.value())) {
                weightedSum += pointSample.value() * weight;
                weightTotal += weight;
            }
        }

        @Override
        public Sample build() {
            double value = (weightTotal > 0.0d) ? weightedSum / weightTotal : Double.NaN;
            return new PointSample(sampleName, labels, value);
        }
    }

    private static final class MeterSampleAccumulator implements SampleAccumulator {
        private final String sampleName;
        private final NBLabels labels;
        private long countSum = 0L;
        private double meanRateWeighted = 0.0d;
        private double oneMinuteWeighted = 0.0d;
        private double fiveMinuteWeighted = 0.0d;
        private double fifteenMinuteWeighted = 0.0d;
        private double weightTotal = 0.0d;

        private MeterSampleAccumulator(String sampleName, NBLabels labels) {
            this.sampleName = sampleName;
            this.labels = labels;
        }

        @Override
        public void addSample(Sample sample, long viewInterval) {
            if (!(sample instanceof MeterSample meterSample)) {
                throw new IllegalArgumentException("Expected MeterSample but found " + sample.getClass().getSimpleName());
            }
            double weight = (viewInterval > 0L) ? viewInterval : 1.0d;
            countSum += meterSample.count();
            meanRateWeighted += meterSample.meanRate() * weight;
            oneMinuteWeighted += meterSample.oneMinuteRate() * weight;
            fiveMinuteWeighted += meterSample.fiveMinuteRate() * weight;
            fifteenMinuteWeighted += meterSample.fifteenMinuteRate() * weight;
            weightTotal += weight;
        }

        @Override
        public Sample build() {
            double divisor = (weightTotal > 0.0d) ? weightTotal : 1.0d;
            return new MeterSample(
                sampleName,
                labels,
                countSum,
                meanRateWeighted / divisor,
                oneMinuteWeighted / divisor,
                fiveMinuteWeighted / divisor,
                fifteenMinuteWeighted / divisor
            );
        }
    }

    private static final class SummarySampleAccumulator implements SampleAccumulator {
        private final String sampleName;
        private final NBLabels labels;
        private long totalCount = 0L;
        private double totalSum = 0.0d;
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;
        private double sumSquares = 0.0d;
        private final Map<Double, WeightedValue> quantileWeights = new TreeMap<>();
        private double rateWeight = 0.0d;
        private double meanRateWeighted = 0.0d;
        private double oneMinuteWeighted = 0.0d;
        private double fiveMinuteWeighted = 0.0d;
        private double fifteenMinuteWeighted = 0.0d;

        private SummarySampleAccumulator(String sampleName, NBLabels labels) {
            this.sampleName = sampleName;
            this.labels = labels;
        }

        @Override
        public void addSample(Sample sample, long viewInterval) {
            if (!(sample instanceof SummarySample summarySample)) {
                throw new IllegalArgumentException("Expected SummarySample but found " + sample.getClass().getSimpleName());
            }
            SummaryStatistics stats = summarySample.statistics();
            long count = stats.count();
            double mean = stats.mean();
            double variance = stats.stddev() * stats.stddev();
            double sampleSumSquares = count * (variance + (mean * mean));
            totalCount += count;
            totalSum += summarySample.sum();
            min = Math.min(min, stats.min());
            max = Math.max(max, stats.max());
            sumSquares += sampleSumSquares;

            for (Entry<Double, Double> entry : summarySample.quantiles().entrySet()) {
                double quantile = entry.getKey();
                double value = entry.getValue();
                double weight = (count > 0L) ? count : 1.0d;
                quantileWeights.computeIfAbsent(quantile, q -> new WeightedValue()).add(value, weight);
            }

            RateStatistics rates = summarySample.rates();
            if (rates != null) {
                double weight = (viewInterval > 0L) ? viewInterval : 1.0d;
                rateWeight += weight;
                meanRateWeighted += rates.mean() * weight;
                oneMinuteWeighted += rates.oneMinute() * weight;
                fiveMinuteWeighted += rates.fiveMinute() * weight;
                fifteenMinuteWeighted += rates.fifteenMinute() * weight;
            }
        }

        @Override
        public Sample build() {
            double effectiveCount = (totalCount > 0L) ? totalCount : 1.0d;
            double mean = totalSum / effectiveCount;
            double meanSquare = mean * mean;
            double meanOfSquares = sumSquares / effectiveCount;
            double variance = Math.max(0.0d, meanOfSquares - meanSquare);
            double stddev = Math.sqrt(variance);
            double resolvedMin = (min == Double.POSITIVE_INFINITY) ? Double.NaN : min;
            double resolvedMax = (max == Double.NEGATIVE_INFINITY) ? Double.NaN : max;

            SummaryStatistics aggregatedStats = new SummaryStatistics(totalCount, resolvedMin, resolvedMax, mean, stddev);

            Map<Double, Double> aggregatedQuantiles = new LinkedHashMap<>();
            for (Entry<Double, WeightedValue> entry : quantileWeights.entrySet()) {
                aggregatedQuantiles.put(entry.getKey(), entry.getValue().value());
            }

            RateStatistics aggregatedRates = null;
            if (rateWeight > 0.0d) {
                double divisor = rateWeight;
                aggregatedRates = new RateStatistics(
                    meanRateWeighted / divisor,
                    oneMinuteWeighted / divisor,
                    fiveMinuteWeighted / divisor,
                    fifteenMinuteWeighted / divisor
                );
            }

            ConvenientSnapshot snapshot = new ConvenientSnapshot(new AggregatedSnapshot(aggregatedStats, aggregatedQuantiles));

            return new SummarySample(
                sampleName,
                labels,
                totalSum,
                aggregatedStats,
                aggregatedQuantiles,
                aggregatedRates,
                snapshot
            );
        }
    }

    private static final class WeightedValue {
        private double weightedSum = 0.0d;
        private double weightTotal = 0.0d;

        private void add(double value, double weight) {
            if (!Double.isFinite(value)) {
                return;
            }
            weightedSum += value * weight;
            weightTotal += weight;
        }

        private double value() {
            return (weightTotal > 0.0d) ? (weightedSum / weightTotal) : Double.NaN;
        }
    }

    private static final class AggregatedSnapshot extends com.codahale.metrics.Snapshot {
        private final SummaryStatistics stats;
        private final NavigableMap<Double, Double> quantiles;

        private AggregatedSnapshot(SummaryStatistics stats, Map<Double, Double> quantiles) {
            this.stats = stats;
            this.quantiles = new TreeMap<>(quantiles);
        }

        @Override
        public double getValue(double quantile) {
            if (quantiles.isEmpty()) {
                return stats.mean();
            }
            Double floorKey = quantiles.floorKey(quantile);
            Double ceilingKey = quantiles.ceilingKey(quantile);
            if (floorKey == null) {
                return quantiles.firstEntry().getValue();
            }
            if (ceilingKey == null) {
                return quantiles.lastEntry().getValue();
            }
            if (Objects.equals(floorKey, ceilingKey)) {
                return quantiles.get(floorKey);
            }
            double lowerValue = quantiles.get(floorKey);
            double upperValue = quantiles.get(ceilingKey);
            double range = ceilingKey - floorKey;
            double position = (quantile - floorKey) / range;
            return lowerValue + (position * (upperValue - lowerValue));
        }

        @Override
        public long[] getValues() {
            return new long[0];
        }

        @Override
        public int size() {
            return (int) Math.min(Integer.MAX_VALUE, stats.count());
        }

        @Override
        public long getMax() {
            return (long) Math.round(stats.max());
        }

        @Override
        public double getMean() {
            return stats.mean();
        }

        @Override
        public long getMin() {
            return (long) Math.round(stats.min());
        }

        @Override
        public double getStdDev() {
            return stats.stddev();
        }

        @Override
        public void dump(java.io.OutputStream output) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(output)) {
                for (Entry<Double, Double> entry : quantiles.entrySet()) {
                    writer.printf("%f=%f%n", entry.getKey(), entry.getValue());
                }
            }
        }

        @Override
        public double getMedian() {
            return getValue(0.5d);
        }

        @Override
        public double get75thPercentile() {
            return getValue(0.75d);
        }

        @Override
        public double get95thPercentile() {
            return getValue(0.95d);
        }

        @Override
        public double get98thPercentile() {
            return getValue(0.98d);
        }

        @Override
        public double get99thPercentile() {
            return getValue(0.99d);
        }

        @Override
        public double get999thPercentile() {
            return getValue(0.999d);
        }
    }
}
