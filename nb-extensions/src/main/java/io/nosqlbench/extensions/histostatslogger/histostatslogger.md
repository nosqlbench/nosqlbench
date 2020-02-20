histostatslogger extension
==========================

This extension allows you to record periodic histogram stats for
a matching set of histogram or timer metrics, at some interval you specify. 

### Example
~~~
histostatslogger.logHistoStats("test run 42", ".*", "histostats.csv", "0.5s");
~~~

The arguments to logHistoStats are:
**logHistoStats( *comment*, *regex*, *filename*, *interval* )**, where the fields are:

- comment - a session or comment name, which is required. When this API is invoked from the command line via --log-histostats, the session name is simply the name of the scenario session.
- regex - a regular expression that is used to match metric names. The value '.*' matches everything.
- filename - the name of a file to log the statistics to.
- interval - the interval size of each row written.

All matching metrics that are capable of HDR histograms (all histograms and timers in this runtime) that also match the metric name in the pattern will be logged, at the interval provided.

The format looks like this, similar to that of an HdrHistogram log,
with the same support for tags:
~~~
#logging stats for session testing extention histostatslogger
#[Histogram log format version 1.0]
#[StartTime: 1479151175.380 (seconds since epoch), Mon Nov 14 13:19:35 CST 2016]
#Tag,Interval_Start,Interval_Length,count,min,p25,p50,p75,p90,p95,p98,p99,p999,p9999,max
Tag=testhistostatslogger.delay,0.047,0.457,1,16,31,31,31,31,31,31,31,31,31,31
Tag=testhistostatslogger.cycles,0.076,0.430,4490,1024,8191,8191,8191,8191,8191,8191,8191,8191,8191,8388607
...
~~~
