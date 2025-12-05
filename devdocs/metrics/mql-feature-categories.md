---
title: "MetricsQL Feature Categories"
description: "Implementation mapping for MetricsQL functions in SQLite."
audience: developer
diataxis: explanation
tags:
  - metricsql
  - implementation
component: core
topic: metrics
status: live
owner: "@nosqlbench/metrics"
generated: false
---

# MetricsQL Feature Categories - Detailed Implementation Mapping

## Category A: Direct SQL Mapping Functions
**Implementation Complexity**: Low
**SQL Pattern**: Direct function mapping or simple expressions

### A1. Mathematical Functions
These map directly to SQLite built-in functions:

| MetricsQL Function | SQLite Equivalent | Example |
|-------------------|------------------|---------|
| `abs(m)` | `ABS(value)` | `abs(temperature)` |
| `ceil(m)` | `CEIL(value)` or `CAST(value + 0.999999 AS INTEGER)` | `ceil(response_time)` |
| `floor(m)` | `FLOOR(value)` or `CAST(value AS INTEGER)` | `floor(cpu_usage)` |
| `round(m)` | `ROUND(value)` | `round(memory_gb, 2)` |
| `sqrt(m)` | `SQRT(value)` | `sqrt(variance)` |
| `exp(m)` | `EXP(value)` | `exp(growth_rate)` |
| `ln(m)` | `LN(value)` or `LOG(value)` | `ln(requests)` |
| `log2(m)` | `LOG(value) / LOG(2)` | `log2(bytes)` |
| `log10(m)` | `LOG10(value)` | `log10(count)` |
| `pow(m, n)` | `POWER(value, n)` | `pow(base, 2)` |

### A2. Trigonometric Functions
SQLite has limited trig support, may need custom functions:

| MetricsQL Function | Implementation Note |
|-------------------|-------------------|
| `sin(m)` | Requires SQLite extension or custom function |
| `cos(m)` | Requires SQLite extension or custom function |
| `tan(m)` | Requires SQLite extension or custom function |
| `asin(m)` | Requires SQLite extension or custom function |
| `acos(m)` | Requires SQLite extension or custom function |
| `atan(m)` | Requires SQLite extension or custom function |

## Category B: Window-Based Rollup Functions
**Implementation Complexity**: Medium
**SQL Pattern**: Window functions with time buckets

### B1. Basic Statistical Rollups
```sql
-- Template for rollup functions
WITH time_window AS (
  SELECT
    timestamp_ms,
    value,
    label_set_id
  FROM sample_value sv
  JOIN ...
  WHERE timestamp_ms >= ? AND timestamp_ms <= ?
)
SELECT
  [AGGREGATE](value) AS result,
  label_set_id
FROM time_window
GROUP BY label_set_id
```

| Function | SQL Aggregate | Description |
|----------|--------------|-------------|
| `avg_over_time(m[d])` | `AVG(value)` | Average over window |
| `sum_over_time(m[d])` | `SUM(value)` | Sum over window |
| `min_over_time(m[d])` | `MIN(value)` | Minimum in window |
| `max_over_time(m[d])` | `MAX(value)` | Maximum in window |
| `count_over_time(m[d])` | `COUNT(value)` | Count of samples |
| `stddev_over_time(m[d])` | `STDDEV(value)` | Standard deviation |
| `stdvar_over_time(m[d])` | `VARIANCE(value)` | Variance |

### B2. Rate-Based Rollups
Require LAG/LEAD window functions:

```sql
-- rate(m[5m]) implementation
WITH rates AS (
  SELECT
    value,
    timestamp_ms,
    LAG(value) OVER (PARTITION BY label_set_id ORDER BY timestamp_ms) AS prev_value,
    LAG(timestamp_ms) OVER (PARTITION BY label_set_id ORDER BY timestamp_ms) AS prev_ts
  FROM ...
)
SELECT
  (value - prev_value) / ((timestamp_ms - prev_ts) / 1000.0) AS rate_per_sec
FROM rates
WHERE prev_value IS NOT NULL
```

| Function | Description | Special Handling |
|----------|-------------|-----------------|
| `rate(m[d])` | Per-second rate | Counter resets detection |
| `irate(m[d])` | Instant rate (last 2 points) | Use only last 2 samples |
| `increase(m[d])` | Total increase | Counter resets detection |
| `delta(m[d])` | Difference | No counter reset handling |
| `idelta(m[d])` | Instant delta | Last 2 points only |
| `deriv(m[d])` | Derivative | Linear regression |

### B3. Quantile Rollups
Require percentile calculations:

```sql
-- quantile_over_time(0.95, m[5m])
WITH ordered AS (
  SELECT
    value,
    PERCENT_RANK() OVER (PARTITION BY label_set_id ORDER BY value) AS percentile
  FROM ...
)
SELECT value
FROM ordered
WHERE percentile >= 0.95
LIMIT 1
```

| Function | Description |
|----------|-------------|
| `quantile_over_time(φ, m[d])` | φ-quantile over window |
| `median_over_time(m[d])` | 50th percentile |

### B4. Special Rollups
Complex implementations:

| Function | Description | Implementation Notes |
|----------|-------------|---------------------|
| `resets(m[d])` | Count counter resets | Detect value < prev_value |
| `changes(m[d])` | Count value changes | Count distinct values |
| `present_over_time(m[d])` | 1 if data exists | EXISTS check |
| `absent_over_time(m[d])` | 1 if no data | NOT EXISTS check |
| `range_over_time(m[d])` | max - min | MAX(value) - MIN(value) |

## Category C: Cross-Series Aggregations
**Implementation Complexity**: Medium
**SQL Pattern**: GROUP BY with aggregates

### C1. Basic Aggregations
```sql
-- sum(m) by (label)
SELECT
  label_value,
  SUM(value) AS result
FROM ...
GROUP BY label_value
```

| Function | SQL | Modifiers |
|----------|-----|-----------|
| `sum(m)` | `SUM(value)` | `by (labels)`, `without (labels)` |
| `avg(m)` | `AVG(value)` | `by (labels)`, `without (labels)` |
| `min(m)` | `MIN(value)` | `by (labels)`, `without (labels)` |
| `max(m)` | `MAX(value)` | `by (labels)`, `without (labels)` |
| `count(m)` | `COUNT(DISTINCT label_set_id)` | `by (labels)`, `without (labels)` |
| `group(m)` | Returns 1 | `by (labels)`, `without (labels)` |

### C2. Statistical Aggregations
| Function | Description |
|----------|-------------|
| `stddev(m)` | Standard deviation across series |
| `stdvar(m)` | Variance across series |
| `quantile(φ, m)` | φ-quantile across series |
| `median(m)` | Median across series |

### C3. Top/Bottom K
```sql
-- topk(3, m)
SELECT * FROM (
  SELECT *, ROW_NUMBER() OVER (ORDER BY value DESC) AS rn
  FROM ...
) WHERE rn <= 3
```

| Function | Description |
|----------|-------------|
| `topk(k, m)` | Top k series by value |
| `bottomk(k, m)` | Bottom k series by value |
| `limitk(k, m)` | Limit to k series |

## Category D: Label Manipulation
**Implementation Complexity**: High
**SQL Pattern**: Complex string operations and CTEs

### D1. Label Modification Functions
```sql
-- label_set(m, "new_label", "value")
WITH modified AS (
  SELECT
    *,
    'value' AS new_label
  FROM ...
)
SELECT * FROM modified
```

| Function | Description | SQL Approach |
|----------|-------------|--------------|
| `label_set(m, k, v)` | Add/modify label | Add column in SELECT |
| `label_map(m, k, map)` | Map label values | CASE expression |
| `label_del(m, k...)` | Remove labels | Exclude from GROUP BY |
| `label_keep(m, k...)` | Keep only specified | Include only in GROUP BY |
| `label_copy(m, src, dst)` | Copy label | Duplicate column |
| `label_move(m, src, dst)` | Rename label | Alias column |
| `label_join(m, dst, sep, src...)` | Concatenate labels | String concatenation |
| `label_replace(m, dst, repl, src, regex)` | Regex replace | SQLite REGEXP |

## Category E: Binary Operations
**Implementation Complexity**: High
**SQL Pattern**: JOIN operations between metrics

### E1. Arithmetic Operations
```sql
-- metric1 + metric2
SELECT
  m1.timestamp,
  m1.value + m2.value AS result,
  m1.labels
FROM
  (SELECT ... WHERE metric = 'metric1') m1
  JOIN
  (SELECT ... WHERE metric = 'metric2') m2
  ON m1.timestamp = m2.timestamp
  AND m1.label_set_id = m2.label_set_id
```

| Operator | SQL Operation |
|----------|--------------|
| `+` | `m1.value + m2.value` |
| `-` | `m1.value - m2.value` |
| `*` | `m1.value * m2.value` |
| `/` | `m1.value / m2.value` |
| `%` | `m1.value % m2.value` |
| `^` | `POWER(m1.value, m2.value)` |

### E2. Comparison Operations
Return 0 or 1 based on comparison:

| Operator | SQL Expression |
|----------|---------------|
| `==` | `CASE WHEN m1.value = m2.value THEN 1 ELSE 0 END` |
| `!=` | `CASE WHEN m1.value != m2.value THEN 1 ELSE 0 END` |
| `>` | `CASE WHEN m1.value > m2.value THEN 1 ELSE 0 END` |
| `<` | `CASE WHEN m1.value < m2.value THEN 1 ELSE 0 END` |
| `>=` | `CASE WHEN m1.value >= m2.value THEN 1 ELSE 0 END` |
| `<=` | `CASE WHEN m1.value <= m2.value THEN 1 ELSE 0 END` |

### E3. Set Operations
| Operator | Description | SQL Pattern |
|----------|-------------|-------------|
| `and` | Intersection | INNER JOIN |
| `or` | Union | UNION |
| `unless` | Difference | LEFT JOIN WHERE NULL |

### E4. Matching Modifiers
| Modifier | Description |
|----------|-------------|
| `on(labels)` | Match only on specified labels |
| `ignoring(labels)` | Match ignoring specified labels |
| `group_left(labels)` | Many-to-one matching |
| `group_right(labels)` | One-to-many matching |

## Category F: Time Functions
**Implementation Complexity**: Low
**SQL Pattern**: Timestamp operations

| Function | Description | SQL Implementation |
|----------|-------------|-------------------|
| `time()` | Current timestamp | `strftime('%s', 'now')` |
| `now()` | Alias for time() | `strftime('%s', 'now')` |
| `start()` | Query start time | Parameter |
| `end()` | Query end time | Parameter |
| `step()` | Query step | Parameter |
| `day_of_month()` | Day of month | `strftime('%d', timestamp)` |
| `day_of_week()` | Day of week | `strftime('%w', timestamp)` |
| `days_in_month()` | Days in month | Complex calculation |
| `hour()` | Hour of day | `strftime('%H', timestamp)` |
| `minute()` | Minute of hour | `strftime('%M', timestamp)` |
| `month()` | Month of year | `strftime('%m', timestamp)` |
| `year()` | Year | `strftime('%Y', timestamp)` |

## Category G: Special Functions
**Implementation Complexity**: Varies

### G1. Histogram Functions
Require special handling of histogram data:

| Function | Description |
|----------|-------------|
| `histogram_quantile(φ, m)` | Calculate quantile from histogram |
| `histogram_share(m)` | Share of observations |
| `histogram_avg(m)` | Average from histogram |
| `histogram_stddev(m)` | Stddev from histogram |
| `histogram_stdvar(m)` | Variance from histogram |

### G2. Prediction Functions
Require linear regression or similar:

| Function | Description |
|----------|-------------|
| `predict_linear(m[d], t)` | Linear prediction |
| `holt_winters(m[d], sf, tf)` | Holt-Winters prediction |

### G3. Utility Functions
| Function | Description | Implementation |
|----------|-------------|---------------|
| `vector(s)` | Create constant vector | SELECT constant |
| `scalar(m)` | Convert to scalar | Single value check |
| `sort(m)` | Sort by value | ORDER BY value |
| `sort_desc(m)` | Sort descending | ORDER BY value DESC |

## Implementation Priority Matrix

| Priority | Categories | Rationale |
|----------|-----------|-----------|
| P0 (Critical) | A1, B1, B2 (rate/increase), C1, E1 | Core functionality |
| P1 (High) | B2 (remaining), C2, D1 (basic), F | Common use cases |
| P2 (Medium) | B3, B4, C3, E2, E3 | Advanced queries |
| P3 (Low) | A2, D1 (advanced), G1, G2, G3 | Specialized features |

## DRY Implementation Strategy

### 1. Base Transformers
Create abstract base classes for each category:

```java
abstract class RollupTransformer {
    protected String buildTimeWindowCTE(String metric, Duration window) { }
    protected String buildAggregation(AggregationType type) { }
}

abstract class AggregationTransformer {
    protected String buildGroupBy(List<String> labels) { }
    protected String buildHaving(String condition) { }
}

abstract class BinaryOpTransformer {
    protected String buildJoin(JoinType type, String left, String right) { }
}
```

### 2. Function Registry
Map functions to their implementations:

```java
class FunctionRegistry {
    Map<String, FunctionTransformer> functions = new HashMap<>();

    void register() {
        // Mathematical functions
        functions.put("abs", new DirectSQLFunction("ABS"));
        functions.put("ceil", new DirectSQLFunction("CEIL"));

        // Rollup functions
        functions.put("rate", new RateTransformer());
        functions.put("avg_over_time", new RollupTransformer("AVG"));

        // Aggregations
        functions.put("sum", new AggregateTransformer("SUM"));
    }
}
```

### 3. Shared SQL Fragments
Reusable SQL building blocks:

```java
class SQLFragments {
    static final String LABEL_JOIN = "...";
    static final String TIME_FILTER = "timestamp_ms >= ? AND timestamp_ms <= ?";
    static final String METRIC_FILTER = "sn.sample = ?";

    static String buildLabelFilter(Map<String, String> labels) { }
    static String buildTimeWindow(Duration window) { }
}
```

This categorization provides a clear roadmap for implementing MetricsQL features in a DRY, maintainable way using SQLite as the backend.
