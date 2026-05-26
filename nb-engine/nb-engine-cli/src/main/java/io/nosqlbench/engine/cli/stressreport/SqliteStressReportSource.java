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

import io.nosqlbench.nb.mql.commands.InstantCommand;
import io.nosqlbench.nb.mql.commands.QuantileCommand;
import io.nosqlbench.nb.mql.commands.StatsCommand;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/// Reads a session's final state from the SQLite database written by
/// [io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter].
///
/// Uses the project's MQL command layer ([InstantCommand], [QuantileCommand]) rather than
/// hand-rolled SQL, with one targeted direct query to enumerate distinct `op` label values
/// (the commands return labels as a GROUP_CONCAT'd string, which would otherwise have to be
/// parsed back out).
///
/// **Latency-instrument auto-switch.** If any `cycles_responsetime` instance exists in the
/// database, a cycle rate limiter was active during the run and the report should reflect
/// response-time latencies (coordinated-omission corrected). Otherwise we use service-time.
/// This mirrors cassandra-stress's `serviceTime` (free-rate) vs `responseTime` (rate-fixed)
/// behavior.
///
/// **max latency gap.** The SQLite reporter does not store `max` for timers unless
/// `--sqlite-histograms` is enabled (it writes only quantiles, count, and rates — see
/// [io.nosqlbench.nb.api.engine.metrics.reporters.SqliteSnapshotReporter#writeSummaryDetails]).
/// We report `Double.NaN` for max in that case and set [SessionSummary#maxAvailable] to false
/// so the renderer can show `n/a` with an explanatory callout.
public final class SqliteStressReportSource implements StressReportSource {

    private final Path dbPath;

    public SqliteStressReportSource(Path dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public String sourceDescription() {
        return "sqlite: " + dbPath.toAbsolutePath();
    }

    @Override
    public SessionSummary readSummary() throws Exception {
        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {

            // Per-op breakdown comes from the per-op timer families nb-engine creates when
            // `instrument: true` is set on a block: `successfor_<opname>` and `errorsfor_<opname>`.
            // Those carry an `op=<opname>` label (added automatically by ParsedOp). If those
            // are absent, the session lacks per-op instrumentation and there is nothing op-
            // keyed to report — we still synthesise a single `total` row from the per-activity
            // `result` / `result_success` / cycles_*timer metrics so the report is useful.
            List<String> opNames = discoverInstrumentedOps(conn);
            boolean perOp = !opNames.isEmpty();

            boolean rateLimited = sampleNameExists(conn, "cycles_responsetime");
            String latencyInstrument;
            if (perOp) {
                // Per-op timers conflate service/response time — there's only one timer per op.
                // Label the source honestly.
                latencyInstrument = "successfor_<op>";
            } else {
                latencyInstrument = rateLimited ? "cycles_responsetime" : "cycles_servicetime";
            }

            String sessionLabel = readSingleLabelValue(conn, "session");
            long runtimeMs = readSessionRuntimeMs(conn);

            Map<String, OpSummary> byOp = new TreeMap<>();
            if (perOp) {
                for (String op : opNames) {
                    byOp.put(op, readPerOpSummary(conn, op));
                }
            } else {
                OpSummary total = readActivityTotal(conn, latencyInstrument);
                if (total != null) {
                    byOp.put("total", total);
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
    }

    /// List the distinct `op` label values that appear on per-op `successfor_*` timer families.
    /// Returns empty when the session was not configured with `instrument: true`.
    private List<String> discoverInstrumentedOps(Connection conn) throws Exception {
        String sql = """
            SELECT DISTINCT lv.value
            FROM sample_name sn
            JOIN metric_instance mi ON mi.sample_name_id = sn.id
            JOIN label_set_membership lsm ON mi.label_set_id = lsm.label_set_id
            JOIN label_key lk ON lsm.label_key_id = lk.id
            JOIN label_value lv ON lsm.label_value_id = lv.id
            WHERE sn.sample LIKE 'successfor_%'
              AND lk.name = 'op'
            ORDER BY lv.value
            """;
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getString(1));
            }
        }
        return out;
    }

    private OpSummary readPerOpSummary(Connection conn, String op) throws Exception {
        String successMetric = "successfor_" + op;
        String errorMetric = "errorsfor_" + op;

        long successCount = readInstantCount(conn, successMetric);
        long errorCount = readInstantCount(conn, errorMetric);
        Map<Double, Double> qs = new HashMap<>();
        for (double q : new double[]{0.5, 0.95, 0.99, 0.999}) {
            qs.put(q, readQuantile(conn, successMetric, op, q));
        }
        double[] stats = readStats(conn, successMetric, op);
        return new OpSummary(
            op,
            successCount + errorCount,
            errorCount,
            stats[2],
            qs.getOrDefault(0.5, Double.NaN),
            qs.getOrDefault(0.95, Double.NaN),
            qs.getOrDefault(0.99, Double.NaN),
            qs.getOrDefault(0.999, Double.NaN),
            stats[1]
        );
    }

    /// Build a single "total" row from per-activity `result` / `result_success` / cycles_*timer
    /// metrics when no per-op instrumentation is present.
    private OpSummary readActivityTotal(Connection conn, String latencyInstrument) throws Exception {
        long resultCount = readInstantCount(conn, "result");
        if (resultCount == 0L) return null;
        long successCount = readInstantCount(conn, "result_success");
        long errors = Math.max(0L, resultCount - successCount);

        Map<Double, Double> qs = new HashMap<>();
        for (double q : new double[]{0.5, 0.95, 0.99, 0.999}) {
            qs.put(q, readQuantileNoFilter(conn, latencyInstrument, q));
        }
        double[] stats = readStatsNoFilter(conn, latencyInstrument);
        return new OpSummary(
            "total",
            resultCount,
            errors,
            stats[2],
            qs.getOrDefault(0.5, Double.NaN),
            qs.getOrDefault(0.95, Double.NaN),
            qs.getOrDefault(0.99, Double.NaN),
            qs.getOrDefault(0.999, Double.NaN),
            stats[1]
        );
    }

    private long readInstantCount(Connection conn, String metric) throws Exception {
        InstantCommand cmd = new InstantCommand();
        QueryResult res = cmd.execute(conn, Map.of("metric", metric));
        long total = 0L;
        for (Map<String, Object> row : res.rows()) {
            Object v = row.get("value");
            if (v instanceof Number n) total += (long) n.doubleValue();
        }
        return total;
    }

    /// Same shape as [#readQuantile(Connection,String,String,double)] but without an op label
    /// filter — used by the activity-total fallback.
    private double readQuantileNoFilter(Connection conn, String metric, double q) throws Exception {
        QuantileCommand cmd = new QuantileCommand();
        QueryResult res = cmd.execute(conn, Map.of("metric", metric, "quantile", q));
        if (res.isEmpty()) return Double.NaN;
        Map<String, Object> row = res.rows().get(res.rows().size() - 1);
        Object qv = row.get("quantile_value");
        if (!(qv instanceof Number n)) return Double.NaN;
        return n.doubleValue() / 1_000_000.0;
    }

    private double[] readStatsNoFilter(Connection conn, String metric) throws Exception {
        StatsCommand cmd = new StatsCommand();
        QueryResult res;
        try {
            res = cmd.execute(conn, Map.of("metric", metric));
        } catch (Exception ex) {
            return new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }
        if (res.isEmpty()) return new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        Map<String, Object> row = res.rows().get(res.rows().size() - 1);
        return new double[]{
            numericOrNaN(row.get("min_value")) / 1_000_000.0,
            numericOrNaN(row.get("max_value")) / 1_000_000.0,
            numericOrNaN(row.get("mean_value")) / 1_000_000.0,
            numericOrNaN(row.get("stddev_value")) / 1_000_000.0
        };
    }

    /// Run [StatsCommand] for the given timer instrument and op, returning
    /// `[min, max, mean, stddev]` in milliseconds. Returns all-NaN if the source has no row
    /// (e.g. database written by an older nb-api version without the `sample_statistics` table).
    private double[] readStats(Connection conn, String metric, String op) throws Exception {
        StatsCommand cmd = new StatsCommand();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("metric", metric);
        params.put("labels", Map.of("op", op));
        QueryResult res;
        try {
            res = cmd.execute(conn, params);
        } catch (Exception ex) {
            // sample_statistics table may be missing in older databases; treat as gap
            return new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }
        if (res.isEmpty()) {
            return new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }
        Map<String, Object> row = res.rows().get(res.rows().size() - 1);
        double min = numericOrNaN(row.get("min_value")) / 1_000_000.0;
        double max = numericOrNaN(row.get("max_value")) / 1_000_000.0;
        double mean = numericOrNaN(row.get("mean_value")) / 1_000_000.0;
        double stddev = numericOrNaN(row.get("stddev_value")) / 1_000_000.0;
        return new double[]{min, max, mean, stddev};
    }

    private static double numericOrNaN(Object v) {
        return (v instanceof Number n) ? n.doubleValue() : Double.NaN;
    }

    /// Run [QuantileCommand] for the given timer instrument and op, returning the quantile value
    /// in milliseconds.
    private double readQuantile(Connection conn, String metric, String op, double q) throws Exception {
        QuantileCommand cmd = new QuantileCommand();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("metric", metric);
        params.put("quantile", q);
        params.put("labels", Map.of("op", op));
        QueryResult res = cmd.execute(conn, params);
        if (res.isEmpty()) return Double.NaN;
        // Pick the latest row (timestamps already filtered to MAX, but rows may differ by labels)
        Map<String, Object> row = res.rows().get(res.rows().size() - 1);
        Object qv = row.get("quantile_value");
        if (!(qv instanceof Number n)) return Double.NaN;
        // Stored value is in source units (nanoseconds for cycles_*timer); convert to ms.
        return n.doubleValue() / 1_000_000.0;
    }

    /// Targeted direct query to test whether the SQLite database contains any sample row for
    /// the given metric family. The MQL command layer would also surface this, but a simple
    /// EXISTS query is unambiguous and lets the latency-instrument auto-switch decision sit
    /// in one place.
    private boolean sampleNameExists(Connection conn, String sampleName) throws Exception {
        String sql = """
            SELECT 1
            FROM sample_name sn
            JOIN metric_instance mi ON mi.sample_name_id = sn.id
            JOIN sample_value sv ON sv.metric_instance_id = mi.id
            WHERE sn.sample = ?
            LIMIT 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sampleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /// Read a single label value (e.g. `session`) from any label set in the database. All
    /// label sets share the foundational session identity, so this is independent of which
    /// instance we look up.
    private String readSingleLabelValue(Connection conn, String labelKey) throws Exception {
        String sql = """
            SELECT lv.value
            FROM label_set_membership lsm
            JOIN label_key lk ON lsm.label_key_id = lk.id
            JOIN label_value lv ON lsm.label_value_id = lv.id
            WHERE lk.name = ?
            LIMIT 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, labelKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /// Read the session runtime from the `session_time` gauge — the last value written.
    private long readSessionRuntimeMs(Connection conn) throws Exception {
        InstantCommand cmd = new InstantCommand();
        QueryResult res = cmd.execute(conn, Map.of("metric", "session_time"));
        if (res.isEmpty()) return 0L;
        Object value = res.rows().get(0).get("value");
        return (value instanceof Number n) ? (long) n.doubleValue() : 0L;
    }

}
