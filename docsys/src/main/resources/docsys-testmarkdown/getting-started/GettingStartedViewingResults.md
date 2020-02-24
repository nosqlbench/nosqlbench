# Viewing Results
Coming off of our first run with DSBench, we ran a very simple workload against our database.
In that example, we saw that DSBench writes to a log file and it is in that log file where the most
basic form of metrics are displayed.

### Log File Metrics

For our previous run, we saw that DSBench was writing to `logs/scenario_20190812_154431_028.log`

Below is the full section in that log that gives us our basic metrics. There is a lot to digest here,
for now we will only focus a subset of the most important metrics.
```
2019-08-12 15:46:00,274 INFO [main] i.e.c.ScenarioResult [ScenarioResult.java:48] -- BEGIN METRICS DETAIL --
2019-08-12 15:46:00,294 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=GAUGE, name=baselines/cql-keyvalue.cycles.config.burstrate, value=5500.0
2019-08-12 15:46:00,295 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=GAUGE, name=baselines/cql-keyvalue.cycles.config.cyclerate, value=5000.0
2019-08-12 15:46:00,295 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=GAUGE, name=baselines/cql-keyvalue.cycles.waittime, value=3898782735
2019-08-12 15:46:00,298 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=HISTOGRAM, name=baselines/cql-keyvalue.resultset-size, count=100000, min=0, max=1, mean=8.0E-5, stddev=0.008943914131967056, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0
2019-08-12 15:46:00,340 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=HISTOGRAM, name=baselines/cql-keyvalue.skipped-tokens, count=0, min=0, max=0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0
2019-08-12 15:46:00,341 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=HISTOGRAM, name=baselines/cql-keyvalue.tries, count=100000, min=1, max=1, mean=1.0, stddev=0.0, median=1.0, p75=1.0, p95=1.0, p98=1.0, p99=1.0, p999=1.0
2019-08-12 15:46:00,341 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=METER, name=baselines/cql-keyvalue.rows, count=8, mean_rate=0.33513484972659807, m1=0.36684141626782935, m5=0.39333484605698305, m15=0.3977778345542248, rate_unit=events/second
2019-08-12 15:46:00,589 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.bind, count=100000, min=1.582, max=23439.359, mean=12.56832522, stddev=341.12259029628433, median=3.969, p75=5.733, p95=14.857, p98=25.578, p99=35.727, p999=97.487, mean_rate=4142.45514275983, m1=3508.0300578687047, m5=3299.8619559559247, m15=3260.8242490944554, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:00,826 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.cycles.servicetime, count=100000, min=348012.544, max=3849846.783, mean=2565961.07337728, stddev=796189.5358718627, median=2535587.839, p75=3299737.599, p95=3665297.407, p98=3743154.175, p99=3759669.247, p999=3807510.527, mean_rate=4133.36694607174, m1=3637.62940362701, m5=3458.3041653186974, m15=3424.659562378474, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:00,935 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.execute, count=100000, min=3.486, max=12572.159, mean=21.37352134, stddev=147.5872262658514, median=12.455, p75=21.65, p95=45.701, p98=69.079, p99=105.123, p999=695.103, mean_rate=4084.755592762048, m1=3511.6350453271425, m5=3304.558546576714, m15=3265.7216557117335, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:00,943 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.pages, count=0, min=0.0, max=0.0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,090 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.phases.servicetime, count=100000, min=444.0, max=381059.071, mean=3798.41135488, stddev=10790.998109403186, median=1899.647, p75=3679.999, p95=10174.975, p98=15896.575, p99=24294.399, p999=136609.791, mean_rate=4089.258228031301, m1=3638.711481830029, m5=3459.480755773593, m15=3425.8517756084334, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,171 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.read_input, count=10050, min=0.1, max=39045.119, mean=35.44877721393035, stddev=945.8304421638578, median=0.874, p75=1.039, p95=2.036, p98=4.114, p99=11.585, p999=16249.343, mean_rate=409.40825761884753, m1=367.86916182353934, m5=350.83483186356915, m15=347.63927014428833, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,310 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.result, count=100000, min=233.48, max=358596.607, mean=3732.00338612, stddev=10254.850416061185, median=1874.815, p75=3648.767, p95=10115.071, p98=15855.615, p99=23916.543, p999=111292.415, mean_rate=4024.0234405430424, m1=3514.053841156124, m5=3307.431472596865, m15=3268.6786509004132, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,452 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.result-success, count=100000, min=435.168, max=358645.759, mean=3752.40990808, stddev=10251.524945886964, median=1889.791, p75=3668.479, p95=10154.495, p98=15884.287, p99=24280.063, p999=111443.967, mean_rate=4003.3090048756894, m1=3523.40328629036, m5=3318.8463896065778, m15=3280.480326762243, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,460 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.retry-delay, count=0, min=0.0, max=0.0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,605 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.strides.servicetime, count=10000, min=10772.48, max=627572.735, mean=117668.1484032, stddev=72863.5379858271, median=106024.959, p75=144965.631, p95=236265.471, p98=304971.775, p99=450625.535, p999=613449.727, mean_rate=399.8614604956103, m1=355.11611665744687, m5=337.61623765618054, m15=334.0569514490176, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,702 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.tokenfiller, count=22208, min=1011.328, max=71806.975, mean=1135.442975051788, stddev=784.3016082392345, median=1076.799, p75=1087.807, p95=1207.103, p98=1454.015, p99=2429.567, p999=8490.495, mean_rate=879.7500537541893, m1=833.0176653507624, m5=814.1230871081734, m15=810.3570012336148, rate_unit=events/second, duration_unit=microseconds
2019-08-12 15:46:01,703 INFO [main] i.e.c.ScenarioResult [ScenarioResult.java:56] -- END METRICS DETAIL --
```

#### results
This is the primary metric that should be used to get a quick idea of the throughput and latency for a given run. It encapsulates the entire operation life cycle ( ie. bind, execute, get result back ).

For this example we see that we averaged 3732 operations / second with 3.6ms 75th percentile latency and 23.9ms 99th percentile latency. Note the raw metrics are in microseconds.

```
2019-08-12 15:46:01,310 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.result, count=100000, min=233.48, max=358596.607, mean=3732.00338612, stddev=10254.850416061185, median=1874.815, p75=3648.767, p95=10115.071, p98=15855.615, p99=23916.543, p999=111292.415, mean_rate=4024.0234405430424, m1=3514.053841156124, m5=3307.431472596865, m15=3268.6786509004132, rate_unit=events/second, duration_unit=microseconds
```

#### result-success
This metric shows whether there were any errors during the run. You should confirm that the count is equal to the number of cycles for the run.

Here we see that all 100k of our cycles succeeded. Note that the metrics for throughput and latency here are slightly different than the `results` metric simply because this is a separate timer around success only results.

```
2019-08-12 15:46:01,452 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=TIMER, name=baselines/cql-keyvalue.result-success, count=100000, min=435.168, max=358645.759, mean=3752.40990808, stddev=10251.524945886964, median=1889.791, p75=3668.479, p95=10154.495, p98=15884.287, p99=24280.063, p999=111443.967, mean_rate=4003.3090048756894, m1=3523.40328629036, m5=3318.8463896065778, m15=3280.480326762243, rate_unit=events/second, duration_unit=microseconds
```

#### resultset-size
For read workloads, this metric shows the size of result sent back to DSBench from the server. This is useful to confirm that you are reading rows that already exist in the database.

TODO: talk about mix of read / writes and how that affects this metric
```
2019-08-12 15:46:00,298 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=HISTOGRAM, name=baselines/cql-keyvalue.resultset-size, count=100000, min=0, max=1, mean=8.0E-5, stddev=0.008943914131967056, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0
```

#### tries
DSBench will retry failures 10 times by default, this is configurable via the `maxtries` command line option < link >. This metric shows a histogram of the number of tries that each operation required, in this example, there were no retries as the `count` is 100k.
```
2019-08-12 15:46:00,341 INFO [main] i.e.c.ScenarioResult [Slf4jReporter.java:373] type=HISTOGRAM, name=baselines/cql-keyvalue.tries, count=100000, min=1, max=1, mean=1.0, stddev=0.0, median=1.0, p75=1.0, p95=1.0, p98=1.0, p99=1.0, p999=1.0
```

### More Metrics

DSBench extends many ways to report the metrics from a run. To get more information on the options, see the doc links below.
- Interpreting Metrics < link >
- Built-in Docker Dashboard < link >
- Reporting to CSV < link >
- Reporting to Graphite < link >
- Reporting to HDR < link >

### Congratulations
You have completed your first run with DSBench! Let's head over to the Next Steps section < link > to talk about the possibilities that are now at our fingertips.