---
title: Next Steps
weight: 5
---

# Next Steps

Now that you've run nosqlbench for the first time and seen what it does, you can
choose what level of customization you want for further testing.

The sections below describe key areas that users typically customize
when working with nosqlbench.

Everyone who uses nosqlbench will want to get familiar with the 'NoSQLBench Basics' section below.
This is essential reading for new and experienced testers alike.

## High-Level Users

Several canonical workloads are already baked-in to nosqlbench for immediate use.
If you are simply wanting to drive workloads from nosqlbench without building a custom workload,
then you'll want to learn about the available workloads and their options.

Recommended reading for high-level testing workflow:
1. 'Built-In Workloads'
2. 'nosqlbench Basics'

## Workload Builders

If you want to use nosqlbench to build a tailored workload that closely emulates what
 a specific application would do, then you can build a YAML file that specifies all
  of the details of an iterative workload. You can specify the access patterns,
   data distributions, and more.

The recommended reading for this is:

1. 'NoSQLBench Basics'
2. All of the 'Designing Workloads' section.
3. The online examples (find the links in the Designing Workloads section.)

## Scenario Developers

The underlying runtime for a scenario in nosqlbench is based on EngineBlock,
which means it has all the scripting power that comes with that. For advanced
scenario designs, iterative testing models, or analysis methods, you can use
ECMAScript to control the scenario from start to finish. This is an advanced
 feature that is not recommended for first-time users. A guide for scenario
 developers will be released in increments.
