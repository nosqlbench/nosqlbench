+++
title = "Reading Metrics"
description = "Understand the core metrics reported by NoSQLBench"
weight = 104
template = "page.html"

[extra]
quadrant = "tutorials"
topic = "getting-started"
category = "metrics"
tags = ["metrics", "performance", "latency", "throughput"]
+++

# Reading Metrics

NoSQLBench provides core metrics for every workload, regardless of driver or protocol. This guide explains the most important metrics and how to interpret them.

## Core Metrics

### metric: result

**The primary metric** for quick throughput and latency assessment. Encompasses the entire operation lifecycle: bind → execute → get result.

**Example:**

```
type=TIMER, name=cql-keyvalue.result, count=100000,
  min=233.48, max=358596.607, mean=3732.00338612, stddev=10254.850416061185,
  median=1874.815, p75=3648.767, p95=10115.071, p98=15855.615, p99=23916.543, p999=111292.415,
  mean_rate=4024.0234405430424, m1=3514.053841156124, m5=3307.431472596865, m15=3268.6786509004132,
  rate_unit=events/second, duration_unit=microseconds
```

**Key values:**

- **Throughput:** `mean_rate=4024` ops/second (average across entire run)
- **Latency (p75):** `p75=3648.767` microseconds = ~3.6ms (75th percentile)
- **Latency (p99):** `p99=23916.543` microseconds = ~23.9ms (99th percentile)

**Note:** Raw metrics are in microseconds. The `duration_unit` may vary based on NoSQLBench configuration - always check it.

### metric: result-success

Shows operations that completed **without errors**. Confirms test reliability.

**Example:**

```
type=TIMER, name=cql-keyvalue.result-success, count=100000,
  min=435.168, max=358645.759, mean=3752.40990808, stddev=10251.524945886964,
  median=1889.791, p75=3668.479, p95=10154.495, p98=15884.287, p99=24280.063, p999=111443.967,
  mean_rate=4003.3090048756894, m1=3523.40328629036, m5=3318.8463896065778, m15=3280.480326762243,
  rate_unit=events/second, duration_unit=microseconds
```

**Verification:** `count` should equal your `cycles` if you expect zero failures. Here: 100,000 cycles succeeded out of 100,000.

**Note:** Throughput and latency differ slightly from `result` metric because this timer only includes successful operations (no exceptions).

### metric: resultset-size

**For read workloads:** Shows the size of result sets returned from the server.

**Example:**

```
type=HISTOGRAM, name=cql-keyvalue.resultset-size, count=100000,
  min=0, max=1, mean=8.0E-5, stddev=0.008943914131967056,
  median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0
```

**Purpose:** Confirms you're reading rows that exist in the database. A mean near 0 indicates most reads returned no data (possible cache misses or empty dataset).

### metric: tries

Shows retry behavior. NoSQLBench retries failures up to 10 times by default (configurable with `maxtries`).

**Example:**

```
type=HISTOGRAM, name=cql-keyvalue.tries, count=100000,
  min=1, max=1, mean=1.0, stddev=0.0, median=1.0,
  p75=1.0, p95=1.0, p98=1.0, p99=1.0, p999=1.0
```

**Interpretation:** All values are 1.0 = no retries needed. All operations succeeded on first try.

## Understanding Percentiles

**Percentiles** answer: "What percentage of operations completed within X time?"

- **p50 (median):** 50% of operations completed in this time or less
- **p75:** 75% completed in this time or less
- **p95:** 95% completed in this time or less
- **p99:** 99% completed in this time or less
- **p999:** 99.9% completed in this time or less

**Focus on p99:** This represents your "tail latency" - the worst-case experience for 1% of operations. Critical for user experience.

## Configuration Metrics

### cycles.config.cyclerate

Target operations per second:

```
type=GAUGE, name=cql-keyvalue.cycles.config.cyclerate, value=5000.0
```

This is your requested rate limit (5000 ops/sec).

### cycles.config.burstrate

Maximum burst rate allowed:

```
type=GAUGE, name=cql-keyvalue.cycles.config.burstrate, value=5500.0
```

### cycles.waittime

Total time spent waiting (rate limiting):

```
type=GAUGE, name=cql-keyvalue.cycles.waittime, value=3898782735
```

High wait time = NoSQLBench spent significant time rate-limiting to meet your target cyclerate.

## Next Steps

You've completed your first NoSQLBench run!

- **[Next Steps](05-next-steps.md)** - Choose your path forward
- **[Workload Design Guides](../../guides/workload-design/)** - Create custom workloads
- **[Metrics Reference](../../reference/metrics/)** - Complete metrics documentation

## Congratulations!

You now understand the basics of running NoSQLBench and interpreting results. Continue to [Next Steps](05-next-steps.md) to choose your learning path.
