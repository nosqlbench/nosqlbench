---
title: read-input
description: read-input
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 35
---

NoSQLBench comes with a set of standard metrics that are part of every driver. Each driver enhances
the metrics available by adding their own metrics with the NoSQLBench APIs. This section explains
what the standard metrics are, and how to interpret them.

## read-input

Within NoSQLBench, a data stream provider called an _Input_ is responsible for providing the actual
cycle number that will be used by consumer threads. Because different _Input_ implementations may
perform differently, a separate metric is provided to track the performance in terms of client-side
overhead. The **read-input** metric is a timer that only measured the time it takes for a given
activity thread to read the input value, nothing more.

## strides

A stride represents the work-unit for a thread within NoSQLBench. It allows a set of cycles to be
logically grouped together for purposes of optimization -- or in some cases -- to simulate realistic
client-side behavior over multiple operations. The stride is the number of cycles that will be
allocated to each thread before it starts iterating on them.

The **strides** timer measures the time each stride takes, including all cycles within the stride.
It starts measuring time before the cycle starts, and stops measuring after the last cycle in the
stride has run.

## cycles

Within NoSQLBench, each logical iteration of a statement is handled within a distinct cycle. A cycle
represents an iteration of a workload. This corresponds to a single operation executed according to
some statement definition.

The **cycles** metric is a timer that starts counting at the start of a cycle, before any specific
activity behavior has control. It stops timing once the logical cycle is complete.

## cycles.servicetime

Each cycle of an activity has a metric which measures its internal service time, measured from the
moment the cycle starts processing to the moment is fully complete. This is provided

When rate limiters are used, this sub-name identifies the _service time_ component as

## *cycles.waittime

When a rate limiter is used, the waittime metric captures the notion of scheduling delay with
respect to the requested rate. For example, if you specify a rate of 10 Kops/S, but at the 20 second
mark, only 190Kops have completed, this represents one second of scheduling delay (10 Kops worth of
operations at 10 Kops/S = 1 second). The cycles.waittime metric would thus indicate ~ 1S worth of
waittime as the workload _falling behind by about 1 second_, although it would report in nanos.

## *cycles.responsetime

When a rate limiter is used, the responsetime metric combines the servicetime and waittime values to
yield a computed responsetime. This is a measure of how long a user would have had to wait for an
operation to complete based on some ideal schedule, as described by a rate limiter. In this way, a
rate limiter acts as both a minimal and a maximal target. It is presumed that the composed system is
fast enough to run at the limited rate, thus any slow-downs which cause the system to run
effectively behind schedule represent a user-impacting effect.

## result

👉 This metric is provided directly by drivers. All conforming driver implementations should provide
this metric as described below.

Each operation's execution is tracked with the `result` timer. This timer is used to measure
**ALL** operations, even those with errors.

## result-success

👉 This metric is provided directly by drivers. All conforming driver implementations should provide
this metric as described below.

For operations which completed successfully with no exception, a separate `result-success` timer is
used. When your workload is running well, both the `result` and `result-success` timer count the
same number and rate of operations. This provides a useful cross-check between metrics.

## *-error

👉 This metric is provided directly by drivers. All conforming driver implementations should provide
this metric as described below. This happens automatically when the standard error handler
implementation is used.

When the error handler sees an exception, the name of the exception is converted to a metric name
with `-error` as the suffix. There will be one of these metric names created for each unique
exception that occurs within an activity.




