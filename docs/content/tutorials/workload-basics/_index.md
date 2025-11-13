+++
title = "Workload Basics"
description = "Learn how to build workloads to simulate real-world data and access patterns"
weight = 20
template = "section.html"

[extra]
quadrant = "tutorials"
topic = "workload-basics"
+++

# Workload Basics

This tutorial series teaches you how to design and build workloads for NoSQLBench. You'll learn:

- What workloads are and how they work
- How to create operation templates
- How to use data bindings for generating test data
- How to organize and structure workload files
- How to create named scenarios

It is recommended that you read through these tutorials in order for a complete understanding of workload construction.

## Quick Start

If you haven't yet run NoSQLBench with a built-in workload, you may want to try that first:

```shell
# List available workloads
nb5 --list-workloads

# Copy a workload to your local directory as a starting point
nb5 --copy cql-iot
```

The built-in workloads follow the conventions and patterns demonstrated in these tutorials.
