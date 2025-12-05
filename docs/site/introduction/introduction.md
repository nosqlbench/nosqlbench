---
title: What is NoSQLBench?
description: High-level overview of NoSQLBench capabilities.
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
weight: 1
---

Welcome to the documentation for NoSQLBench. This is a power tool that emulates real application
workloads. This means that you can fast-track performance, sizing, and data model testing without
writing your own testing harness.

To get started right away, jump to the [Getting Started](../getting-started/00-get-nosqlbench.md)
section of the docs.

## What is NoSQLBench?

NoSQLBench is a serious performance testing tool for the NoSQL ecosystem.

**NoSQLBench brings advanced testing capabilities into one tool that are not found in other testing
tools.**

- You can run common testing workloads directly from the command line within 5 minutes of reading this.
- You can generate virtual data sets of arbitrary size, with deterministic data and statistically
  shaped values.
- You can design custom workloads that emulate your application, contained in a single file, based
  on statement templates—no IDE or coding required.
- You can immediately plot your results in a Docker and Grafana stack on Linux with a single command
  line option.
- You can open the access panels when necessary and rewire the runtime behavior of NoSQLBench for advanced
  testing. This includes access to a full scripting environment with JavaScript.

The core machinery of NoSQLBench has been built with attention to detail. It has been battle tested
within DataStax as a way to help users validate their data models, baseline system performance, and
qualify system designs for scale.

In short, NoSQLBench wishes to be a programmable power tool for performance testing. However, it is
somewhat generic. It doesn't know directly about a particular type of system or protocol. It simply
provides a suitable machine harness in which to put your drivers and testing logic. If you know how
to build a client for a particular kind of system, NoSQLBench will let you load it like a plugin and control
it dynamically.

Initially, NoSQLBench was used for CQL testing, but we have seen this expanded over time by
other users and vendors with drivers for a variety of systems. We would like to see this
expanded further with contributions from others.

## Origins

The code in this project comes from multiple sources. The procedural data generation capability was
created in earlier versions of NoSQLBench by Eric Bowman and included several experimental features.
The scripting runtime and workload APIs were designed with flexibility in mind so that the tool
could adapt to many NoSQL systems over time.

An early version of NoSQLBench was developed under a different name and used internally to test
workloads for Apache Cassandra. Since then it has been rebuilt from scratch using the lessons we
learned from previous generations. With each iteration, we raise the bar on what "good" looks like
for performance testing frameworks.

We offer NoSQLBench as a new way of thinking about testing systems. It is not limited to testing
only one type of system. We want to build a community of users and practice around this project so
that everyone in the NoSQL ecosystem can benefit from common concepts, shared understanding, and
reliable patterns of use.

## Scalable User Experience

NoSQLBench endeavors to be valuable to all users. We do this by making it easy for you, our user, to
do just what you need without worrying about the rest. If you need to do something simple, it should
be simple to find the right settings and just do it. If you need something more sophisticated, then
you should be able to find what you need with a reasonable amount of effort and no surprises.

That is the core design principle behind NoSQLBench. We hope you like it.
