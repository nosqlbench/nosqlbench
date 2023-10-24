---
title: CQL Wide Rows
weight: 3
---

## Description

The CQL Wide Rows workload provides a way to tax a system with wide rows of a given size. This is useful to help
understand underlying performance differences between version and configuration options when using data models that have
wide rows.

For in-depth testing, this workload needs significant density of partitions in combination with fully populated wide
rows. For exploratory or parameter contrasting tests, ensure that the rampup phase is configured correctly to establish
this initial state.

## Data Set

### baselines.widerows dataset (rampup)

- part - text, number in string form, sequentially from 1..1E9
- clust - text, number in string form, sequentially from 1..1E9
- data - text, extract from lorem ipsum between 50 and 150 characters

### baselines.widerows dataset (main)

- part - text, number in string form, sequentially from 1..1E9
- clust - text, number in string form, sequentially from 1..<partsize>
- data - text, extract from lorem ipsum between 50 and 150 characters

- machine_id - 1000 unique values
- sensor_name - 100 symbolic names, from a seed file
- time - monotonically increasing timestamp
- station_id - 100 unique values
- sensor_value - normal distribution, median 100, stddev 5.0

## Operations

### insert (rampup, main)

    insert into baselines.iot
    (machine_id, sensor_name, time, sensor_value, station_id)
    values (?,?,?,?,?)

### query (main)

    select * from baselines.iot
    where machine_id=? and sensor_name=?
    limit 10

## Workload Parameters

This workload has no adjustable parameters when used in the baseline tests.

When used for additional testing, the following parameters should be supported:

- partcount - the number of unique partitions
- partsize - the number of logical rows within a CQL partition

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

- bytes compacted over time
- pending compactions
- active data on disk
- total data on disk

## Notes on Interpretation




