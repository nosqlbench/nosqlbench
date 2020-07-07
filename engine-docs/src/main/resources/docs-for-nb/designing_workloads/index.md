---
title: Designing Workloads
weight: 40
---

# Designing Workloads

Workloads in nosqlbench are always controlled by a workload definition.
Even the built-in workloads are simply pre-configured and controlled
from a single YAML file which is bundled internally.

With nosqlbench a standard YAML configuration format is provided that is
used across all activity types. This makes it easy to specify
statements, statement parameters, data bindings, and tags. This section
describes the standard YAML format and how to use it.

It is recommended that you read through the examples in each of the
design sections in order. This guide was designed to give you a detailed
understanding of workload construction with nosqlbench. The examples
will also give you better insight into how nosqlbench works at a
fundamental level.

## Multi-Protocol Support

You will notice that this guide is not overly CQL-specific. That is
because nosqlbench is a multi-protocol tool. All that is needed for you
to use this guide with other protocols is the release of more activity
types. Try to keep that in mind as you think about designing workloads.

## Advice for new builders

### Review existing examples

The built-in workloads that are include with nosqlbench are also easy to copy out as a starting point. You just need to
use two commands:

    # find a workload you want to copy
    nb --list-workloads


    # copy a workload to your local directory
    nb --copy cql-iot

### Follow the conventions

The tagging conventions described under the YAML Conventions section
will make your testing go smoother. All of the baselines that we publish
for nosqlbench will use this form.


