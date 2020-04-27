---
title: High Fidelity Metrics
weight: 12
---

# High-Fidelity Metrics

Since NoSQLBench has been built as a serious testing tool for all users,
some attention was necessary on the way metric are used.

## Discrete Reservoirs

In NoSQLBench, we avoid the use of time-decaying metrics reservoirs.
Internally, we use HDR reservoirs with discrete time boundaries. This is
so that you can look at the min and max values and know that they apply
accurately to the whole sampling window.

## Metric Naming

All running activities have a symbolic alias that identifies them for the
purposes of automation and metrics. If you have multiple activities
running concurrently, they will have different names and will be
represented distinctly in the metrics flow.

## Precision and Units

By default, the internal HDR histogram reservoirs are kept at 4 digits of
precision. All timers are kept at nanosecond resolution.

## Metrics Reporting

Metrics can be reported via graphite as well as CSV, logs, HDR logs, and
HDR stats summary CSV files.

## Coordinated Omission

The metrics naming and semantics in NoSQLBench are setup so that you can
have coordinated omission metrics when they are appropriate, but there are
no there changes when they are not. This means that the metric names and
meanings remain stable in any case.

Particularly, NoSQLBench avoids the term "latency" altogether as it is
often overused and thus prone to confusing people.

Instead, the terms `service time`, `wait time`, and `response time` are
used. These are abbreviated in metrics as `servicetime`, `waittime`, and
`responsetime`.

The `servicetime` metric is the only one which is always present. When a
rate limiter is used, then additionally `waittime` and `responsetime` are
reported.


