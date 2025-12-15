# NoSQLBench Metrics Naming Guide

## Overview

NoSQLBench follows **OpenMetrics canonical naming conventions** to ensure compatibility with Prometheus, VictoriaMetrics, Grafana, and other monitoring systems. This guide explains the current naming format and historical changes.

---

## Current Metric Naming Conventions

### 1. Underscore Separators (Not Dots)

Multi-component metric names use underscores to comply with OpenMetrics specifications.

**Pattern:** `<metric>_<component>`

**Examples:**
- `cycles_servicetime` - Cycle service time timer
- `cycles_responsetime` - Cycle response time timer
- `cycles_waittime` - Wait time when rate-limited
- `strides_responsetime` - Stride response time timer

### 2. Standard Activity Metrics

Every activity automatically tracks these core metrics:

**Timing Metrics (Timers):**
- `read_input` - Overhead acquiring cycle ranges
- `strides` - Stride execution time
- `strides_responsetime` - Stride time including rate delays
- `cycles_servicetime` - Cycle execution time
- `cycles_responsetime` - Cycle time including rate delays
- `bind` - Data binding phase
- `execute` - Operation execution
- `result` - Result processing (all attempts)
- `result_success` - Successful operations only
- `verifier` - Verification phase

**Counters:**
- `pending_ops` - Operations currently in flight

**Wait Time Gauges:**
- `cycles_waittime` - Accumulated wait time when rate-limited

**Error Tracking:**
- `errors_<ExceptionName>` - Per-exception counters
- `errors_ALL` - Total error counter
- `errormeters_<ExceptionName>` - Per-exception rate meters
- `errormeters_ALL` - All errors rate meter
- `errortimers_<ExceptionName>` - Per-exception timing
- `error_rate_1m` - 1-minute error rate (gauge)
- `error_rate_5m` - 5-minute error rate (gauge)
- `error_rate_15m` - 15-minute error rate (gauge)
- `error_rate_total` - Cumulative error ratio (gauge)
- `errors_total` - Total errors (gauge)

**Verification:**
- `verificationcounts_RETRIES` - Verification retry counter
- `verificationcounts_ERRORS` - Verification error counter

**Retry Tracking:**
- `tries` - Histogram of retry attempts

### 3. OpenMetrics Export Suffixes

When metrics are exported in Prometheus/OpenMetrics format, type-specific suffixes are automatically added:

**Counters:**
- Internal: `pending_ops`
- Exported: `pending_ops_total`

**Timers/Histograms (exported as Summary):**
- Base metric: `result_success`
- Quantile samples: `result_success{quantile="0.99"}`
- Aggregates:
  - `result_success_count` - Total count
  - `result_success_sum` - Sum of all values
  - `result_success_min` - Minimum value
  - `result_success_max` - Maximum value
  - `result_success_mean_rate` - Mean rate
  - `result_success_m1_rate` - 1-minute rate
  - `result_success_m5_rate` - 5-minute rate
  - `result_success_m15_rate` - 15-minute rate

---

## Historical Changes

### Version 5.21 (Jan 2024): Dot-to-Underscore Migration

**What Changed:** Multi-component metric names switched from dots to underscores.

| Old Name (Pre-5.21) | New Name (5.21+) |
|---------------------|------------------|
| `cycles.servicetime` | `cycles_servicetime` |
| `cycles.waittime` | `cycles_waittime` |
| `cycles.responsetime` | `cycles_responsetime` |
| `strides.responsetime` | `strides_responsetime` |

**Why:** OpenMetrics reserves dots for namespace/scope separation. Underscores are the canonical component separator.

**Impact:** Scripts and dashboards referencing old names need updating.

### Version 5.23 (Sep 2023): Error Metric Consolidation

**What Changed:** Error metric names simplified with consistent underscore separators.

| Old Name (Pre-5.23) | New Name (5.23+) |
|---------------------|------------------|
| `errorcounts.ALL` | `errors_ALL` |
| `errorcounts.<ExceptionName>` | `errors_<ExceptionName>` |
| `errormeters.ALL` | `errormeters_ALL` |
| `errormeters.<ExceptionName>` | `errormeters_<ExceptionName>` |
| `errortimers.<ExceptionName>` | `errortimers_<ExceptionName>` |
| `verificationcounts.RETRIES` | `verificationcounts_RETRIES` |
| `verificationcounts.ERRORS` | `verificationcounts_ERRORS` |

**Why:** Consistent naming pattern across all error metrics.

### Version 5.24 (Jan 2024): Pre-Computed Error Gauges

**What Changed:** Added new pre-computed error tracking gauges.

**New Metrics:**
- `error_rate_1m`, `error_rate_5m`, `error_rate_15m` - Error rate gauges
- `error_rate_total` - Cumulative error ratio
- `errors_total` - Total error count

**Why:** Easier error rate monitoring without complex PromQL queries.

---

## Querying Metrics in Different Systems

### JavaScript/Groovy Scripts (Direct Access)

```javascript
// Access timer count
metrics.myactivity.cycles_servicetime.count

// Access timer mean rate
metrics.myactivity.cycles_servicetime.meanRate

// Access error counts
metrics.myactivity.errors_TimeoutException.count

// Access error rates (added v5.24)
metrics.myactivity.error_rate_1m.value
```

### Prometheus (Raw NoSQLBench Export)

NoSQLBench exports timers/histograms as **Summary** type:

```promql
# Query specific quantile
result_success{quantile="0.99"}

# Get count
result_success_count

# Get sum for rate calculations
rate(result_success_sum[5m])

# Counter with automatic _total suffix
pending_ops_total

# Error rates
error_rate_1m
```

### VictoriaMetrics/Grafana

**Important:** VictoriaMetrics automatically converts summary quantiles to histogram format for better aggregation.

**What NoSQLBench exports:**
```
# TYPE result_success summary
result_success{quantile="0.99"} 5000.0
```

**What VictoriaMetrics shows in queries:**
```promql
# VictoriaMetrics converts quantile → le internally
result_success{le="0.99"}  # Note: le instead of quantile

# Compute custom percentiles (enabled by VictoriaMetrics conversion)
histogram_quantile(0.95, rate(result_success[5m]))

# Standard aggregates work the same
result_success_count
rate(result_success_sum[5m])
```

**Why the difference:**
- NoSQLBench exports standard summary format with `quantile` labels
- VictoriaMetrics converts these to `le` labels internally for histogram-like aggregation
- This is a VictoriaMetrics feature, not a NoSQLBench export format
- Enables cross-instance aggregation and custom percentile calculations

### SQLite Metrics Database (MQL Queries)

```bash
# Query metrics by name
nb5 mql query "cycles_servicetime{alias='myactivity'}"

# List all metrics
nb5 mql metrics --db logs/metrics.db

# Query with MetricsQL
nb5 mql query "rate(result_success[5m])"
```

---

## Migration Checklist

If updating from pre-5.21 NoSQLBench:

### Scripts (JavaScript/Groovy)
- [ ] Replace `cycles.servicetime` → `cycles_servicetime`
- [ ] Replace `cycles.waittime` → `cycles_waittime`
- [ ] Replace `cycles.responsetime` → `cycles_responsetime`
- [ ] Replace `errorcounts.` → `errors_`
- [ ] Replace `errormeters.` → `errormeters_`
- [ ] Replace `errortimers.` → `errortimers_`

### Prometheus/Grafana Dashboards
- [ ] Update query variable names (use underscores)
- [ ] If using VictoriaMetrics: queries with `le=` will continue working
- [ ] If using Prometheus: use `quantile=` for summary metrics
- [ ] Consider using new pre-computed `error_rate_*` gauges

### PromQL/MetricsQL Queries
- [ ] Update metric names to use underscores
- [ ] Verify `_total` suffix on counters
- [ ] Use appropriate label (`quantile=` for Prometheus, `le=` for VictoriaMetrics)

---

## Quick Reference: Name Patterns

**Activity Metrics Format:**
```
{activity_alias}.{metric_name}_{component}.{property}
```

**Examples:**
```
myload.cycles_servicetime.count        # Timer sample count
myload.cycles_servicetime.meanRate     # Timer mean rate
myload.errors_TimeoutException.count   # Error counter
myload.error_rate_1m.value            # 1-min error rate gauge
```

**Prometheus Export Format:**
```
{metric_name}_{component}{suffix}{labels}
```

**Examples:**
```
pending_ops_total                               # Counter
result_success{quantile="0.99"}                # Summary quantile
result_success_count                           # Summary count
cycles_servicetime_m1_rate                     # 1-minute rate
```

---

## Summary Type vs Histogram Type

NoSQLBench uses **Summary** type for timers and histograms:

**Summary Type (Current NoSQLBench):**
```
# TYPE result_success summary
result_success{quantile="0.5"} 1000.0
result_success{quantile="0.99"} 5000.0
result_success_count 10000
result_success_sum 25000000
```

**Rationale:**
- Accurately represents NoSQLBench's HDR histogram quantiles
- Quantiles pre-computed on client side
- Lower bandwidth (only selected quantiles exported)
- Semantically correct for single-instance testing

**Note:** VictoriaMetrics converts summary → histogram format internally for aggregation, which is why you see `le=` labels in VictoriaMetrics queries even though NoSQLBench exports `quantile=`.

---

## Troubleshooting

**Issue:** Seeing `le=` labels instead of `quantile=`
**Cause:** You're querying VictoriaMetrics, which auto-converts summaries to histograms
**Solution:** This is expected behavior - use `le=` in VictoriaMetrics queries

**Issue:** Old metric names not found
**Cause:** Using pre-5.21 names with post-5.21 NoSQLBench
**Solution:** Update names to use underscores (see migration checklist)

**Issue:** Error messages showing "Error in space '951'" instead of "Error in space '0'"
**Cause:** Bug in error message formatting (fixed in v5.25)
**Solution:** Upgrade to v5.25+ - this was cosmetic only, space caching worked correctly

---

## Additional Resources

- [OpenMetrics Specification](https://github.com/OpenObservability/OpenMetrics/blob/main/specification/OpenMetrics.md)
- [VictoriaMetrics Summary to Histogram Conversion](https://docs.victoriametrics.com/)
- NoSQLBench SQLite Metrics: Use `nb5 mql --help` for query syntax
