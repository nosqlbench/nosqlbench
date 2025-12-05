---
title: Example Results
description: Understand the default metrics emitted by nb5.
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
weight: 103
---

After running a simple workload, nb5 writes metrics to a log file—this is the most basic way to see
results.

## Log File Metrics

By default you’ll see a log similar to `logs/scenario_YYYYMMDD_HHMMSS.log`. Even if you don’t route
metrics elsewhere, nb5 periodically reports them to the log file and flushes the final window when
the scenario ends.

> The last report contains a partial interval of results. Only metrics that average over time (or
> compute the mean for the whole test) are meaningful in that partial window.

Example excerpt:

```
2019-08-12 15:46:00,274 INFO ... BEGIN METRICS DETAIL --
2019-08-12 15:46:00,294 INFO ... name=cql-keyvalue.cycles.config.burstrate, value=5500.0
2019-08-12 15:46:00,295 INFO ... name=cql-keyvalue.cycles.config.cyclerate, value=5000.0
2019-08-12 15:46:00,295 INFO ... name=cql-keyvalue.cycles.waittime, value=3898782735
2019-08-12 15:46:00,298 INFO ... name=cql-keyvalue.resultset-size, count=100000, ...
2019-08-12 15:46:01,703 INFO ... -- END METRICS DETAIL --
```

This log contains a lot of information, but it’s not the most desirable way to consume metrics.
Consider one of these options:

1. Send metrics to Graphite with `--report-graphite-to <host>`.
2. Record metrics to local CSV files with `--report-csv-to <directory>`.
3. Record HDR histograms with `--log-histograms <file>.log`.

See the command-line reference for more details on routing metrics to your preferred collector or
format.
