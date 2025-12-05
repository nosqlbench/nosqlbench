---
title: "SQLite Metrics Reference"
description: "Schema and usage guide for the SQLite metrics reporters."
audience: developer
diataxis: reference
tags:
  - metrics
  - sqlite
component: core
topic: metrics
status: live
owner: "@nosqlbench/metrics"
generated: false
---

# SQLite Metrics Reference

## What Gets Written
- Every session writes a 30s cadence snapshot to `logs/<session>_metrics.db`. A symlink named `logs/metrics.db` points to the live session file.
- Additional reporters can be enabled via `--report-sqlite-to <file>[,<filter>][,<interval>]`; they run alongside the default session reporter.
- Values are stored in an OpenMetrics-aligned schema so a single metric family can hold multiple sample types (counter totals, gauge values, histogram summaries, meter rates).

Run `sqlite3 logs/metrics.db` to open the live database, or point the tool at any snapshot you created with `--report-sqlite-to`.

## Schema at a Glance
```
metric_family (id, name, help, unit, type)
    └─< sample_name (id, metric_family_id, sample)
         └─< sample_value (id, sample_name_id, label_set_id, timestamp_ms, value, count, sum, min, max, mean, stddev)
              ├─< sample_quantile (sample_value_id, quantile, quantile_value)
              ├─< sample_rate (sample_value_id, rate_type, rate_value)
              └─< sample_histogram (sample_value_id, start_seconds, interval_seconds, max_value, histogram_base64)

label_key (id, name) ─┐
                      ├─< label_set_membership (label_set_id, label_key_id, label_value_id)
label_value (id, value)┘
    ↑
label_set (id, hash)
```

### Table Roles
- `metric_family`: One row per logical metric (`name`, optional `unit`, `help`, and `type`: COUNTER, GAUGE, SUMMARY, TIMER, etc.).
- `sample_name`: A family can expose several samples (`counter_metric_total`, `latency_seconds`, `latency_seconds_created`). These are the identifiers you filter on in queries.
- `sample_value`: One row per snapshot, per sample name, per unique label set. `value` holds the primary reading (counter total, gauge reading, histogram count). Summary metrics also populate `count`, `sum`, `min`, `max`, `mean`, `stddev`.
- `sample_quantile`: Quantile buckets emitted for summary/timer samples (p50, p95, etc.).
- `sample_rate`: Additional rates such as `mean`, `m1`, `m5`, `m15` for timers/meters.
- `sample_histogram`: Optional per reporter (off by default). When enabled, stores the base64 payload produced by `HistogramLogWriter` plus timing metadata so you can rebuild HDR log lines later.
- `label_key`, `label_value`, `label_set`, `label_set_membership`: Normalised label storage. `label_set.hash` is a deterministic encoding of the label map; join through `label_set_membership` to recover key/value pairs.

Enable histogram persistence by adding `histograms` to the SQLite reporter specification, for example:

```bash
nb5 --report-sqlite-to logs/custom_metrics.db,,10s,histograms …
```

The auto-created session reporter honours the component property `sqlite_histograms=true` (or `metrics.sqlite.histograms=true`) when you want histograms captured in the default `logs/<session>_metrics.db`.


## Quick Inventory Commands
```bash
# List tables
sqlite3 logs/metrics.db '.tables'

# Show schema for a table
sqlite3 logs/metrics.db '.schema sample_value'

# Enable readable output while exploring interactively
sqlite3 logs/metrics.db <<'EOSQL'
.headers on
.mode column
SELECT id, name, type, unit FROM metric_family ORDER BY name LIMIT 10;
EOSQL
```

## Practical Query Patterns

### 1. Discover Metric Families and Their Samples
Identify the metric families recorded in a run and the sample names they expose.

```bash
sqlite3 logs/metrics.db <<'SQL'
.headers on
.mode column
SELECT mf.name AS family_name,
       mf.type AS type,
       GROUP_CONCAT(sn.sample, ', ') AS samples
FROM metric_family mf
JOIN sample_name sn ON sn.metric_family_id = mf.id
GROUP BY mf.id
ORDER BY mf.name;
SQL
```

### 2. Time-Series for a Specific Metric
Fetch counter totals for a metric, including labels, ordered by capture time.

```bash
sqlite3 logs/metrics.db <<'SQL'
.headers on
.mode column
WITH labeled_samples AS (
  SELECT sv.id,
         datetime(sv.timestamp_ms / 1000, 'unixepoch') AS captured_at,
         sv.value,
         GROUP_CONCAT(lk.name || '=' || lv.value, ', ') AS labels
  FROM sample_value sv
  JOIN sample_name sn ON sn.id = sv.sample_name_id
  JOIN label_set ls ON ls.id = sv.label_set_id
  LEFT JOIN label_set_membership lsm ON lsm.label_set_id = ls.id
  LEFT JOIN label_key lk ON lk.id = lsm.label_key_id
  LEFT JOIN label_value lv ON lv.id = lsm.label_value_id
  WHERE sn.sample = 'counter_metric_total'
  GROUP BY sv.id
)
SELECT captured_at, value AS total, labels
FROM labeled_samples
ORDER BY captured_at;
SQL
```

### 3. Filter by Label Values
Narrow a query to only the rows that contain specific labels (e.g., one activity).

```bash
sqlite3 logs/metrics.db <<'SQL'
.headers on
.mode column
SELECT datetime(sv.timestamp_ms / 1000, 'unixepoch') AS captured_at,
       sv.value AS ops_total
FROM sample_value sv
JOIN sample_name sn ON sn.id = sv.sample_name_id
JOIN label_set_membership lsm ON lsm.label_set_id = sv.label_set_id
JOIN label_key lk ON lk.id = lsm.label_key_id
JOIN label_value lv ON lv.id = lsm.label_value_id
WHERE sn.sample = 'activity_ops_total'
  AND lk.name = 'activity'
  AND lv.value = 'write'
ORDER BY sv.timestamp_ms;
SQL
```

### 4. Extract Latency Quantiles from Summaries/Timers
Summaries and timers populate the `sample_quantile` table alongside aggregate statistics.

```bash
sqlite3 logs/metrics.db <<'SQL'
.headers on
.mode column
SELECT datetime(sv.timestamp_ms / 1000, 'unixepoch') AS captured_at,
       sq.quantile,
       sq.quantile_value AS latency_ms
FROM sample_quantile sq
JOIN sample_value sv ON sv.id = sq.sample_value_id
JOIN sample_name sn ON sn.id = sv.sample_name_id
WHERE sn.sample = 'op_latency_ms'
ORDER BY sv.timestamp_ms, sq.quantile;
SQL
```

### 5. Inspect Meter/Timer Rates
For meter-style metrics (e.g., throughput), `sample_rate` holds mean and exponentially weighted moving averages.

```bash
sqlite3 logs/metrics.db <<'SQL'
.headers on
.mode column
SELECT datetime(sv.timestamp_ms / 1000, 'unixepoch') AS captured_at,
       sr.rate_type,
       sr.rate_value
FROM sample_rate sr
JOIN sample_value sv ON sv.id = sr.sample_value_id
JOIN sample_name sn ON sn.id = sv.sample_name_id
WHERE sn.sample = 'activity_throughput'
ORDER BY sv.timestamp_ms, sr.rate_type;
SQL
```

## Tips
- Snapshot files are append-only. Rotate or copy them before deleting if you plan to run multiple sessions.
- Build views for frequently-used metrics: `CREATE VIEW latency_p95 AS ...` can simplify repeated analyses.
- The default cadence is 30 s; custom reporters inherit whatever interval you provide. Shorter cadences produce more granular time-series at the cost of larger databases.
- Use `--report-sqlite-to` filters (e.g., `name=op_latency_ms;activity=write`) to keep databases focused on the metrics you care about.

With these patterns you can combine NoSQLBench metrics in SQL tooling, spreadsheets, or BI systems without bespoke exporters.
