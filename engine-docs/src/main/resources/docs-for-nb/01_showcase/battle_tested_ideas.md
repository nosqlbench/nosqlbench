---
title: Core Concepts
weight: 2
---

# Refined Core Concepts

The core concepts that NoSQLBench is built on have been scrutinized,
replaced, refined, and hardened through several years of use
by users of various needs and backgrounds.

This is important when trying to find a way to express common patterns
in what is often a highly fragmented practice. Testing is hard. Scale
testing is hard. Distributed testing is hard. We need a set of conceptual
building blocks that can span across workloads and system types, and
machinery to put these concepts to use. Some concepts used in NoSQLBench
are shared below for illustration, but this is by no means an exhaustive
list.

### The Cycle

Cycles in NoSQLBench are whole numbers on a number line. All operations
in a NoSQLBench session are derived from a single cycle. It's a long value,
and a seed. The cycle determines not only which statements (of those available)
will get executed, but it also determines what the values bound to that
statement will be.

Cycles are specified as a closed-open `[min,max)` interval, just as slices
in some languages. That is, the min value is included in the range, but the
max value is not. This means that you can stack slices using common numeric
reference points without overlaps or gaps. It means you can have exact awareness
of what data is in your dataset, even incrementally.

You can think of a cycle as a single-valued coordinate system for data that
lives adjacent to that number on the number line.

### The Activity

An activity is a multi-threaded flywheel of statements in some sequence
and ratio. Activities run over the numbers in a cycle range. Each activity
has a driver type which determines the native protocol that it speaks.

An activity continuously

### The Activity Type

An activity type is a high level driver for a protocol. It is like a
statement-aware cartridge that knows how to take a basic statement template
and turn it into an operation for the scenario to execute.

### The Scenario

The scenario is a runtime session that holds the activities while they run.
A NoSQLBench scenario is responsible for aggregating global runtime settings,
metrics reporting channels, logfiles, and so on.

### The Scenario Script

Each scenario is governed by a script runs single-threaded, asynchronously
from activities, but in control of activities. If needed, the scenario script
is automatically created for the user, and the user never knows it is there.
If the user has advanced testing requirements, then they may take advantage
of the scripting capability at such time.
When the script exits, *AND* all activities are complete, then the scenario
is complete..
