+++
title = "Scaling Up Testing"
description = "A blueprint for incremental scale-up testing methodology"
weight = 40
template = "page.html"

[extra]
quadrant = "guides"
topic = "testing"
category = "scale"
tags = ["scaling", "methodology", "testing", "performance"]
+++

This is a simple high-level overview of _a_ scale-up testing method, for those who are new to it
and need a basic blueprint.

Testing for scale is often a complicated business. Often, users think that it is too expensive
because a test at scale requires many systems. This is a not often the case. You can establish
the relationships between testing resources and results on any scale. Further, you can establish
the character of scalability at small scales at first and then go from there. This is often the
best approach in terms of incremental results.

## Start Small

When testing for scale, it is useful to establish your testing method at some arbitrarily small
size of system and go from there. This allows you to prove out your testing apparatus and target
system configuration in an affordable and easier-to-manage way. While a small system may not
show you the _scaled up_ performance that you want to measure, it gives you the first reference
point you need in order to verify _how_ a system scales.

## Select Key Metrics

You can only establish the character of scaling by plotting multiple points between an
independent and a dependent variable. At the highest level, the independent variable is
"hardware" from a system scaling perspective, and "investment" from an TCO perspective. Most
users will need to focus on how much hardware is needed to meet a given performance requirement,
SLA, or throughput (or both). You must know the fundamental questions you are asking for the
test to be framed and represented properly.

Metrics which are most often used include:
- response time, one of (or all of)
  - p99 response time (historically called _latency_, but see the timing terminology reference)
  - read-specific p99 response time, as read patterns in systems are often much more indicative
    of overally scaling character
- Dataset Size
- Saturating throughput
- p99 response time at some nominal throughput
- data density

## Nominal vs. Saturating throughput

Operational systems are not run in production at 100% utilization. As an _operational system_,
properly managed, they will be deployed to scale over the demand with some added headroom. This
headroom is crucial to allow for self-healing or administrative events at the control plane layer
while maintaining the workload at some acceptable level (throughput and latency).

Realistic measurement of a production-like system in  depends on knowing both what throughput that
system is capable of and how it responds at a realistic loading level. Running a system in
production at 10% utilization is not cost-effective. Conversely, running a system at 99%
utilization is not effective for maintaining availability through variable load, hardware
failures, or administrative actions. So how do you find the level of throughput to test at?

A basic recipe consists of:

1. Run the workload at a reasonable concurrency and no rate throttling and measure the
   _saturating_ throughput.
2. Select a proportional loading level which represents what you would do in production. For
   example, for a headroom of 30%, you might select a loading level of 70%
3. Run your main workload at the proportional rate using client-side rate-limiting, and take
   your response time measurements from this run.

## Scale Up

Once you have established your metrics at nominal throughput on your baseline system, you are
ready to scale up your test. For the scaling mode that you are testing, change an independent
variable (add a node, add cores, or whatever constitutes a _resource_ in your target system).

Repeat the test flow above with a different set of parameters. Once you get two sets of results,
you have enough to start characterizing scalability.

In general, distributed systems which are designed to scale focus on maintaining a given
response time character when the proportion of resources is congruent to the rate of requests.
Other scaling modes are available, but details vary by system, so be clear about what you are
testing and why.

## Explain Your Results

It is crucial that you frame the fundamental premise of your test in any results.

This includes key details, like:

- What questions you aim to answer - What is the purpose of the test?
- A high-level outline of the testing method or workflow used.
- Details on how metrics are measured
  (see [Vantage Points](vantage-points.md))
- The basic formulae (like the nominal vs saturating rates above)
- a direct and uncomplicated visual about the fundamental relationship between
the test parameters
- Full details of the system and workload used for testing. Even if you don't provide this
  up-front it has to be readily available when asked for any result to be taken seriously.
