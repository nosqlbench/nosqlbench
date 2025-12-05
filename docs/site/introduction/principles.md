---
title: Design Principles
description: Guiding values behind NoSQLBench.
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
weight: 15
---

There are a few core design principles that steer the technical design of NoSQLBench. They are
shared here to help explain what NoSQLBench is all about.

## Respect for Users

While this sounds like a conduct-level aspect, the focus here is on what it means for design.
Respect is absolutely part of the code of conduct governing this project as well, but it also means
we try to build systems that respect users in general and in specific ways that can only be tackled
through thoughtful design.

## Durable Concepts

We focus on core concepts that stand the test of time and give time back to users. We look for
concepts that return more clarity and reuse than they take away through indirection. We build
patterns of use around these concepts to bring users together in common practice and move the
testing ecosystem forward.

## Composable Systems

We build composable systems so they can quickly be used in a pre-built form at a high level, and we
make them reconfigurable so advanced users can repurpose them for contextual needs. This provides a
sliding scale of user experience where time invested translates into incremental value.

## High Fidelity

We build high-fidelity measurement tools and instruments into NoSQLBench so that results are useful,
repeatable, and reproducible. We build efficiency into the NoSQLBench machinery so that testing
tools maintain enough headroom to make accurate measurements at speed.
