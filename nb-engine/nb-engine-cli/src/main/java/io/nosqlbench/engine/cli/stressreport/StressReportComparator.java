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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/// A/B equivalence check: compare two [SessionSummary] objects produced from different sources
/// for the same session, and produce a list of fields that disagree beyond tolerance.
///
/// **Tolerances.**
/// - Integer counts (ops, errors): exact match required.
/// - Latency floats: small relative tolerance, both sources must report a value (or both NaN)
///   for that field — otherwise it's a mismatch flagged for diagnosis.
///
/// **Honest gaps.** When one source supplies a value and the other reports NaN (e.g. SQLite
/// without histograms cannot supply `max`), that's intentionally reported as a *mismatch*
/// rather than papered over: it surfaces the coverage difference between sources. The
/// app-level caller can choose how strict to be about that.
public final class StressReportComparator {

    private static final double FLOAT_REL_TOL = 1e-3;

    public ComparisonResult compare(SessionSummary a, SessionSummary b) {
        List<Difference> diffs = new ArrayList<>();

        if (a.rateLimited() != b.rateLimited()) {
            diffs.add(new Difference("session", "rateLimited",
                String.valueOf(a.rateLimited()), String.valueOf(b.rateLimited())));
        }
        if (!safeEquals(a.latencyInstrument(), b.latencyInstrument())) {
            diffs.add(new Difference("session", "latencyInstrument",
                a.latencyInstrument(), b.latencyInstrument()));
        }

        Set<String> ops = new LinkedHashSet<>();
        ops.addAll(a.byOp().keySet());
        ops.addAll(b.byOp().keySet());
        for (String op : ops) {
            OpSummary ao = a.byOp().get(op);
            OpSummary bo = b.byOp().get(op);
            if (ao == null) {
                diffs.add(new Difference(op, "presence", "missing", "present"));
                continue;
            }
            if (bo == null) {
                diffs.add(new Difference(op, "presence", "present", "missing"));
                continue;
            }
            if (ao.opCount() != bo.opCount()) {
                diffs.add(new Difference(op, "opCount",
                    String.valueOf(ao.opCount()), String.valueOf(bo.opCount())));
            }
            if (ao.errorCount() != bo.errorCount()) {
                diffs.add(new Difference(op, "errorCount",
                    String.valueOf(ao.errorCount()), String.valueOf(bo.errorCount())));
            }
            compareFloat(diffs, op, "meanMs", ao.meanMs(), bo.meanMs());
            compareFloat(diffs, op, "medianMs", ao.medianMs(), bo.medianMs());
            compareFloat(diffs, op, "p95Ms", ao.p95Ms(), bo.p95Ms());
            compareFloat(diffs, op, "p99Ms", ao.p99Ms(), bo.p99Ms());
            compareFloat(diffs, op, "p999Ms", ao.p999Ms(), bo.p999Ms());
            compareFloat(diffs, op, "maxMs", ao.maxMs(), bo.maxMs());
        }

        return new ComparisonResult(a.byOp().size(), diffs);
    }

    private void compareFloat(List<Difference> diffs, String op, String field, double av, double bv) {
        boolean aNaN = Double.isNaN(av);
        boolean bNaN = Double.isNaN(bv);
        if (aNaN && bNaN) return;
        if (aNaN ^ bNaN) {
            diffs.add(new Difference(op, field,
                aNaN ? "n/a" : String.format(Locale.US, "%.4f", av),
                bNaN ? "n/a" : String.format(Locale.US, "%.4f", bv)));
            return;
        }
        double scale = Math.max(Math.abs(av), Math.abs(bv));
        double rel = scale == 0.0 ? 0.0 : Math.abs(av - bv) / scale;
        if (rel > FLOAT_REL_TOL) {
            diffs.add(new Difference(op, field,
                String.format(Locale.US, "%.4f", av),
                String.format(Locale.US, "%.4f", bv)));
        }
    }

    private boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public String renderResult(ComparisonResult result, String aDesc, String bDesc) {
        StringBuilder out = new StringBuilder();
        if (result.differences().isEmpty()) {
            out.append(String.format(Locale.US,
                "A/B match: %d op(s) compared, all fields within tolerance (rel=%g).%n",
                result.opsCompared(), FLOAT_REL_TOL));
            return out.toString();
        }
        out.append(String.format(Locale.US,
            "A/B mismatch: %d difference(s) detected (tolerance rel=%g).%n",
            result.differences().size(), FLOAT_REL_TOL));
        out.append("  A = ").append(aDesc).append('\n');
        out.append("  B = ").append(bDesc).append('\n');
        out.append(String.format(Locale.US, "%n  %-16s %-16s %-24s %-24s%n",
            "op", "field", "A", "B"));
        for (Difference d : result.differences()) {
            out.append(String.format(Locale.US, "  %-16s %-16s %-24s %-24s%n",
                d.op(), d.field(), d.aValue(), d.bValue()));
        }
        return out.toString();
    }

    public record Difference(String op, String field, String aValue, String bValue) {
    }

    public record ComparisonResult(int opsCompared, List<Difference> differences) {
        public boolean isMatch() {
            return differences.isEmpty();
        }
    }
}
