---
title: Core Concepts
weight: 2
---

# Refined Core Concepts

The core concepts that NoSQLBench is built on have been scrutinized,
replaced, refined, and hardened through several years of use by users of
various needs and backgrounds.

This level of refinement is important when trying to find a way to express
common patterns in what is often a highly fragmented practice. Testing is
hard. Scale testing is hard. Distributed testing is hard. Combined, the
challenge of executing realistic tests is often quite daunting to all but
seasons test engineers. To make this worse, existing tools have only
skirmished with this problem enough to make dents, but none has tackled
full-on the lack of conceptual building blocks.

This has to change. We need a set of conceptual building blocks that can
span across workloads and system types, and machinery to put these
concepts to use. This is why it is important to focus on finding a useful
and robust set of concepts to use as the foundation for the rest of the
toolkit to be built on. Finding these building blocks is often one of the
most difficult tasks in systems design. Once you find and validate a
useful set of concepts, everything else gets easier

We feel that the success that we've already had using NoSQLBench has been
strongly tied to the core concepts. Some concepts used in NoSQLBench are
shared below for illustration, but this is by no means an exhaustive list.

### The Cycle

Cycles in NoSQLBench are whole numbers on a number line. Each operation in
a NoSQLBench scenario is derived from a single cycle. It's a long value,
and a seed. The cycle determines not only which statements is selected for
execution, but also what synthetic payload data will be attached to it.

Cycles are specified as a closed-open `[min,max)` interval, just as slices
in some languages. That is, the min value is included in the range, but
the max value is not. This means that you can stack slices using common
numeric reference points without overlaps or gaps. It means you can have
exact awareness of what data is in your dataset, even incrementally.

You can think of a cycle as a single-valued coordinate system for data
that lives adjacent to that number on the number line. In this way,
virtual dataset functions are ways of converting coordinates into data.

### The Activity

An activity is a multi-threaded flywheel of statements in some sequence
and ratio. Activities run over the numbers in a cycle range. Each activity
has a driver type which determines the native protocol that it speaks.

### The Driver Type

A driver type is a high level driver for a protocol. It is like a
statement-aware cartridge that knows how to take a basic statement
template and turn it into an operation for an activity to execute within
the scenario.

### The Scenario

The scenario is a runtime session that holds the activities while they
run. A NoSQLBench scenario is responsible for aggregating global runtime
settings, metrics reporting channels, log files, and so on. All activities
run within a scenario, under the control of the scenario script.

### The Scenario Script

Each scenario is governed by a script runs single-threaded, asynchronously
from activities, but in control of activities. If needed, the scenario
script is automatically created for the user, and the user never knows it
is there. If the user has advanced testing requirements, then they may
take advantage of the scripting capability at such time. When the script
exits, *AND* all activities are complete, then the scenario is complete.
