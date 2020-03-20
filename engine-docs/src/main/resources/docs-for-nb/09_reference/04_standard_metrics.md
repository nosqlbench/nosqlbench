---
title: Standard Metrics
---

# Standard Metrics

nosqlbench comes with a set of standard metrics that will be part of every activity type (driver). Each activity type (driver) enhances the metrics available by adding their own metrics with the nosqlbench APIs. This section explains what the standard metrics are, and how to interpret them.

## read-input

Within nosqlbench, a data stream provider called an _Input_ is responsible for providing the actual cycle number that will be used by consumer threads. Because different _Input_ implementations may perform differently, a separate metric is provided to track the performance in terms of client-side overhead. The **read-input** metric is a timer that only measured the time it takes for a given activity thread to read the input value, nothing more.

## strides

A stride represents the work-unit for a thread within nosqlbench. It allows a set of cycles to be logically grouped together for purposes of optimization -- or in some cases -- to simulate realistic client-side behavior over multiple operations. The stride is the number of cycles that will be allocated to each thread before it starts iterating on them.

The **strides** timer measures the time each stride takes, including all cycles within the stride. It starts measuring time before the cycle starts, and stops measuring after the last cycle in the stride has run.

## cycles

Within nosqlbench, each logical iteration of a statement is handled within a distinct cycle. A cycle represents an iteration of a workload. This corresponds to a single operation executed according to some statement definition.

The **cycles** metric is a timer that starts counting at the start of a cycle, before any specific activity behavior has control. It stops timing once the logical cycle is complete. This includes and additional phases that are executed by multi-phase actions.




