# Test Database Documentation

This directory contains pre-generated SQLite metrics databases for testing the MetricsQL query engine. Each database represents a specific scenario with known data patterns.

## Overview

All databases use the NoSQLBench OpenMetrics-aligned schema created by `SqliteSnapshotReporter`. They contain realistic metric data generated using actual metric instruments (`NBMetricCounter`, `NBMetricTimer`).

---

## simple_counter.db

### Purpose
Test basic counter queries, instant values, simple rate calculations, and time-series ranges.

### Schema
- **Metric Family**: `activity_ops`
- **Sample**: `activity_ops_total` (COUNTER)
- **Type**: COUNTER

### Label Dimensions
- `activity`: {read, write}
- `host`: {server1}

### Data Characteristics
- **Snapshots**: 10 (taken ~100ms apart during generation)
- **Label Combinations**: 2 (read, write)
- **Total Samples**: 20
- **Growth Pattern**: Linear increments
  - `activity=read`: increments by 10 per snapshot (0, 10, 20, ..., 90)
  - `activity=write`: increments by 15 per snapshot (0, 15, 30, ..., 135)

### Expected Query Results

**Instant Query (latest value):**
```sql
SELECT sv.value, lv.value as activity
FROM sample_value sv
JOIN metric_instance mi ON sv.metric_instance_id = mi.id
JOIN sample_name sn ON mi.sample_name_id = sn.id
JOIN label_set ls ON mi.label_set_id = ls.id
JOIN label_set_membership lsm ON ls.id = lsm.label_set_id
JOIN label_key lk ON lsm.label_key_id = lk.id
JOIN label_value lv ON lsm.label_value_id = lv.id
WHERE sn.sample = 'activity_ops_total'
  AND lk.name = 'activity'
  AND sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
ORDER BY activity;
```
Expected: `{activity=read: 90, activity=write: 135}`

**Count Total Samples:**
```sql
SELECT COUNT(*) FROM sample_value;
```
Expected: `20`

**Rate Calculation (approximate):**
For `activity=read`, the rate should be approximately **10 ops per 100ms** = **100 ops/sec** (simplified; actual timing varies slightly)

### Use Cases
- Testing instant value queries
- Basic label filtering
- Simple rate calculations
- Time-series data retrieval

---

## multi_dimensional.db

### Purpose
Test complex label filtering, multi-dimensional aggregations, grouping operations, and topk queries.

### Schema
- **Metric Family**: `requests`
- **Sample**: `request_total` (COUNTER)
- **Type**: COUNTER

### Label Dimensions
- `activity`: {read, write}
- `host`: {server1, server2, server3}
- `region`: {us-east, us-west}
- `env`: {prod, staging}

### Data Characteristics
- **Snapshots**: 4
- **Label Combinations**: 24 (2 × 3 × 2 × 2)
- **Total Samples**: 96
- **Value Pattern**: `(combo_index * 10) + (snapshot_index * 100)`
  - First snapshot: values 0, 10, 20, ..., 230
  - Second snapshot: values 100, 110, 120, ..., 330
  - And so on...

### Expected Query Results

**Count by Environment:**
```sql
SELECT lv.value as env, COUNT(*) as sample_count
FROM sample_value sv
JOIN metric_instance mi ON sv.metric_instance_id = mi.id
JOIN label_set_membership lsm ON mi.label_set_id = lsm.label_set_id
JOIN label_key lk ON lsm.label_key_id = lk.id
JOIN label_value lv ON lsm.label_value_id = lv.id
WHERE lk.name = 'env'
GROUP BY env;
```
Expected: `{prod: 48, staging: 48}`

**Unique Label Sets:**
```sql
SELECT COUNT(DISTINCT label_set_id) FROM metric_instance;
```
Expected: `24`

### Use Cases
- Multi-dimensional filtering (e.g., `activity=write AND region=us-east AND env=prod`)
- Aggregations with grouping (e.g., `sum by (region, env)`)
- Top-K queries (e.g., "top 5 hosts by request count")
- Complex label pattern matching

---

## latency_timers.db

### Purpose
Test summary/timer metrics with quantiles, statistical aggregations, and rate statistics.

### Schema
- **Metric Family**: `operation_latency`
- **Sample**: `operation_latency_ms` (TIMER/SUMMARY)
- **Type**: TIMER

### Label Dimensions
- `operation`: {select, insert, update, delete}

### Data Characteristics
- **Snapshots**: 6
- **Operations**: 4
- **Total Sample Values**: 24
- **Quantiles**: p50, p75, p90, p95, p98, p99, p999 (stored in `sample_quantile` table)
- **Statistics**: count, sum, min, max, mean, stddev (in `sample_value`)
- **Rate Statistics**: mean, m1, m5, m15 (in `sample_rate` table)

### Latency Patterns
Base latencies by operation (in nanoseconds):
- `select`: 10ms base, increases by 2ms per snapshot
- `insert`: 20ms base, increases by 2ms per snapshot
- `update`: 30ms base, increases by 2ms per snapshot
- `delete`: 40ms base, increases by 2ms per snapshot

Distribution within each snapshot:
- 50% of samples at base latency (p50)
- 25% at 1.5× base (p75)
- 20% at 2.0× base (p95)
- 5% at 3.0× base (p99)

### Expected Query Results

**Get p95 latency for select operations:**
```sql
SELECT sq.quantile_value
FROM sample_quantile sq
JOIN sample_value sv ON sq.sample_value_id = sv.id
JOIN metric_instance mi ON sv.metric_instance_id = mi.id
JOIN sample_name sn ON mi.sample_name_id = sn.id
JOIN label_set_membership lsm ON mi.label_set_id = lsm.label_set_id
JOIN label_key lk ON lsm.label_key_id = lk.id
JOIN label_value lv ON lsm.label_value_id = lv.id
WHERE sn.sample = 'operation_latency_ms'
  AND lk.name = 'operation'
  AND lv.value = 'select'
  AND sq.quantile = 0.95
  AND sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
LIMIT 1;
```
Expected: Approximately `22,000,000` nanoseconds (22ms) for latest snapshot

**Count quantile entries:**
```sql
SELECT COUNT(*) FROM sample_quantile;
```
Expected: `168` (24 samples × 7 quantiles each)

### Use Cases
- Quantile extraction (p50, p95, p99)
- Statistical aggregations (mean, stddev, min, max)
- Latency trend analysis over time
- Rate statistics queries (m1, m5, m15)

---

## rate_calculations.db

### Purpose
Test rate() and increase() commands with predictable, mathematically known patterns over extended time periods.

### Schema
- **Metric Family**: `patterns`
- **Sample**: `counter_total` (COUNTER)
- **Type**: COUNTER

### Label Dimensions
- `pattern`: {linear, exponential, step}

### Data Characteristics
- **Snapshots**: 120 (simulating 1 hour at 30s intervals)
- **Patterns**: 3
- **Total Samples**: 360
- **Interval Between Snapshots**: ~50ms (during generation)

### Growth Patterns

#### Linear Pattern (`pattern=linear`)
- Growth: **+10 per snapshot**
- Values: 0, 10, 20, 30, ..., 1190
- Expected rate: **10 ops / 50ms** = **200 ops/sec** (constant)
- Total increase over all snapshots: **1190**

#### Exponential Pattern (`pattern=exponential`)
- Growth: **×1.01 per snapshot** (1% compound growth)
- Starting value: 100
- Values: 100, 101, 102.01, 103.03, ...
- Expected rate: Increases over time
- Formula: `value[i] = 100 × (1.01)^i`

#### Step Pattern (`pattern=step`)
- Growth: **Steps every 10 snapshots**
- Values: 100 (snapshots 0-9), 200 (10-19), 300 (20-29), ..., 1200 (110-119)
- Expected rate: **0 within step, jump at boundaries**
- Total steps: 12

### Expected Query Results

**Linear pattern increase over all snapshots:**
```sql
SELECT MAX(sv.value) - MIN(sv.value) as total_increase
FROM sample_value sv
JOIN metric_instance mi ON sv.metric_instance_id = mi.id
JOIN sample_name sn ON mi.sample_name_id = sn.id
JOIN label_set_membership lsm ON mi.label_set_id = lsm.label_set_id
JOIN label_key lk ON lsm.label_key_id = lk.id
JOIN label_value lv ON lsm.label_value_id = lv.id
WHERE sn.sample = 'counter_total'
  AND lk.name = 'pattern'
  AND lv.value = 'linear';
```
Expected: `1190`

**Count snapshots per pattern:**
```sql
SELECT lv.value as pattern, COUNT(*) as snapshots
FROM sample_value sv
JOIN metric_instance mi ON sv.metric_instance_id = mi.id
JOIN label_set_membership lsm ON mi.label_set_id = lsm.label_set_id
JOIN label_key lk ON lsm.label_key_id = lk.id
JOIN label_value lv ON lsm.label_value_id = lv.id
WHERE lk.name = 'pattern'
GROUP BY pattern;
```
Expected: `{linear: 120, exponential: 120, step: 120}`

### Use Cases
- Testing rate() with constant rate (linear)
- Testing rate() with changing rate (exponential)
- Testing rate() with discontinuous data (step)
- Testing increase() over various time windows
- Validating rate calculation algorithms with known results

---

## examples.db

### Purpose
Test TopK queries, range queries, increase/rate calculations, and example documentation tests with realistic API request data.

### Schema
- **Metric Family**: `api_requests_total`
- **Sample**: `api_requests_total` (COUNTER)
- **Type**: COUNTER

### Label Dimensions
- `method`: {GET, POST, PUT}
- `status`: {200, 404, 500}
- `endpoint`: {/api/users, /api/orders, /api/products}

### Data Characteristics
- **Snapshots**: 5 (30 seconds apart)
- **Label Combinations**: 27 (3 methods × 3 statuses × 3 endpoints)
- **Total Samples**: 135
- **Growth Pattern**: Each combination grows linearly, with values ranging from 1000 to 11000+ across combinations
- **MetricsQL Specs**: Each metric instance has a spec like `api_requests_total{endpoint="/api/users",method="GET",status="200"}`

### Expected Query Results

**TopK Query (top 3 by value):**
```sql
SELECT mi.spec, sv.value
FROM metric_instance mi
JOIN sample_value sv ON sv.metric_instance_id = mi.id
WHERE sv.timestamp_ms = (SELECT MAX(timestamp_ms) FROM sample_value)
ORDER BY sv.value DESC
LIMIT 3;
```
Expected: Top 3 metric instances with highest values (around 11000+)

**Range Query (all snapshots for specific metric):**
```sql
SELECT mi.spec, sv.value, datetime(sv.timestamp_ms / 1000, 'unixepoch') as time
FROM metric_instance mi
JOIN sample_value sv ON sv.metric_instance_id = mi.id
WHERE mi.spec LIKE 'api_requests_total{endpoint="/api/users",method="GET",status="200"}'
ORDER BY sv.timestamp_ms;
```
Expected: 5 time-series points showing growth

### Use Cases
- Testing TopK queries with many label combinations
- Range queries over multiple snapshots
- Increase and rate calculations with realistic API metrics
- Example documentation tests
- Filtering by status codes (e.g., errors vs success)

---

## Schema Reference

All databases share this schema (created by `SqliteSnapshotReporter`):

```
metric_family (id, name, help, unit, type)
    └─ sample_name (id, metric_family_id, sample)
        └─ metric_instance (id, sample_name_id, label_set_id, spec) [UNIQUE(sample_name_id, label_set_id)]
            └─ sample_value (id, metric_instance_id, timestamp_ms, value)
                 ├─ sample_quantile (sample_value_id, quantile, quantile_value)
                 ├─ sample_rate (sample_value_id, rate_type, rate_value)
                 └─ sample_histogram (sample_value_id, start_seconds, interval_seconds, max_value, histogram_base64)

label_key (id, name)
label_value (id, value)
label_set (id, hash)
label_set_membership (label_set_id, label_key_id, label_value_id)
```

### Key Schema Features

- **metric_instance table**: Materializes unique combinations of (sample_name_id, label_set_id), representing distinct metric instances
  - **spec column**: Contains the fully MetricsQL/PromQL-compliant metric specifier in the form `metric_family{label1="value1",label2="value2"}` with labels sorted alphabetically
  - Example: `api_requests_total{endpoint="/api/users",method="GET",status="200"}`
- **Simplified sample_value**: Now only stores (metric_instance_id, timestamp_ms, value) - all label information accessed through metric_instance
- **Efficient queries**: The metric_instance table enables direct filtering by metric identity without joining through samples, and the spec column provides instant access to human-readable metric identifiers

## Regenerating Test Databases

To regenerate all test databases (e.g., after schema changes):

```bash
mvn exec:java -pl nb-apis/nb-mql-api \
  -Dexec.mainClass="io.nosqlbench.nb.mql.testdata.TestDatabaseGenerator" \
  -Dexec.classpathScope=test
```

The generator will overwrite existing databases in `src/test/resources/testdata/`.

## Using in Tests

Use `TestDatabaseLoader` utility to access these databases in your tests:

```java
import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;

Path dbPath = TestDatabaseLoader.getDatabase("simple_counter.db");
Connection conn = MetricsDatabaseReader.connect(dbPath);
// ... run queries
```

---

*Generated: 2025-10-29*
*Schema Version: OpenMetrics-aligned (SqliteSnapshotReporter) with metric_instance table*
