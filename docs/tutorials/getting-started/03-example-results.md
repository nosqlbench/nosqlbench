+++
title = "Understanding Output"
description = "Interpret NoSQLBench output and log file metrics"
weight = 103
template = "docs-page.html"

[extra]
quadrant = "tutorials"
topic = "getting-started"
category = "metrics"
tags = ["metrics", "output", "logs", "results"]
+++

# Understanding NoSQLBench Output

After running a workload, NoSQLBench generates metrics and logs. This guide explains what you're seeing and where to find it.

## Log File Metrics

NoSQLBench writes metrics to a log file shown in the output:

```
Configured scenario log at logs/scenario_20230113_135200_029.log
```

Even without configuring external metrics collection, NoSQLBench periodically reports all metrics to this log file. At scenario end, it flushes a final report.

**Warning:** The last report contains only a partial interval. When viewing the last window, only time-averaged metrics or whole-test means are meaningful.

## Sample Metrics Output

```
2019-08-12 15:46:00,274 INFO [main] -- BEGIN METRICS DETAIL --
2019-08-12 15:46:00,294 INFO [main] type=GAUGE, name=cql-keyvalue.cycles.config.burstrate, value=5500.0
2019-08-12 15:46:00,295 INFO [main] type=GAUGE, name=cql-keyvalue.cycles.config.cyclerate, value=5000.0
2019-08-12 15:46:00,295 INFO [main] type=GAUGE, name=cql-keyvalue.cycles.waittime, value=3898782735
2019-08-12 15:46:00,298 INFO [main] type=HISTOGRAM, name=cql-keyvalue.resultset-size, count=100000,
  min=0, max=1, mean=8.0E-5, stddev=0.008943914131967056, median=0.0, p75=0.0, p95=0.0,
  p98=0.0, p99=0.0, p999=0.0
2019-08-12 15:46:01,703 INFO [main] -- END METRICS DETAIL --
```

There's a lot here - let's focus on the most important metrics first. For detailed analysis, see [Reading Metrics](04-reading-metrics.md).

## Alternative Metrics Formats

Log files aren't ideal for metrics analysis. NoSQLBench supports several export formats:

### 1. Graphite Server

```bash
./nb5 <workload> <scenario> ... --report-graphite-to graphitehost
```

Best for real-time monitoring with dedicated infrastructure.

### 2. CSV Files

```bash
./nb5 <workload> <scenario> ... --report-csv-to my_metrics_dir
```

Best for spreadsheet analysis or custom processing.

### 3. HDR Histogram Logs

```bash
./nb5 <workload> <scenario> ... --log-histograms my_hdr_metrics.log
```

Best for detailed latency analysis with HDR Histogram tools.

### 4. Metrics SQLite Database

NoSQLBench can store metrics in SQLite for querying with MetricsQL:

```bash
./nb5 <workload> <scenario> ... --report-csv-to .
```

Then query with:

```bash
./nb5 mql summary
./nb5 mql "SELECT * FROM metrics"
```

See the [MQL Reference](../../reference/apps/mql.md) for query syntax.

## What's Next?

You've run your first NoSQLBench scenario! Next steps:

- **[Reading Metrics](04-reading-metrics.md)** - Understand key metrics in detail
- **[Next Steps](05-next-steps.md)** - Choose your path forward

## Related Documentation

- **[Metrics Reference](../../reference/metrics/)** - Complete metrics documentation
- **[CLI Reference](../../reference/cli/)** - All command-line options
- **[MQL App](../../reference/apps/mql.md)** - MetricsQL for metrics analysis
