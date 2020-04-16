---
title: CQL Key-Value
weight: 1
---

## Description

The CQL Key-Value workload demonstrates the simplest possible schema with payload data. This is useful for measuring
system capacity most directly in terms of raw operations. As a reference point, provides some insight around types of
workloads that are constrained around messaging, threading, and tasking, rather than bulk throughput.

During preload, all keys are set with a value. During the main phase of the workload, random keys from the known
population are replaced with new values which never repeat. During the main phase, random partitions are selected for
upsert, with row values never repeating.

## Operations

### insert (rampup, main)

    insert into baselines.keyvalue (key, value) values (?,?);

### read (main)

    select * from baselines.keyvalue where key=?key;

## Data Set

### baselines.keyvalue insert (rampup)

- key - text, number as string, selected sequentially up to keycount
- value - text, number as string, selected sequentially up to valuecount

### baselines.keyvalue insert (main)

- key - text, number as string, selected uniformly within keycount
- value - text, number as string, selected uniformly within valuecount

### baselines.keyvalue read (main)

- key - text, number as string, selected uniformly within keycount

## Workload Parameters

This workload has no adjustable parameters when used in the baseline tests.

When used for additional testing, the following parameters should be supported:

- keycount - the number of unique keys
- valuecount - the number of unique values

## Key Performance Metrics

Client side metrics are a more accurate measure of the system behavior from a user's perspective. For microbench and
baseline tests, these are the only required metrics. When gathering metrics from multiple server nodes, they should be
kept in aggregate form, for min, max, and average for each time interval in monitoring. For example, the avg p99 latency
for reads should be kept, as well as the min p99 latency for reads. If possible metrics, should be kept in plot form,
with discrete histogram values per interval.

### Client-Side

- read ops/s
- write ops/s
- read latency histograms
- write latency histograms
- exception counts

### Server-Side

- pending compactions
- bytes compacted
- active data on disk
- total data on disk

# Notes on Interpretation

Once the average ratio of overwrites starts to balance with the rate of compaction, a steady state should be achieved.
At this point, pending compactions and bytes compacted should be mostly flat over time.
