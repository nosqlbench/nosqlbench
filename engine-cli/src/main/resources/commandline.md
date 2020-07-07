### Command-Line Options ###

Help ( You're looking at it. )

    --help

Short options, like '-v' represent simple options, like verbosity.
Using multiples increases the level of the option, like '-vvv'.

Long options, like '--help' are top-level options that may only be
used once. These modify general behavior, or allow you to get more
details on how to use PROG.

All other options are either commands, or named arguments to commands.
Any single word without dashes is a command that will be converted
into script form. Any option that includes an equals sign is a
named argument to the previous command. The following example
is a commandline with a command *start*, and two named arguments
to that command.

    PROG start driver=diag alias=example

### Discovery options ###

These options help you learn more about running PROG, and
about the plugins that are present in your particular version.

Get a list of additional help topics that have more detailed
documentation:

    PROG help topics

Provide specific help for the named activity type:

    PROG help <activity type>

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

### Execution Options ###

This is how you actually tell PROG what scenario to run. Each of these
commands appends script logic to the scenario that will be executed.
These are considered as commands, can occur in any order and quantity.
The only rule is that arguments in the arg=value form will apply to
the preceding script or activity.

Add the named script file to the scenario, interpolating named parameters:

    script <script file> [arg=value]...

Add the named activity to the scenario, interpolating named parameters

    activity [arg=value]...

### General options ###

These options modify how the scenario is run.

Specify a directory for scenario log files:

    --logs-dir <dirname>

Specify a limit on logfiles (old files will be purged):

    --logs-max <count>

Specify the priority level of file logs:

    --logs-level <level>

where `<level>` can be one of OFF, ERROR, WARN, INFO, DEBUG, TRACE, or ALL

Specify an override for one or more classes:

    --log-level-override com.foobarbaz:DEBUG,com.barfoobaz:TRACE

Specify the logging pattern:

    --with-logging-pattern '%date %level [%thread] %logger{10} [%file:%line] %msg%n'

    ( default: '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n' )
    ( See https://logback.qos.ch/manual/layouts.html#ClassicPatternLayout for format options )

Specify a directory and enable CSV reporting of metrics:

    --report-csv-to <dirname>

Specify the graphite destination and enable reporting

    --report-graphite-to <addr>[:<port>]

Specify the interval for graphite or CSV reporting in seconds (default: 10)

    --report-interval <interval-seconds>

Specify the metrics name prefix for graphite reporting

    --metrics-prefix <metrics-prefix>

Log all HDR histogram data to a file

    --log-histograms histodata.log
    --log-histograms 'histodata.log:.*'
    --log-histograms 'histodata.log:.*:1m'
    --log-histograms 'histodata.log:.*specialmetrics:10s'

Log HDR histogram stats to a CSV file

    --log-histostats stats.csv
    --log-histostats 'stats.csv:.*'       # same as above
    --log-histostats 'stats.csv:.*:1m'    # with 1-minute interval
    --log-histostats 'stats.csv:.*specialmetrics:10s'

Adjust the progress reporting inverval

    --progress console:10s

or

    --progress logonly:5m

If you want to add in classic time decaying histogram metrics
for your histograms and timers, you may do so with this option:

    --classic-histograms prefix
    --classic-histograms 'prefix:.*'               # same as above
    --classic-histograms 'prefix:.*specialmetrics' # subset of names


Name the current session, for logfile naming, etc
By default, this will be "scenario-TIMESTAMP", and a logfile will be created
for this name.

    --session-name <name>

Enlist nosqlbench to stand up your metrics infrastructure using a local docker runtime:

    --docker-metrics

When this option is set, nosqlbench will start graphite, prometheus, and grafana automatically
on your local docker, configure them to work together, and point nosqlbench to send metrics
the system automatically. It also imports a base dashboard for nosqlbench and configures grafana
snapshot export to share with a central DataStax grafana instance (grafana can be found on localhost:3000
with the default credentials admin/admin).


### Console Options ###
Increase console logging levels: (Default console logging level is *warning*)

    -v         (info)
    -vv        (debug)
    -vvv       (trace)

    --progress console:1m (disables itself if -v options are used)

These levels affect *only* the console output level. Other logging level
parameters affect logging to the scenario log, stored by default in logs/...

Show version, long form, with artifact coordinates.

    --version
