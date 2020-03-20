---
title: Scenario Scripting
---

# Scenario Scripting

## Motive

The EngineBlock runtime is a combination of a scripting sandbox and a workload execution machine. This is not accidental. With this particular arrangement, it should be possible to build sophisticated tests across a variety of scenarios. In particular, logic which can observe and react to the system under test can be powerful. With this approach, it becomes possible to break away from the conventional run-interpret-adjust cycle which is all too often done by human hands.

## Machinery, Controls & Instruments

All of the heavy lifting is left to Java and the core nosqlbench runtime. This includes the iterative workloads that are meant to test the target system. This is combined with a control layer which is provided by Nashorn and eventually GraalVM. This division of responsibility allows the high-level test logic to be "script" and the low-level activity logic to be "machinery".  While the scenario script has the most control, it also is the least busy relative to activity workloads. The net effect is that you have the efficiency of the iterative test loads in conjunction with the open design palette of a first-class scripting language.

Essentially, the ActivityType drivers are meant to handle the workload-specific machinery. They also provide dynamic control points and parameters which special to that activity type (driver). This exposes a full feedback loop between a running scenario script and the activities that it runs. The scenario is free to read the performance metrics from a running activity and make changes to it on the fly.

## Scripting Environment

The nosqlbench scripting environment provided has a few
modifications meant to streamline understanding and usage of nosqlbench dynamic parameters and metric.

### Active Bindings

Active bindings are control variables which, when assigned to, cause an immediate change in the behavior of the runtime. Each of the variables
 below is pre-wired into each script environment.

#### scenario

This is the __Scenario Controller__ object which manages the activity executors in the runtime. All the methods on this Java type are provided
to the scripting environment directly.

#### activities.&lt;alias&gt;.&lt;paramname&gt;

Each activity parameter for a given activity alias is available at this name within the scripting environment. Thus, you can change the number of threads on an activity named foo (alias=foo) in the scripting environment by assigning a value to it as in `activities.foo.threads=3`.
 Any assignments take effect synchronously before the next line of the script continues executing.

#### __metrics__.&lt;alias&gt;.&lt;metric name&gt;

Each activity metric for a given activity alias is available at this name.
This gives you access to the metrics objects directly. Some metrics objects
have also been enhanced with wrapper logic to provide simple getters and setters, like `.p99ms` or `.p99ns`, for example.

Interaction with the nosqlbench runtime and the activities therein is made easy
by the above variables and objects. When an assignment is made to any of these variables, the changes are propagated to internal listeners. For changes to _threads_, the thread pool responsible for the affected activity adjusts the number of active threads (AKA slots). Other changes are further propagated directly to the thread harnesses and components which implement the ActivityType.

:::warning
Assignment to the _workload_ and _alias_ activity parameters has no special effect, as you can't change an activity to a different driver once it has been created.
:::

You can make use of more extensive Java or Javascript libraries as needed,
mixing then with the runtime controls provided above.

## Enhanced Metrics for Scripting

The metrics available in nosqlbench are slightly different than the standard
kit with dropwizard metrics. The key differences are:

### HDR Histograms

All histograms use HDR histograms with *four* significant digits.

All histograms reset on snapshot, automatically keeping all data until you
report the snapshot or access the snapshot via scripting. (see below).

The metric types that use histograms have been replaced with nicer version for scripting. You don't have to do anything differently in your reporter config to use them. However, if you need to use the enhanced versions in your local scripting, you can. This means that Timer and Histogram types are enhanced. If you do not use the scripting extensions, then you will automatically get the standard behavior that you are used to, only with higher-resolution HDR and full snapshots for each report to your downstream metrics systems.

### Scripting with Delta Snapshots

For both the timer and the histogram types, you can call getDeltaReader(), or access it simply as &lt;metric&gt;.deltaReader. When you do this, the delta snapshotting behavior is maintained until you use the deltaReader to access it. You can get a snapshot from the deltaReader by calling getDeltaSnapshot(10000), which causes the snapshot to be reset for collection, but retains a cache of the snapshot for any other consumer of getSnapshot() for that duration in milliseconds. If, for example, metrics reporters access the snapshot in the next 10 seconds, the reported snapshot will be exactly what was used in the script.

This is important for using local scripting methods and calculations with aggregate views downstream. It means that the histograms will match up between your local script output and your downstream dashboards, as they will both be using the same frame of data, when done properly.

### Histogram Convenience Methods

All histogram snapshots have additional convenience methods for accessing every percentile in (P50, P75, P90, P95, P98, P99, P999, P9999) and every time unit in (s, ms, us, ns). For example, getP99ms() is supported, as is getP50ns(), and every other possible combination. This means that you can access the 99th percentile metric value in your scripts for activity _foo_ as _metrics.foo.cycles.snapshot.p99ms_.

## Control Flow

When a script is run, it has absolute control over the scenario runtime while it is active. Once the script reaches its end, however, it will only exit if all activities have completed. If you want to explicitly stop a script, you must stop all activities.

## Strategies

You can use nosqlbench in the classic form with `run driver=<activity_type> param=value ...` command line syntax. There are reasons, however, that you will sometimes want customize and modify your scripts directly, such as:

- Permute test variables to cover many sub-conditions in a test.
- Automatically adjust load factors to identify the nominal capacity of a system.
- Adjust rate of a workload in order to get a specific measurement of system behavior.
- React to changes in test or target system state in order to properly sequence a test.

## Script Input & Output

Internal buffers are kept for _stdin_, _stdout_, and _stderr_ for the scenario script execution. These are logged to the logfile upon script completion, with markers showing the timestamp and file descriptor (stdin, stdout, or stderr) that each line was recorded from.

## External Docs

- [Java Platform, Standard Edition Nashorn User's Guide (Java 8)](https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/api.html)
- [Nashorn extensions on OpenJDK Wiki](https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions)
- [Scripting for the Java (8) Platform](http://docs.oracle.com/javase/8/docs/technotes/guides/scripting/)
