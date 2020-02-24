histologger extension
=====================

This extension allows you to record HDR Histogram intervals
 in the standard HistogramLogWriter format, for a matching set of histogram or timer metrics, at some interval you specify. 

### Example
~~~
histologger.logHistoIntervals("test run 42", ".*", "hdrdata.log", "0.5s");
~~~

The arguments to logHistoStats are:
**logHistoIntervals( *comment*, *regex*, *filename*, *interval* )**, where the fields are:

- comment - a session or comment name, which is required. When this API is invoked from the command line via --log-histostats, the session name is simply the name of the scenario session.
- regex - a regular expression that is used to match metric names. The value '.*' matches everything.
- filename - the name of a file to log the statistics to.
- interval - the interval size of each row written.

All matching metrics that are capable of HDR histograms (all histograms and timers in this runtime) that also match the metric name in the pattern will be logged, at the interval provided.

The format looks like this:
~~~
#logging histograms for session testing extention histostatslogger
#[Histogram log format version 1.3]
#[StartTime: 1479149958.287 (seconds since epoch), Mon Nov 14 12:59:18 CST 2016]
"StartTimestamp","Interval_Length","Interval_Max","Interval_Compressed_Histogram"
Tag=blockingactivity1.delay,0.003,0.500,0.000,HISTFAAAABl42pNpmSzMwMDAyIAKYHwm+w9QFgA8ewJ6
Tag=csvmetrics.cycles,0.003,0.503,0.000,HISTFAAAABl42pNpmSzMwMDAyIAKYHwm+w9QFgA8ewJ6
~~~
