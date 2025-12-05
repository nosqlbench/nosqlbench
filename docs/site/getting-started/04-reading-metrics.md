---
title: Reading Metrics
description: Interpret the default metrics timers and histograms.
audience: user
diataxis: howto
tags:
- site
- docs
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 104
---

NoSQLBench emits a core set of metrics for every workload. This page explains each metric and shows
sample log output.

## `result`

The primary metric for throughput and latency. It encapsulates the entire operation life cycle
(bind, execute, receive result).

```
... type=TIMER, name=cql-keyvalue.result, count=100000, min=233.48, max=358596.607, ...
```

In this example we averaged ~3,700 ops/sec with 3.6 ms P75 latency and 23.9 ms P99 latency. Durations
are in microseconds (check `duration_unit`).

## `result-success`

Shows only successful operations. The count should match the cycle count when no errors occur.
Throughput/latency differ slightly because retries are excluded.

```
... type=TIMER, name=cql-keyvalue.result-success, count=100000, ...
```

## `resultset-size`

For read workloads this histogram shows the size of each result returned from the server, useful for
confirming you are reading rows that already exist.

```
... type=HISTOGRAM, name=cql-keyvalue.resultset-size, count=100000, ...
```

## `tries`

NoSQLBench retries failures up to 10 times by default (configurable with `maxtries`). This histogram
shows how many attempts each operation required. In this example, every op succeeded on the first try.

```
... type=HISTOGRAM, name=cql-keyvalue.tries, count=100000, min=1, max=1, ...
```

## More Metrics

NoSQLBench can report metrics to several destinations:

- Built-in Docker dashboard
- CSV files (`--report-csv-to <dir>`)
- Graphite (`--report-graphite-to <host>`)
- HDR logs (`--log-histograms <file>`)

Run `./nb5 --help` for details on each reporting option.

## Congratulations

You’ve completed your first run with NoSQLBench! Continue to the next page for suggested next steps.
