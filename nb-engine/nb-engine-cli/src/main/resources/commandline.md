# Command-Line Options

Help ( You're looking at it. )

    --help

Short options, like '-v' represent simple options, like verbosity. Using multiples increases the
level of the option, like '-vvv'.

Long options, like '--help' are top-level options that may only be used once. These modify general
behavior, or allow you to get more details on how to use ${PROG}.

All other options are either commands, or named arguments to commands. Any single word without
dashes is a command that will be converted into script form. Any option that includes an equals sign
is a named argument to the previous command. The following example is a commandline with a command *
start*, and two named arguments to that command.

    ${PROG} start driver=diag alias=example

## Discovery options

These options help you learn more about running ${PROG}, and about the plugins that are present in your
particular version.

Show version, long form, with artifact coordinates.

    --version

Get a list of additional help topics that have more detailed documentation:

    ${PROG} help topics

Provide specific help for the named activity type:

    ${PROG} help <activity type>

List the available drivers:

    --list-drivers

List the available scenarios:

    --list-scenarios

List only the available workloads which contain the above scenarios:

    --list-workloads

Copy a workload or other file to your local directory as a starting point:

    --copy <name>

Provide the metrics that are available for scripting

    --list-metrics <activity type> [ <activity name> ]

## Execution Options

This is how you actually tell ${PROG} what scenario to run. Each of these commands appends script logic
to the scenario that will be executed. These are considered as commands, can occur in any order and
quantity. The only rule is that arguments in the cmdParam=value form will apply to the preceding script
or activity.

Add the named script file to the scenario, interpolating named parameters:

    script <script file> [cmdParam=value]...

Add the named activity to the scenario, interpolating named parameters

    activity [cmdParam=value]...

## Logging options

Specify a directory for scenario log files:

    --logs-dir <dirname>

Specify a limit on logfiles (old files will be purged):

    --logs-max <count>

Specify the priority level of file logs:

    --logs-level <level>

where `<level>` can be one of OFF, ERROR, WARN, INFO, DEBUG, TRACE, or ALL

Specify an override for one or more classes:

    --log-level-override com.foobarbaz:DEBUG,com.barfoobaz:TRACE

Specify the logging pattern for console and logfile:

    --logging-pattern '%date %level [%thread] %logger{10} [%file:%line] %msg%n'
    --logging-pattern 'TERSE'

Specify the logging pattern for console only:

    --console-pattern '%date %level [%thread] %logger{10} [%file:%line] %msg%n'
    --console-pattern 'TERSE-ANSI'

Specify the logging pattern for logfile only:

    --logfile-pattern '%date %level [%thread] %logger{10} [%file:%line] %msg%n'
    --logfile-pattern 'VERBOSE'

    # See https://logging.apache.org/log4j/2.x/manual/layouts.html#Pattern_Layout
    # These shortcuts are allowed
    TERSE        %8r %-5level [%t] %-12logger{0} %msg%n%throwable
    VERBOSE      %d{DEFAULT}{GMT} [%t] %logger %-5level: %msg%n%throwable
    TERSE-ANSI   %8r %highlight{%-5level} %style{%C{1.} [%t] %-12logger{0}} %msg%n%throwable
    VERBOSE-ANSI %d{DEFAULT}{GMT} [%t] %highlight{%logger %-5level}: %msg%n%throwable
    # ANSI variants are auto promoted for console if --ansi=enable
    # ANSI variants are auto demoted for logfile in any case

## Console Options

Increase console logging levels: (Default console logging level is *warning*)

    -v         (info)
    -vv        (debug)
    -vvv       (trace)

    --progress console:1m (disables itself if -v options are used)

These levels affect *only* the console output level. Other logging level parameters affect logging
to the scenario log, stored by default in logs/...

Explicitly enable or disable ANSI logging support:
(ANSI support is enabled if the TERM environment variable is defined)

    --ansi=enabled
    --ansi=disabled

Adjust the progress reporting interval:

    --progress console:1m

or

    --progress logonly:5m

NOTE: The progress indicator on console is provided by default unless logging levels are turned up
or there is a script invocation on the command line.

## Metrics options

Specify a directory and enable CSV reporting of metrics:

    --report-csv-to <dirname>

Specify the graphite destination and enable reporting

    --report-graphite-to <addr>[:<port>]

Specify the interval for graphite or CSV reporting in seconds:

    --report-interval 10

Specify the metrics name prefix for graphite reporting:

    --metrics-prefix <metrics-prefix>

Log all HDR histogram data to a file:

    --log-histograms histodata.log
    --log-histograms 'histodata.log:.*'
    --log-histograms 'histodata.log:.*:1m'
    --log-histograms 'histodata.log:.*specialmetrics:10s'

Log HDR histogram stats to a CSV file:

    --log-histostats stats.csv
    --log-histostats 'stats.csv:.*'       # same as above
    --log-histostats 'stats.csv:.*:1m'    # with 1-minute interval
    --log-histostats 'stats.csv:.*specialmetrics:10s'

Adjust the HDR histogram precision:

    --hdr-digits 3

The default is 3 digits, which creates 1000 equal-width histogram buckets for every named metric in
every reporting interval. For longer running test or for test which require a finer grain of
precision in metrics, you can set this up to 4 or 5. Note that this only sets the global default.
Each activity can also override this value with the hdr_digits parameter. Be aware that each
increase in this number multiples the amount of detail tracked on the client by 10x, so use
caution.

If you want to add in classic time decaying histogram metrics for your histograms and timers, you
may do so with this option:

    --classic-histograms prefix
    --classic-histograms 'prefix:.*'               # same as above
    --classic-histograms 'prefix:.*specialmetrics' # subset of names

Name the current session, for logfile naming, etc By default, this will be "scenario-TIMESTAMP", and
a logfile will be created for this name.

    --session-name <name>

When this option is set, nosqlbench will start graphite, prometheus, and grafana automatically on
your local docker, configure them to work together, and point nosqlbench to send metrics the system
automatically. It also imports a base dashboard for nosqlbench and configures grafana snapshot
export to share with a central DataStax grafana instance (grafana can be found on localhost:3000
with the default credentials admin/admin).

### Metrics Labeling

Metrics have attached labels which identify which session, scenario, activity, and operation
they are attached to. Not all labels will be present, as metrics are instanced at different
levels and may or may not be op or activity specific.

By default, labels are added automatically to metrics. You can change this if needed.

    # add labels to metrics, in addition to the default ones
    --add-labels label1=value1,label2=value2,...

    # replace the initial set of labels
    --set-labels label1=value1,label2=value2,...

The default labels include appname, command, scenario, activity, op, and name.

### Summary Reporting

The classic metrics logging format is used to report results into the logfile for every scenario.
This format is not generally human-friendly, so a better summary report is provided by default to
the console and/or a specified summary file by default.

By default, summaries are always reported to a summary file in the logs directory.
It is highly recommended that you use this form in general. Users are often more interested
in seeing play-by-play high-level details on console, and more human-readable forms of metrics
summaries are easily created with other options.

Examples:

    # report to auto-named summary file for every session
    --report-summary-to _LOGS_/_SESSION_.summary

    # report to console if session ran more than 60 seconds
    --report-summary-to stdout:60

    # simply enable reporting summary to console only, same as above
    --summary

    # do both (the default)
    --report-summary-to stdout:60,_LOGS_/_SESSION_.summary

Values of `stdout` or `stderr` are send summaries directly to the console, and any other pattern is
taken as a file name.

You can use `_SESSION_` and `_LOGS_` to automatically name the file according to the current session
name and log directory.

The reason for the optional timing parameter is to allow for results of short scenario runs to be
squelched. Metrics for short runs are not generally accurate nor meaningful. Spamming the console
with boiler-plate in such cases is undesirable. If the minimum session length is not specified, it
is assumed to be 0, meaning that a report will always show on that channel.

