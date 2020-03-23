---
title: 03 Metrics
weight: 4
---

# Example Metrics

A set of core metrics are provided for every workload that runs with nosqlbench,
 regardless of the activity type and protocol used. This section explains each of
  these metrics and shows an example of them from the log file.

## metric: result

This is the primary metric that should be used to get a quick idea of the
throughput and latency for a given run. It encapsulates the entire
operation life cycle ( ie. bind, execute, get result back ).

For this example we see that we averaged 3732 operations / second with 3.6ms
 75th percentile latency and 23.9ms 99th percentile latency. Note the raw metrics are
  in microseconds. This duration_unit may change depending on how a user configures
  nosqlbench, so always double-check it.

```
2019-08-12 15:46:01,310 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=cql-keyvalue.result, count=100000, min=233.48, max=358596.607, mean=3732.00338612, stddev=10254.850416061185, median=1874.815, p75=3648.767, p95=10115.071, p98=15855.615, p99=23916.543, p999=111292.415, mean_rate=4024.0234405430424, m1=3514.053841156124, m5=3307.431472596865, m15=3268.6786509004132, rate_unit=events/second, duration_unit=microseconds
```

## metric: result-success

This metric shows whether there were any errors during the run. You can confirm that
 the count is equal to the number of cycles for the run if
  you are expecting or requiring zero failed operations.

Here we see that all 100k of our cycles succeeded. Note that the metrics for throughput
and latency here are slightly different than the `results` metric simply because this
 is a separate timer that only includes operations which completed with no exceptions.

```
2019-08-12 15:46:01,452 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=cql-keyvalue.result-success, count=100000, min=435.168, max=358645.759, mean=3752.40990808, stddev=10251.524945886964, median=1889.791, p75=3668.479, p95=10154.495, p98=15884.287, p99=24280.063, p999=111443.967, mean_rate=4003.3090048756894, m1=3523.40328629036, m5=3318.8463896065778, m15=3280.480326762243, rate_unit=events/second, duration_unit=microseconds
```

## metric: resultset-size

For read workloads, this metric shows the size of result sent back to nosqlbench
from the server. This is useful to confirm that you are reading rows that already
exist in the database.

```
2019-08-12 15:46:00,298 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=HISTOGRAM, name=cql-keyvalue.resultset-size, count=100000, min=0, max=1, mean=8.0E-5, stddev=0.008943914131967056, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0
```

#### metric: tries

nosqlbench will retry failures 10 times by default, this is configurable via the `maxtries` command line
option for the cql activity type. This metric shows a histogram of the number of tries that each operation
required, in this example, there were no retries as the `count` is 100k.
```
2019-08-12 15:46:00,341 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=HISTOGRAM, name=cql-keyvalue.tries, count=100000, min=1, max=1, mean=1.0, stddev=0.0, median=1.0, p75=1.0, p95=1.0, p98=1.0, p99=1.0, p999=1.0
```

### More Metrics

nosqlbench extends many ways to report the metrics from a run, including:

- Built-in Docker Dashboard
- Reporting to CSV
- Reporting to Graphite
- Reporting to HDR


To get more information on these options, see the output of

    ./nb --help

### Congratulations

You have completed your first run with nosqlbench!

In the 'Next Steps' section, you'll find options for how to continue, whether you are looking
for basic testing or something more advanced.

