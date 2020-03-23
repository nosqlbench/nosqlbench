---
title: Advanced Testing
weight: 13
---

# Advanced Testing

:::info
Some of the features discussed here are only for advanced testing scenarios.
:::


## Hybrid Rate Limiting

Rate limiting is a complicated endeavor, if you want to do it well. The basic
rub is that going fast means you have to be less accurate, and vice-versa.
As such, rate limiting is a parasitic drain on any system. The act of rate
limiting is in and of itself poses a limit to the maximum rate, regardless
of the settings you pick, because this forces your system to interact with
some hardware notion of time passing, and this takes CPU cycles that could
be going to the thing you are limiting.

This means that in practice, rate limiters are often very featureless. It's
daunting enough to need rate limiting, and asking for anything more than
that is often wishful thinking. Not so in NoSQLBench.

The rate limiter in NoSQLBench provides a comparable degree of performance
and accuracy to others found in the Java ecosystem, but it *also* has advanced
features:

- Allows a sliding scale between average rate limiting and strict rate limiting.
- Internally accumulates delay time, for C.O. friendly metrics
- It is resettable and reconfigurable on the fly
- It provides its configured values in addition to performance data in metrics

## Flexible Error Handling

An emergent facility in NoSQLBench is the way that error are handled within
an activity. For example, with the CQL activity type, you are able to route
error handling for any of the known exception types. You can count errors,
you can log them. You can cause errored operations to  auto-retry if possible,
up to a configurable number of tries.

This means, that as a user, you get to decide what your test is about. Is it
about measuring some nominal but anticipated level of errors due to intentional
over-saturation? If so, then count the errors, and look at their histogram data
for timing details within the available timeout.

Are you doing a basic stability test, where you want the test to error out
for even the slightest error? You can configure for that if you need.

## Cycle Logging

It is possible to record the result status of each and every cycles in
a NoSQLBench test run. If the results are mostly homogeneous, the RLE
encoding of the results will reduce the output file down to a small
fraction of the number of cycles. The errors are mapped to ordinals, and
these ordinals are stored into a direct RLE-encoded log file. For most
testing where most of the result are simply success, this file will be tiny.
You can also convert the cycle log into textual form for other testing
and post-processing and vice-versa.

## Op Sequencing

The way that operations are planned for execution in NoSQLBench is based on
a stable ordering that is configurable. The statement forms are mixed
together based on their relative ratios. The three schemes currently supported
are round-robin with exhaustion (bucket), duplicate in order (concat), and
a way to spread each statement out over the unit interval (interval). These
account for most configuration scenarios without users having to micro-manage
their statement templates.

## Sync and Async

There are two distinct usage modes in NoSQLBench when it comes to operation
dispatch and thread management:

### Sync

Sync is the default form. In this mode, each thread reads its sequence
and dispatches one statement at a time, holding only one operation in flight
per thread. This is the mode you often use when you want to emulate an
application's request-per-thread model, as it implicitly linearizes the
order of operations within the computed sequence of statements.

### Async

In Async mode, each thread in an activity is reponsible for juggling a number
of operations in-flight. This allows a NoSQLBench client to juggle an
arbitrarily high number of connections, limited primarily by how much memory
you have.

Internally, the Sync and Async modes have different code paths. It is possible
for an activity type to support one or both of these.
