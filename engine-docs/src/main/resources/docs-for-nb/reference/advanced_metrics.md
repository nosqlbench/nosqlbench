---
title: Advanced Metrics
---

# Advanced Metrics

## Unit of Measure

All metrics collected from activities are recorded in nanoseconds and ops per second. All histograms are recorded with 4
digits of precision using HDR histograms.

## Metric Outputs

Metrics from a scenario run can be gathered in multiple ways:

- In the log output
- In CSV files
- In HDR histogram logs
- In Histogram Stats logs (CSV)
- To a monitoring system via graphite
- via the --docker-metrics option

With the exception of the `--docker-metrics` approach, these forms may be combined and used in combination. The command
line options for enabling these are documented in the built-in help, although some examples of these may be found below.

## Metrics via Graphite

If you like to have all of your testing data in one place, then you may be interested in reporting your measurements to
a monitoring system. For this, nosqlbench includes a
[Metrics Library](https://github.com/dropwizard/metrics). Graphite reporting is baked in as the default reporter.

In order to enable graphite reporting, use one of these options formats:

    --report-graphite-to <host>
    --report-graphite-to <host>:<port>

## Metric Naming

## Prefix

Core metrics use the prefix _nosqlbench_ by default. You can override this with the ``--metrics-prefix` option:

    --metrics-prefix myclient.group5

## Identifiers

Metrics associated with a specific activity will have the activity alias in their name. There is a set of core metrics
which are always present regardless of the activity type. The names and types of additional metrics provided for each
activity type vary.

Sometimes, an activity type will expose metrics on a per statement basis, measuring over all invocations of a given
statement as defined in the YAML. In these cases, you will see `--` separating the name components of the metric. At the
most verbose, a metric name could take on the form like
`<activity>.<docname>--<blockname>--<statementname>--<metricname>`, although this is rare when you name your statements,
which is recommended. Just keep in mind that the double dash connects an activity's alias with named statements *within*
that activity.

## HDR Histograms

### Recording HDR Histogram Logs

You can record details of histograms from any compatible metric (histograms and timers) with an option like this:

    --log-histograms hdrdata.log

If you want to record only certain metrics in this way, then use this form:

    --log-histograms 'hdrdata.log:.*suffix'


Notice that the option is enclosed in single quotes. This is because the second part of the option value is a regex. The
'.*suffix' pattern matches any metric name that ends with "suffix". Effectively, leaving out the pattern is the same as
using '.\*', which matches all metrics. Any valid regex is allowed here.

Metrics may be included in multiple logs, but care should be taken not to overdo this. Keeping higher fidelity histogram
reservoirs does come with a cost, so be sure to be specific in what you record as much as possible.

If you want to specify the recording interval, use this form:

    --log-histograms 'hdrdata.log:.*suffix:5s'

If you want to specify the interval, you must use the third form above, although it is valid to leave the pattern empty,
such as 'hdrdata.log::5s'.

Each interval specified will be tracked in a discrete reservoir in memory, so they will not interfere with each other in
terms of accuracy.

### Recording HDR Histogram Stats

You can also record basic snapshots of histogram data on a periodic interval just like above with HDR histogram logs.
The option to do this is:

    --log-histostats 'hdrstats.log:.*suffix:10s'

Everything works the same as for hdr histogram logging, except that the format is in CSV as shown in the example below:

~~~
#logging stats for session scenario-1479089852022
#[Histogram log format version 1.0]
#[StartTime: 1479089852.046 (seconds since epoch), Sun Nov 13 20:17:32 CST 2016]
#Tag,Interval_Start,Interval_Length,count,min,p25,p50,p75,p90,p95,p98,p99,p999,p9999,max
Tag=diag1.delay,0.457,0.044,1,16,31,31,31,31,31,31,31,31,31,31
Tag=diag1.cycles,0.48,0.021,31,4096,8191,8191,8191,8191,8191,8191,8191,8191,8191,2097151
Tag=diag1.delay,0.501,0.499,1,1,1,1,1,1,1,1,1,1,1,1
Tag=diag1.cycles,0.501,0.499,498,1024,2047,2047,4095,4095,4095,4095,4095,4095,4095,4194303
...
~~~

This includes the metric name (Tag), the interval start time and length (from the beginning of collection time), number
of metrics recorded (count), minimum magnitude, a number of percentile measurements, and the maximum value. Notice that
the format used is similar to that of the HDR logging, although instead of including the raw histogram data, common
percentiles are recorded directly.

