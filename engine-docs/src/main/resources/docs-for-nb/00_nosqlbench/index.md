---
title: NoSQLBench Intro
weight: 0
---

## Welcome to NoSQLBench

Welcome to the documentation for NoSQLBench. This is a power tool that emulates real application workloads.
This means that you can fast-track performance, sizing and data model testing without writing your own testing harness.

To get started right away, jump to the [Quick Start Example](/index.html#/docs/02_getting_started.html) from the menu on the left.

To see the ways you can get NoSQLBench, check out the project site [DOWNLOADS.md](https://github.com/nosqlbench/nosqlbench/blob/master/DOWNLOADS.md).

## What is NoSQLBench?

NoSQLBench is a serious performance testing tool for the NoSQL ecosystem.

**NoSQLBench brings advanced testing capabilities into one tool that are not found in other testing tools.**

- You can run common testing workloads directly from the command line. You
  can start doing this within 5 minutes of reading this.
- You can generate virtual data sets of arbitrary size, with deterministic
  data and statistically shaped values.
- You can design custom workloads that emulate your application, contained
  in a single file, based on statement templates - no IDE or coding required.
- You can immediately plot your results in a docker and grafana stack on Linux
  with a single command line option.
- When needed, you can open the access panels and rewire the runtime behavior
  of NoSQLBench to do advanced testing, including a full scripting environment
  with Javascript.

The core machinery of NoSQLBench has been built with attention to detail.
It has been battle tested within DataStax as a way to help users validate their
data models, baseline system performance, and qualify system designs for scale.

In short, NoSQLBench wishes to be a programmable power tool for performance
testing. However, it is somewhat generic. It doesn't know directly about a
particular type of system, or protocol. It simply provides a suitable machine
harness in which to put your drivers and testing logic. If you know how to build
a client for a particular kind of system, EB will let you load it like a plugin
and control it dynamically.

Initially, NoSQLBench comes with support for CQL, but we would like to see this
expanded with contributions from others.

## Origins

The code in this project comes from multiple sources. The procedural data
generation capability was known before as 'Virtual Data Set'. The core runtime
and scripting harness was from the 'EngineBlock' project. The CQL support was
previously used within DataStax. In March of 2020, DataStax and the project
maintainers for these projects decided to put everything into one OSS project
in order to make contributions and sharing easier for everyone. Thus, the new
project name and structure was launched as nosqlbench.io. NoSQLBench is an
independent project that is primarily sponsored by DataStax.

We offer NoSQLBench as a new way of thinking about testing systems. It is not
limited to testing only one type of system. It is our wish to build a community
of users and practice around this project so that everyone in the NoSQL ecosystem
can benefit from common concepts and understanding and reliable patterns of use.

## Scalable User Experience

NoSQLBench endeavors to be valuable to all users. We do this by making it easy for you, our user, to
do just what you need without worrying about the rest. If you need to do something simple, it should
be simple to find the right settings and just do it. If you need something more sophisticated, then you
should be able to find what you need with a reasonable amount of effort and no surprises.

That is the core design principle behind NoSQLBench. We hope you like it.

