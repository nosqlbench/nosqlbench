---
title: Command Line Options
description: How to discover commands and arguments in the nb5 CLI.
audience: user
diataxis: howto
tags:
- site
- docs
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 10
---

### The NoSQLBench CLI

Help (you're looking at it.):

```
nb5 --help
```

Short options like `-v` represent simple toggles (verbosity, etc.). Using multiples increases the
level, e.g. `-vvv`.

Long options like `--help` are top-level flags that may only be used once. These modify general
behavior or provide additional detail.

All other tokens are either commands or named arguments to commands:

- Any single word without dashes is a command that will be converted into script form.
- Any option that includes an equals sign is a named argument to the preceding command.

Example:

```
nb5 start driver=diag alias=example
```

### Discovery Options

These options help you learn more about running nb5 and about the plugins present in your build.

List additional help topics:

```
nb5 help topics
```

Activity-specific help:

```
nb5 help <activity type>
```

List available drivers, scenarios, and workloads:

```
nb5 --list-drivers
nb5 --list-scenarios
nb5 --list-workloads
```

Copy a bundled workload (or other resource) to your local directory:

```
nb5 --copy <name>
```

List metrics available for scripting:

```
nb5 --list-metrics <activity type> [<activity name>]
```

### Execution Options

These commands append script logic to the scenario; they can occur in any order/quantity. `arg=value`
arguments apply to the preceding script or activity.

Add a script (interpolating named parameters):

```
script <script file> [arg=value]...
```

Add an activity:

```
activity [arg=value]...
```

### General Options

These options modify how the scenario is run.

Set the directory for scenario log files:

```
--logs-dir <dirname>
```

Limit log files (older files are purged):

```
--logs-max <count>
```

Set logfile priority level:

```
--logs-level <level>
```

where `<level>` is one of `OFF`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE`, or `ALL`.

Override logging for specific classes:

```
--log-level-override com.foo:DEBUG,com.bar:TRACE
```

Specify logging patterns:

```
--logging-pattern '%date %level [%thread] %logger{10} [%file:%line] %msg%n'
--logging-pattern 'TERSE'
--console-pattern 'TERSE-ANSI'
```
