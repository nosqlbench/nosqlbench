---
title: 00 YAML Organization
weight: 00
---

# YAML Organization

It is best to keep every workload self-contained within a single YAML file, including schema, data rampup, and the main
phase of testing. The phases of testing are controlled by tags as described in the Standard YAML section.

:::info
The phase names described below have been adopted as a convention within the built-in workloads. It is strongly advised
that new workload YAMLs use the same tagging scheme so that workload are more plugable across YAMLs.
:::

## Schema phase

The schema phase is simply a phase of your test which creates the necessary schema on your target system. For CQL, this
generally consists of a keyspace and one ore more table statements. There is no special schema layer in nosqlbench. All
statements executed are simply statements. This provides the greatest flexibility in testing since every activity type
is allowed to control its DDL and DML using the same machinery.

The schema phase is normally executed with defaults for most parameters. This means that statements will execute in the
order specified in the YAML, in serialized form, exactly once. This is a welcome side-effect of how the initial
parameters like _cycles_ is set from the statements which are activated by tagging.

You can mark statements as schema phase statements by adding this set of tags to the statements, either directly, or by
block:

    tags:
      phase: schema

## Rampup phase

When you run a performance test, it is very important to be aware of how much data is present. Higher density tests are
more realistic for systems which accumulate data over time, or which have a large working set of data. The amount of
data on the system you are testing should recreate a realistic amount of data that you would run in production, ideally.
In general, there is a triangular trade-off between service time, op rate, and data density.

It is the purpose of the _rampup_ phase to create the backdrop data on a target system that makes a test meaningful for
some level of data density. Data density is normally discussed as average per node, but it is also important to consider
distribution of data as it varies from the least dense to the most dense nodes.

Because it is useful to be able to add data to a target cluster in an incremental way, the bindings which are used with
a _rampup_ phase may actually be different from the ones used for a _main_ phase. In most cases, you want the rampup
phase to create data in a way that incrementally adds to the population of data in the cluster. This allows you to add
some data to a cluster with `cycles=0..1M` and then decide whether to continue adding data using the next contiguous
range of cycles, with `cycles=1M..2M` and so on.

You can mark statements as rampup phase statements by adding this set of tags to the statements, either directly, or by
block:

    tags:
      phase: rampup

## Main phase

The main phase of a nosqlbench scenario is the one during which you really care about the metric. This is the actual
test that everything else has prepared your system for.

You can mark statement as schema phase statements by adding this set of tags to the statements, either directly, or by
block:

    tags:
      phase: main
