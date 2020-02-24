csvmetrics extension
====================

This extension makes it easy to configure csv metrics to be logged to a path with a metrics reporter.

### Examples
Explicitly control the timing of logging:
~~~
var csvlogger=csvmetrics.log("metrics.csv");
csvlogger.report();
~~~
This example creates a csv logger but does not start it with automatic reporting. Also, since no metrics were explicitly added, all metrics are included by default.

Automate the reporting at some interval:
~~~
var csvlogger=csvmetrics.log("metrics.csv",30,"SECONDS");
csvlogger.add(metrics.myactivity.cycles)
~~~
In this form, a logging thread is started at the interval specified
by the second and third option. It also has exactly one metric, as adding a metric to it directly overrides the *match all metrics* default behavior.

It is also possible to start a csv logger without scheduled reporting, then to start it at some interval determined later:
~~~
var csvlogger=csvmetrics.log("metrics.csv");
csvlogger.add(metrics.myactivity.cycles)
csvlogger.start(30,"SECONDS");
~~~

The third column must be the name of a TimeUnit from java.util.concurrent.TimeUnit, namely one of "NANOSECONDS", "MICROSECONDS", "MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS", although effective values lower than 5 seconds should be avoided in most cases. 
