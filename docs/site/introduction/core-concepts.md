---
title: Core Concepts
description: Foundational ideas that make NoSQLBench predictable.
audience: user
diataxis: howto
tags:
- site
- docs
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 2
---

NoSQLBench is built on top of core concepts that have been scrutinized, replaced, refined, and
hardened through several years of use by a diverse set of users.

This level of refinement is important when trying to express common patterns in what is often a
highly fragmented practice. Testing is hard. Scale testing is hard. Distributed testing is hard.
Combined, the challenge of executing realistic tests is daunting to all but seasoned test engineers.
Existing tools have only skirmished with this problem enough to make dents, but none have tackled
the lack of conceptual building blocks.

This has to change. We need a set of testing concepts that can span across workloads and system
types, plus machinery to put these concepts to use. Finding useful building blocks is often one of
the most difficult challenges in systems design. Once you find and validate a useful set of
concepts, everything else gets easier.

We believe that the success we've already had using NoSQLBench is strongly tied to these core
concepts. Some of them are shared below for illustration, but this is not an exhaustive list.

### The Cycle

Cycles in NoSQLBench are whole numbers on a number line. Each operation in a NoSQLBench scenario is
derived from a single cycle. It's a `long` value representing a seed. The cycle determines not only which
operation is selected for execution, but also what data will be attached and fed to it.

Cycles are specified as a _closed-open_ `[min,max)` interval, known as _slices_ in some languages. The
min value is included in the range, but the max value is not. This means you can stack slices using
common numeric reference points without overlaps or gaps, and you can have exact awareness of what
data is in your dataset, even incrementally.

You can think of a cycle as a single-valued coordinate system for data that lives adjacent to that
number on the number line. In this way, virtual dataset functions are ways of converting coordinates
into data.

In NoSQLBench, the cycle range determines both the total size of a workload and the specific set of
operations that will be performed. Using the same cycle range is the same as specifying the same
exact operations. This means your tests can be completely deterministic (pseudo-random) and
repeatable, even when they appear random or are shaped by density curves.

### The Activity

An activity is a multithreaded flywheel of statements in some sequence and ratio. Each activity runs
over the numbers in a cycle range. An activity is specified as a series of op templates in some
ratio and order. When an activity runs, it executes an efficient loop over specific operations with
its own thread pool.

### The Op Template

Each possible operation in an activity is provided by the user in a YAML- or data structure–driven
template. The op templates are used to create efficient op dispensers in the runtime according to
the mapping rules for a given driver.

### The Driver Adapter

A driver adapter is a high-level driver for a protocol which interfaces a native driver to the
runtime machinery of NoSQLBench. It's like a statement-aware cartridge that knows how to take a
basic op template and turn it into an operation for an activity to execute for a given cycle.

### The Scenario

A scenario is a runtime session that holds activities while they run. A NoSQLBench scenario
aggregates global runtime settings, metrics reporting channels, log files, and so on. All activities
run within a scenario, under the control of the scenario script.

### The Scenario Script

Each scenario is governed by a central script. This script runs single-threaded, asynchronous from
the activities, maintaining control over them. If necessary, the scenario script is automatically
created for the user, and the user never knows it is there. If the user has advanced testing
requirements, then they may take advantage of the scripting capability. The scenario completes when
the script exits *and* all activities are complete. Shortcut forms of scripting are provided on the
command line to address common variations.
