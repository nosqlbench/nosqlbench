Running Activities and Scenarios via CLI
========================================

PROG always runs a scenario script. However, there are multiple ways to tell
PROG what that script should be.

Any argument in name=value format serves as a parameter to the
script or activity that precedes it.

To create a scenario script that simply runs a single activity to completion,
use this format:
~~~
PROG activity <param>=<value> [...]
~~~

To create a scenario script that runs multiple activities concurrently,
simply add more activities to the list:
~~~
PROG activity <param>=<value> [...] activity <param>=<value> [...]
~~~

To execute a scenario script directly, simply use the format:
~~~
PROG script <scriptname> [param=value [...]]
~~~

Time & Size Units
=================
Anywhere you need to specify a time, you can use standard unit suffixes,
like "1 day", "1m", etc. Both long names and short names work in any
case. The valid time suffixes are ns, us, ms, s, m, h, d, w, y, for
nanoseconds, microseconds, milliseconds, seconds, minutes, hours,
days, weeks, or years, respectively.

As well, when specifying sizes, standard SI and IEC units apply for suffixes like
KB, KiB. For more details, see
[The Wikipedia Page](https://en.wikipedia.org/wiki/Binary_prefix).

For Scenario Designers
======================

You can build up a complex scenario by combining scripts and activities.
If this scenario needs to have some cross-shared logic, that is up to you,
the scenario designer.

## Script Parameters

Any arguments following a script in name=value form will be used to parameterize
the script. Script parameters are simply macro tokens in the form &lt;&lt;NAME:default&gt;&gt;.
All such parameters in the script will be substituted before the script executes,
so parameters may be dropped into scripts ad-hoc.

## Session Names

By using the option --session-name <name>, you can name the session logfile
that will be (over)written with execution details.
~~~
PROG --session-name testsession42
~~~

## Metric Name

If you need to see what metrics are available for a particular activity type,
you can ask PROG to instantiate an activity of that type and discover the
metrics, dumping out a list. The following form of the command shows you how
to make a list that you can copy metric names from for scripting. If you provide
an example activity alias that matches one of your scripts, you can use it exactly
as it appears.
~~~
PROG --list-metrics driver=diag alias=anexample
~~~
This will dump a list of metric names in the shortened format that is most suitable
for scenario script development. This format is required for the --list-metrics
option, but it should be familiar and easy to copy and paste from other command lines.

## Scripting on the command line

There are a few commands available on the command line to allow for basic control
of activities without having to edit the scenario script directly:

To start an activity without waiting for it to complete:
~~~
start <param>=<val> ...
~~~

To start an activity and then wait for it to complete before continuing:
~~~
run <pram>=<value> ...
~~~

To stop an activity by its alias:
~~~
stop <activity alias>
~~~

To wait for a particular activity that has been started to complete before continuing:
~~~
await <activity alias>
~~~

To wait for a number of milliseconds before continuing:
~~~
waitmillis <milliseconds>
~~~

To add a script fragment to the scenario script:
~~~
fragment '<ecmascript>...'
~~~

These can all be used on the command line in any order. The scenario script is assembled
from them before it is executed. If you want to see the resulting script, use the
 --show-script option to dump the script to the console instead of running it.

By combining these activity commands on the command line, you can construct a non-trivial
scenario from other snippets, control activity sequencing and concurrency, etc. This does
not replace what is possible for direct scripting, but it does allow for many custom
test scenarios without it. If you want to do more advanced scripting, please consult
the scenario designers guide.

