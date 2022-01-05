---
title: CQL IoT
weight: 2
---

# CQL IoT

## Description

The CQL IoT workload demonstrates a time-series telemetry system as typically found in IoT applications. The bulk of the
traffic is telemetry ingest. This is useful for establishing steady-state capacity with an actively managed data
lifecycle. This is a steady-state workload, where inserts are 90% of the operations and queries are the remaining 10%.

## Named Scenarios

### default

The default scenario for cql-iot.yaml runs the conventional test phases: schema, rampup, main

## Testing Considerations

For in-depth testing, this workload will take some time to build up data density where TTLs begin purging expired data.
At this point, the test should be considered steady-state.

## Data Set

### baselines.iot dataset (rampup,main)

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

- machines - the number of unique sources (default: 1000)
- stations - the number of unique stations (default: 100)
- limit - the limit for rows in reads (default: 10)
- expiry_minutes - the TTL for data in minutes.
- compression - enabled or disabled, to disable, set compression=''
- write_cl - the consistency level for writes (default: LOCAL_QUORUM)
- read_cl - the consistency level for reads (defaultL LOCAL_QUORUM)

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

- In order for this test to show useful performance contrasts, it has to be ramped to steady-state.
- Ingest of 1G rows yields an on-disk data density of 20.8 GB using default compression settings.




