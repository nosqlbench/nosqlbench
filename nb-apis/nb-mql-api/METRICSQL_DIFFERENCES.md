# MetricsQL Implementation Differences

This document describes behavioral differences between NoSQLBench's MetricsQL implementation and VictoriaMetrics' official MetricsQL engine.

## Summary

| Feature | VictoriaMetrics Behavior | NoSQLBench Behavior | Impact |
|---------|--------------------------|---------------------|--------|
| Counter Reset Detection | Automatic detection and handling | Not implemented | Affects `rate()` and `increase()` accuracy during restarts |
| Sample Before Window | Includes last sample before time window | Uses only samples within window | Minimal impact (<5%) on rate calculations |
| Staleness Intervals | Supports staleness markers and intervals | No staleness concept | Not applicable for local metrics |
| Label Grouping (`by` clause) | Collapses to specified labels only | Groups by full label set | Returns more detailed results than expected |

---

## Counter Reset Detection

### VictoriaMetrics Behavior

When a counter metric decreases (indicating a restart/reset), VictoriaMetrics automatically detects this and treats it as a reset to zero, continuing rate/increase calculations correctly.

**Example:**
```
T0: value=100
T1: value=150  → increase = 50
T2: value=30   → Reset detected! increase = 30 (not -120)
T3: value=60   → increase = 30
Total increase: 110 ✓
```

### NoSQLBench Behavior

Uses simple `MAX(value) - MIN(value)` calculation without reset detection.

**Example with same data:**
```
Total increase: 150 - 30 = 120 ✗ (incorrect)
```

### When This Matters

- **During benchmark restarts**: Each restart resets counters to zero
- **Process crashes**: Counters start over from zero
- **Long-running tests**: Any interruption causes reset
- **Multiple test runs**: Sequential runs appear as counter decreases

### Workaround

For accurate rate/increase calculations across restarts:
- Use separate sessions for each benchmark run
- Query metrics within single continuous runs only
- Compare values within known-good time ranges

---

## Sample Before Window

### VictoriaMetrics Behavior

Includes the last sample *before* the time window start for baseline calculations in `rate()` and `increase()` functions.

**Example:**
```
T-6m: value=100  ← VictoriaMetrics includes this for baseline
T-4m: value=150
T-2m: value=200
T-0m: value=250

Query: rate(metric[5m])
Window used: T-6m to T-0m (includes sample before window)
```

### NoSQLBench Behavior

Uses only samples strictly within the specified time window `[now - 5m, now]`.

**Example with same data:**
```
Query: rate(metric[5m])
Window used: T-5m to T-0m (first sample is T-4m)
```

### When This Matters

- **Irregular sampling**: Larger differences when samples don't align with window boundaries
- **Short time windows**: More noticeable with windows <1m
- **Sparse metrics**: Affects accuracy when few samples exist

With regular sampling (e.g., every 10s), the difference is typically <5%.

### Impact

For typical NoSQLBench use (regular sampling, reasonable windows), this difference is minimal and doesn't affect practical metric interpretation.

---

## Staleness Intervals

### VictoriaMetrics Behavior

Supports Prometheus staleness markers and the `-search.maxStalenessInterval` flag (default 5m). Won't use data points older than the staleness interval even if within the query window.

**Example:**
```
T-10m: value=100
T-5m:  value=150
[6 minute gap]
T-0m:  query time

With maxStaleness=5m: Returns empty (last sample too old)
```

### NoSQLBench Behavior

No staleness concept. If a sample exists in the time window, it's included.

### Why This Difference Exists

Staleness detection is designed for distributed monitoring systems where scraped targets can go offline. For NoSQLBench's local SQLite metrics:
- Metrics exist when the benchmark is running
- No concept of "stale" data vs "missing" data
- No remote scraping or target failures

**Status**: Intentional difference - staleness is not applicable to local metrics collection.

---

## Label Grouping with `by` Clause

### VictoriaMetrics Behavior

The `by` clause collapses all labels except those specified, summing across the removed dimensions.

**Example:**
```metricsql
sum(http_requests{env=prod, svc=api, region=us, method=GET}) by (env)

Result:
{env=prod}: 500  # Summed across all svc/region/method combinations
```

### NoSQLBench Behavior

Currently groups by the complete label set, preserving all labels.

**Example with same query:**
```
Result:
{env=prod, svc=api, region=us, method=GET}: 100
{env=prod, svc=web, region=us, method=GET}: 150
{env=prod, svc=api, region=eu, method=GET}: 120
{env=prod, svc=web, region=eu, method=GET}: 130
# Returns more rows with full label detail
```

### When This Matters

- **PromQL familiarity**: Users expecting PromQL semantics will see unexpected results
- **Dashboard design**: Charts designed for collapsed dimensions show extra series
- **Result size**: Returns more rows than expected

### Workarounds

1. **Use the `aggregate` command**: For true label-specific grouping
   ```bash
   nb5 mql aggregate --by env --agg sum
   ```

2. **Pre-filter labels**: Reduce dimensions before aggregation
   ```metricsql
   sum(http_requests{svc="api"}) by (env)
   ```

3. **Work with detailed results**: More detail isn't wrong, just different

---

## Using These Differences in Practice

### Best Practices

1. **For accurate rate/increase calculations**:
   - Query within single continuous benchmark runs
   - Use session filtering to isolate run boundaries
   - Be aware of reset impacts in long-running tests

2. **For expected label grouping**:
   - Use the `aggregate` command for PromQL-style grouping
   - Or accept more detailed results from MetricsQL queries

3. **For general querying**:
   - Regular sampling minimizes window boundary effects
   - NoSQLBench's local metrics don't need staleness handling
   - Most queries work identically to VictoriaMetrics

### Compatibility Assessment

**High Compatibility Functions** (identical behavior):
- All basic selectors: `metric{label="value"}`
- All rollup functions: `avg_over_time()`, `min_over_time()`, `max_over_time()`, etc.
- All transform functions: `abs()`, `ceil()`, `floor()`, etc.
- Binary operations: `metric1 + metric2`, etc.
- Comparison operators: `metric > 100`, etc.
- Label manipulation: `label_set()`, `label_del()`, etc.

**Functions with Behavioral Differences**:
- `rate()`: Without counter reset detection
- `increase()`: Without counter reset detection
- Aggregations with `by`/`without`: More granular grouping

---

## Future Improvements

Planned enhancements to improve compatibility:

1. **Counter reset detection** (High Priority): Automatic detection and handling of counter resets
2. **True label-specific grouping** (Medium Priority): PromQL-compliant `by` clause behavior
3. **Sample before window** (Low Priority): Minor accuracy improvement for rate calculations

Staleness intervals will remain an intentional omission as they're not applicable to local metrics collection.

---

## Questions or Issues?

If you encounter behavior differences not documented here, please report them at:
https://github.com/nosqlbench/nosqlbench/issues

For VictoriaMetrics' official MetricsQL documentation:
https://docs.victoriametrics.com/MetricsQL.html
