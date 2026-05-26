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

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/// Pure renderer: [SessionSummary] → text. No source-specific knowledge — both the SQLite and
/// CSV sources produce equivalent [SessionSummary] inputs and the renderer treats them the
/// same way.
///
/// Output layout is modelled after cassandra-stress's `summarise()` block (per-op row table
/// plus a footer with totals), preceded by a callout explaining which latency instrument was
/// used and why. Fields without a c-stress equivalent in the nosqlbench instrumentation
/// (`pk/s`, `row/s`, GC stats, convergence stderr) are omitted; the footer notes the gaps.
public final class StressReportRenderer {

    private static final String ROW_FMT =
        "%-20s %12d %12d %10s %10s %10s %10s %10s %10s%n";
    private static final String HEADER_FMT =
        "%-20s %12s %12s %10s %10s %10s %10s %10s %10s%n";

    public String render(SessionSummary s, String sourceDescription) {
        StringBuilder out = new StringBuilder();

        appendComparabilityWarning(out);
        out.append('\n');
        out.append("=== NoSQLBench stress-report ===\n");
        out.append("Source         : ").append(sourceDescription).append('\n');
        out.append("Session        : ").append(s.sessionLabel() == null ? "(unknown)" : s.sessionLabel()).append('\n');
        out.append("Runtime        : ").append(formatDuration(s.runtimeMs())).append('\n');
        out.append('\n');

        appendLatencySourceCallout(out, s);
        appendOmissionCallout(out, s);
        out.append('\n');

        appendOpsTable(out, s);
        out.append('\n');
        appendFooter(out, s);

        return out.toString();
    }

    /// Prominent warning at the very top of every rendered report. Sets reader expectations:
    /// this output is a *similar-shape* summary, not an equivalent-to-cassandra-stress
    /// benchmark. Differences in driver, threading model, and timing jitter mean numbers
    /// from the two systems are not directly comparable.
    private void appendComparabilityWarning(StringBuilder out) {
        out.append("WARNING: Do not expect comparisons made between this report and cassandra-stress\n");
        out.append("         to be accurate and comparable. The two systems are very different, and\n");
        out.append("         unavoidable factors like driver settings, threading models, and load-sensitive\n");
        out.append("         factors like timing jitter will invariably make these comparisons anecdotal at\n");
        out.append("         best. This report is only an example for users wanting a basic summary.\n");
    }

    /// One-line callout explaining which timer instrument was selected and why. The choice
    /// auto-switches between service-time and response-time based on whether a cycle rate
    /// limiter was active during the run — mirroring cassandra-stress's behavior. When per-op
    /// `successfor_<op>` timers are present (block-level `instrument: true`), latencies come
    /// from those instead and the service/response-time distinction is no longer made.
    private void appendLatencySourceCallout(StringBuilder out, SessionSummary s) {
        boolean perOp = "successfor_<op>".equals(s.latencyInstrument());
        String why;
        if (perOp) {
            why = "per-op timers are emitted because the workload sets `instrument: true`; "
                + "these conflate service-time and response-time (one timer per op) — the cycle "
                + "rate-limiter " + (s.rateLimited() ? "was active" : "was not active")
                + " during this run";
        } else {
            why = s.rateLimited()
                ? "cycle rate limiter was active during the run — response-time latencies include scheduling delay (coordinated-omission corrected), matching cassandra-stress's rate-fixed behavior"
                : "no cycle rate limiter detected — service-time latencies match cassandra-stress's free-rate behavior";
        }
        out.append("*** Latency source: ").append(s.latencyInstrument()).append('\n');
        out.append("    Reason       : ").append(why).append('\n');
    }

    /// Honest callouts for fields the report can't supply.
    private void appendOmissionCallout(StringBuilder out, SessionSummary s) {
        if (!s.maxAvailable()) {
            out.append("    NOTE         : max latency unavailable for this source\n");
        }
        out.append("    NOTE         : rows/partitions/GC stats not modeled in nb metrics — omitted\n");
    }

    private void appendOpsTable(StringBuilder out, SessionSummary s) {
        out.append(String.format(Locale.US, HEADER_FMT,
            "op", "total_ops", "errors", "mean_ms", "med_ms", "p95_ms", "p99_ms", "p999_ms", "max_ms"));
        for (Map.Entry<String, OpSummary> e : s.byOp().entrySet()) {
            OpSummary op = e.getValue();
            out.append(formatRow(op.op(), op));
        }
        OpSummary total = aggregateTotal(s);
        out.append(formatRow("total", total));
    }

    private String formatRow(String label, OpSummary op) {
        return String.format(Locale.US, ROW_FMT,
            label,
            op.opCount(),
            op.errorCount(),
            fmtMs(op.meanMs()),
            fmtMs(op.medianMs()),
            fmtMs(op.p95Ms()),
            fmtMs(op.p99Ms()),
            fmtMs(op.p999Ms()),
            fmtMs(op.maxMs())
        );
    }

    /// Synthesise the `total` row by aggregating per-op rows. Counts/errors sum exactly;
    /// latencies are an op-weighted mean of the per-op values (a fair-enough approximation
    /// when per-op histograms aren't separately mergeable from the snapshot).
    private OpSummary aggregateTotal(SessionSummary s) {
        long ops = 0;
        long errs = 0;
        double weightedMean = 0;
        double weightedMedian = 0;
        double weightedP95 = 0;
        double weightedP99 = 0;
        double weightedP999 = 0;
        double maxLat = Double.NaN;
        long weightSum = 0;
        for (OpSummary o : s.byOp().values()) {
            ops += o.opCount();
            errs += o.errorCount();
            long w = o.opCount();
            if (w > 0) {
                weightSum += w;
                weightedMean += w * safe(o.meanMs());
                weightedMedian += w * safe(o.medianMs());
                weightedP95 += w * safe(o.p95Ms());
                weightedP99 += w * safe(o.p99Ms());
                weightedP999 += w * safe(o.p999Ms());
            }
            if (!Double.isNaN(o.maxMs())) {
                if (Double.isNaN(maxLat) || o.maxMs() > maxLat) maxLat = o.maxMs();
            }
        }
        double divisor = weightSum > 0 ? (double) weightSum : 1.0;
        return new OpSummary(
            "total",
            ops,
            errs,
            weightedMean / divisor,
            weightedMedian / divisor,
            weightedP95 / divisor,
            weightedP99 / divisor,
            weightedP999 / divisor,
            maxLat
        );
    }

    private double safe(double v) {
        return Double.isNaN(v) ? 0.0 : v;
    }

    private void appendFooter(StringBuilder out, SessionSummary s) {
        OpSummary total = aggregateTotal(s);
        double runtimeSec = s.runtimeMs() / 1000.0;
        double opRate = runtimeSec > 0 ? total.opCount() / runtimeSec : 0.0;

        out.append("--- summary ---\n");
        out.append(String.format(Locale.US, "Op rate                   : %,12.0f op/s%n", opRate));
        out.append(String.format(Locale.US, "Latency mean              : %s ms%n", fmtMs(total.meanMs())));
        out.append(String.format(Locale.US, "Latency median            : %s ms%n", fmtMs(total.medianMs())));
        out.append(String.format(Locale.US, "Latency 95th percentile   : %s ms%n", fmtMs(total.p95Ms())));
        out.append(String.format(Locale.US, "Latency 99th percentile   : %s ms%n", fmtMs(total.p99Ms())));
        out.append(String.format(Locale.US, "Latency 99.9th percentile : %s ms%n", fmtMs(total.p999Ms())));
        out.append(String.format(Locale.US, "Latency max               : %s ms%n", fmtMs(total.maxMs())));
        out.append(String.format(Locale.US, "Total operations          : %,12d%n", total.opCount()));
        out.append(String.format(Locale.US, "Total errors              : %,12d%n", total.errorCount()));
        out.append(String.format(Locale.US, "Total operation time      : %s%n", formatDuration(s.runtimeMs())));
    }

    private static String fmtMs(double v) {
        if (Double.isNaN(v)) return "       n/a";
        return String.format(Locale.US, "%10.2f", v);
    }

    private static String formatDuration(long ms) {
        if (ms <= 0) return "0:00:00";
        Duration d = Duration.ofMillis(ms);
        long h = d.toHours();
        long m = d.toMinutesPart();
        long sec = d.toSecondsPart();
        return String.format(Locale.US, "%d:%02d:%02d", h, m, sec);
    }
}
