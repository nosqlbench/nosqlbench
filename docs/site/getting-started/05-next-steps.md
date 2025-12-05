---
title: Next Steps
description: Choose how deep you want to go with NoSQLBench.
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
weight: 105
---

Now that you’ve run NoSQLBench for the first time, decide how much customization you need for future
testing. Everyone should become familiar with the
[core concepts](../introduction/core-concepts.md) before diving deeper.

## High-Level Users

Several canonical workloads are baked in for immediate use. If you just need to drive workloads
without building custom YAML, learn about the available workloads and their options.

## Workload Builders

If you want a tailored workload that closely emulates a specific application, you can build a
self-contained YAML file that specifies access patterns, data distributions, and more. See
[Workloads 101](https://docs.nosqlbench.io/workloads-101/) for guidance.

### Built-In Sources

Use `--list-workloads` to see bundled workloads, then `--copy <name>` to export one to your local
directory. These sources provide a wealth of examples as you customize or extend workloads.

## Scenario Developers

For advanced designs, iterative testing models, or analysis methods, you can use ECMAScript to
control the scenario from start to finish. This is an advanced feature; if you need help, reach out
on Discord.
