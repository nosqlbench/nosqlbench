+++
title = "CLI Scripting"
description = "Command line scenario scripting reference"
weight = 20
template = "docs-page.html"

[extra]
quadrant = "reference"
topic = "cli"
category = "scripting"
tags = ["cli", "scripting", "scenarios", "automation"]
+++

Sometimes you want to run a set of workloads in a particular order, or call other specific test
setup logic in between activities or workloads. While the full scripting environment allows you
to do this and more, it is not necessary to write JavaScript for every scenario.

For more basic setup and sequencing needs, you can achieve a fair degree of flexibility on the
command line. A few key API calls are supported directly on the command line. This guide explains
each of them, what the do, and how to use them together.

## Script Construction

As the command line is parsed, from left to right, the scenario script is built in an internal
scripting buffer. Once the command line is fully parsed, this script is executed. Each of the
commands below is effectively a macro for a script fragment. It is important to remember that
order is important.

## Command line format

Newlines are not allowed when building scripts from the command line. As long as you follow the
allowed forms below, you can simply string multiple commands together with spaces between.
Single word options without leading dashes, like `run`, are scenario commands. Subsequent
`key=value` style arguments are their named parameters. Named parameters which follow a scenario
command apply to that command only.

Global options, meaning any argument that starts with a `-` or `--`, are applied when NoSQLBench
starts up, before any scenario is run. These are automatically taken out of the list of what the
scenario scripting sees.

## Concurrency & Control

All activities operate parallel to the scenario script, allowing them to run independently at
their own pace, while the scenario controls and manages them like an automaton. The scenario concludes
only when both the scenario script and the activities are finished.

## Scenario Commands

### start

example: `start driver=<driver> alias=<alias> ...`

You can start an activity with this command. At the time this command is evaluated, the activity is
started, and the script continues without blocking. This is an asynchronous start of an activity. If
you start multiple activities in this way, they will run concurrently.

The driver argument is required to identify which nb5 driver to run. The alias parameter is not
strictly required, unless you want to be able to interact with the started activity later. In any
case, it is a good idea to name all your activities with a meaningful alias.

### await

example: `await <alias>`

Await the normal completion of an activity with the given alias. This causes the scenario script to
pause while it waits for the named activity to finish. This does not tell the activity to stop. It
simply puts the scenario script into a paused state until the named activity is complete.

### run

example: `run driver=<driver> alias=<alias> ...`

Run an activity to completion, waiting until it is complete before continuing with the scenario
script. It is effectively the same as

    start driver=<activity type> ... alias=<alias>
    await <alias>

### stop

example: `stop <alias>`

Stop an activity with the given alias. This is synchronous, and causes the scenario to pause until
the activity is stopped. This means that all threads for the activity have completed and signalled
that they're in a stopped state. This command allows an activity to stop gracefully if possible.
It waits for a number of seconds for all threads to come to a stopped state and will then resort
to using forceStop if needed. Threads which are occupied blocking on remote timeouts or blocking
behavior can prevent an activity from shutting down gracefully.

### forcestop

syntax: `forcestop <alias>`

This is like the stop command, except that it doesn't allow the activity to shut down gracefully.
This command immediately shutdown down the thread pool for a given activity.

### waitmillis

example: `waitmillis <milliseconds>`

Pause the scenario script for this many milliseconds. This doesn't affect any running activities
directly. This is useful for controlling workload run duration, etc.

### script

example: `script <script file>`

Add the contents of the named file to the scenario script buffer.

### fragment

example: `fragment <script text>`

Add the contents of the next argument to the scenario script buffer.

# An example CLI script

Any sequence of these commands, when strung together, constitutes a scenario script.
An example of this is: `./nb5 start driver=stdout alias=a cycles=100K workload=cql-iot tags=block:main start driver=stdout alias=b cycles=200K workload=cql-iot tags=block:main waitmillis 10000 await a stop b`

This is terribly confusing to look at, so we do something like this instead. The backslashes at the end allow you to insert a discarded newline, _as long as there are no
spaces after the backslash._

```shell,linenos
./nb5 \
start driver=stdout alias=a cycles=100K workload=cql-iot tags=block:main \
start driver=stdout alias=b cycles=200K workload=cql-iot tags=block:main \
waitmillis 10000 \
await a \
stop b
```

Here is a narrative of what happens for each line:

```linenos
# nb5 is invoked
# An activity named 'a' is started, with 100K cycles of work.
# An activity named 'b' is started, with 200K cycles of work.
# While these activities run, the scenario script waits for ten seconds.
# The scenario blocks, waiting for activity 'a' to complete its 100K cycles.
# Activity 'b' is immediately stopped.
```

After the stop command at the end of the scenarios script, the whole scenario exits, because all
activities are stopped or complete, **and** the script is complete.
