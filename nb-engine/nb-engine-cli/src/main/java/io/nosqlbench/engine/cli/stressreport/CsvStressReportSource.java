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

package io.nosqlbench.engine.cli.stressreport;

import io.nosqlbench.nb.api.engine.metrics.reporters.CsvMetricsFilesManifest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/// Reads a session's final state from the per-metric CSV files written by
/// [io.nosqlbench.nb.api.engine.metrics.reporters.CsvReporter].
///
/// File discovery goes through the [CsvMetricsFilesManifest] (canonical labels → file map)
/// rather than parsing filenames. The manifest is the single source of truth.
///
/// **CSV timer schema.** Each timer file has the codahale header
/// `t,count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit,duration_unit`
/// (see [io.nosqlbench.nb.api.engine.metrics.reporters.CsvReporter]). Durations are in
/// nanoseconds; we convert to milliseconds.
///
/// **Last row wins.** We take the last (most recent) row of each timer file as the cumulative
/// final value, matching c-stress's `totalSummaryInterval` semantics.
public final class CsvStressReportSource implements StressReportSource {

    private static final int COL_COUNT = 1;
    private static final int COL_MAX = 2;
    private static final int COL_MEAN = 3;
    private static final int COL_P50 = 6;
    private static final int COL_P95 = 8;
    private static final int COL_P99 = 10;
    private static final int COL_P999 = 11;

    private final Path csvDir;

    public CsvStressReportSource(Path csvDir) {
        this.csvDir = csvDir;
    }

    @Override
    public String sourceDescription() {
        return "csv: " + csvDir.toAbsolutePath();
    }

    @Override
    public SessionSummary readSummary() throws Exception {
        Path manifestPath = csvDir.resolve(CsvMetricsFilesManifest.FILE_NAME);
        List<CsvMetricsFilesManifest.Entry> entries = CsvMetricsFilesManifest.readAll(manifestPath);
        Map<String, CsvMetricsFilesManifest.Entry> folded =
            CsvMetricsFilesManifest.foldByLabelKey(entries);

        boolean rateLimited = hasMetric(folded, "cycles_responsetime");

        String sessionLabel = folded.values().stream()
            .map(e -> e.labels().get("session"))
            .filter(java.util.Objects::nonNull)
            .findFirst().orElse(null);
        long runtimeMs = readGaugeValueLong(folded, "session_time");

        // Look for per-op `successfor_<opname>` timers first. nb-engine emits these when a
        // block has `instrument: true`, and they carry `op=<opname>` labels.
        Map<String, CsvMetricsFilesManifest.Entry> successByOp = entriesByOpForPrefix(folded, "successfor_");
        boolean perOp = !successByOp.isEmpty();

        String latencyInstrument;
        Map<String, OpSummary> byOp = new TreeMap<>();

        if (perOp) {
            latencyInstrument = "successfor_<op>";
            Map<String, CsvMetricsFilesManifest.Entry> errorsByOp = entriesByOpForPrefix(folded, "errorsfor_");
            for (Map.Entry<String, CsvMetricsFilesManifest.Entry> e : successByOp.entrySet()) {
                String op = e.getKey();
                double[] cols = readLastRow(csvDir.resolve(e.getValue().file()));
                long successCount = (cols != null) ? (long) cols[COL_COUNT] : 0L;
                long errorCount = 0L;
                CsvMetricsFilesManifest.Entry errEntry = errorsByOp.get(op);
                if (errEntry != null) {
                    double[] errCols = readLastRow(csvDir.resolve(errEntry.file()));
                    if (errCols != null) errorCount = (long) errCols[COL_COUNT];
                }
                byOp.put(op, opSummaryFromTimerCols(op, successCount + errorCount, errorCount, cols));
            }
        } else {
            // No per-op instrumentation. Fall back to per-activity metrics for a single
            // synthesized `total` row.
            latencyInstrument = rateLimited ? "cycles_responsetime" : "cycles_servicetime";
            CsvMetricsFilesManifest.Entry latencyEntry = firstEntryForMetric(folded, latencyInstrument);
            CsvMetricsFilesManifest.Entry resultEntry = firstEntryForMetric(folded, "result");
            CsvMetricsFilesManifest.Entry successEntry = firstEntryForMetric(folded, "result_success");
            if (resultEntry != null) {
                double[] resultCols = readLastRow(csvDir.resolve(resultEntry.file()));
                long resultCount = (resultCols != null) ? (long) resultCols[COL_COUNT] : 0L;
                long successCount = 0L;
                if (successEntry != null) {
                    double[] sc = readLastRow(csvDir.resolve(successEntry.file()));
                    if (sc != null) successCount = (long) sc[COL_COUNT];
                }
                long errors = Math.max(0L, resultCount - successCount);
                double[] latencyCols = (latencyEntry != null)
                    ? readLastRow(csvDir.resolve(latencyEntry.file()))
                    : null;
                byOp.put("total", opSummaryFromTimerCols("total", resultCount, errors, latencyCols));
            }
        }

        return new SessionSummary(
            sessionLabel,
            runtimeMs,
            rateLimited,
            latencyInstrument,
            true,
            byOp
        );
    }

    private OpSummary opSummaryFromTimerCols(String op, long opCount, long errors, double[] cols) {
        if (cols == null) {
            return new OpSummary(op, opCount, errors,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }
        return new OpSummary(op, opCount, errors,
            nsToMs(cols[COL_MEAN]),
            nsToMs(cols[COL_P50]),
            nsToMs(cols[COL_P95]),
            nsToMs(cols[COL_P99]),
            nsToMs(cols[COL_P999]),
            nsToMs(cols[COL_MAX]));
    }

    /// Find one manifest entry per `op` label value whose metric name starts with the given
    /// prefix (e.g. `successfor_` or `errorsfor_`). Used to enumerate the per-op timer
    /// families nb-engine creates under `instrument: true`.
    private Map<String, CsvMetricsFilesManifest.Entry> entriesByOpForPrefix(
        Map<String, CsvMetricsFilesManifest.Entry> folded, String prefix
    ) {
        Map<String, CsvMetricsFilesManifest.Entry> out = new TreeMap<>();
        for (CsvMetricsFilesManifest.Entry entry : folded.values()) {
            if (entry.metric() == null || !entry.metric().startsWith(prefix)) continue;
            String op = entry.labels().get("op");
            if (op == null) continue;
            out.put(op, entry);
        }
        return out;
    }

    private CsvMetricsFilesManifest.Entry firstEntryForMetric(
        Map<String, CsvMetricsFilesManifest.Entry> folded, String metricName
    ) {
        return folded.values().stream()
            .filter(e -> metricName.equals(e.metric()))
            .findFirst().orElse(null);
    }

    private boolean hasMetric(Map<String, CsvMetricsFilesManifest.Entry> folded, String metric) {
        return folded.values().stream().anyMatch(e -> metric.equals(e.metric()));
    }

    private long readGaugeValueLong(
        Map<String, CsvMetricsFilesManifest.Entry> folded,
        String metricName
    ) throws java.io.IOException {
        for (CsvMetricsFilesManifest.Entry entry : folded.values()) {
            if (!metricName.equals(entry.metric())) continue;
            double[] cols = readLastRow(csvDir.resolve(entry.file()));
            if (cols != null && cols.length > 1) {
                return (long) cols[1];
            }
        }
        return 0L;
    }

    /// Read the last non-header line of a CSV file and return it as a `double[]`. Returns null
    /// if the file is missing or empty (header only).
    private double[] readLastRow(Path file) throws java.io.IOException {
        if (!Files.exists(file)) return null;
        List<String> lines = Files.readAllLines(file);
        if (lines.size() < 2) return null;
        String lastLine = lines.get(lines.size() - 1);
        String[] tokens = lastLine.split(",", -1);
        double[] out = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                out[i] = Double.parseDouble(tokens[i].trim());
            } catch (NumberFormatException nfe) {
                out[i] = Double.NaN;
            }
        }
        return out;
    }

    private static double nsToMs(double ns) {
        if (Double.isNaN(ns)) return Double.NaN;
        return ns / 1_000_000.0;
    }
}
