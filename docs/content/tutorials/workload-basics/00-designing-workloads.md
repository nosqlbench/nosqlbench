+++
title = "Designing Workloads"
description = "Introduction to workload design in NoSQLBench"
weight = 10
template = "page.html"

[extra]
quadrant = "tutorials"
topic = "workload-basics"
category = "introduction"
tags = ["workloads", "yaml", "design"]
+++

# This is a Tutorial

It is recommended that you read through the examples in each of the design sections in order. This
guide was designed to give you a detailed understanding of workload construction with NoSQLBench.
The examples will also give you better insight into how NoSQLBench works at a fundamental level.

# Workloads, Defined

Workloads in NoSQLBench are defined by a workload template. You can use workload templates to
describe operations that you want to execute, using any available operation type. A workload
template is usually provided in a YAML file according to the conventions and formats provided in
this section. From here on, we'll simply call them _workloads_.

👉 Workload templates are basically blueprints for operations that you organize in whatever
order and mix you need.

With NoSQLBench, a standard configuration format is provided that's used across all workloads.
This makes it easy to specify op templates, parameters, data bindings, and tags. By default, we
use YAML as our workload format, but you could just as easily use JSON. (YAML is a superset of
JSON). After all, workloads templates are really collections of data structure templates.

This section describes the standard workload syntax in YAML and how to use it.

# Multi-Protocol Support

You will notice that this guide is not overly CQL-specific. That is because NoSQLBench is a
multi-protocol tool. All that is needed for you to use this guide with other protocols is a
different driver parameter. Try to keep that in mind as you think about designing workloads.

# Advice for new builders

## Look for built-ins first

If you haven't yet run NoSQLBench with a built-in workload, then this section may not be necessary
reading. It is possible that a built-in workload will do what you need. If not, please read on.

## Review existing examples

The built-in workloads that are include with NoSQLBench are also easy to copy out as a starting
point. You just need to use two commands:

```shell
# find a workload you want to copy
nb5 --list-workloads

# copy a workload to your local directory
nb5 --copy cql-iot
```

## Follow the conventions

The block names and other conventions demonstrated here represent a pretty common pattern. If
you follow these patterns, your workloads will be more portable across environments. All the
baselines workloads that we publish for NoSQLBench follow these conventions.
