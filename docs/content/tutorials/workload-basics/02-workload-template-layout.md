+++
title = "Workload Template Layout"
description = "Understanding the standard structure and workflow of workload templates"
weight = 30
template = "page.html"

[extra]
quadrant = "tutorials"
topic = "workload-basics"
category = "structure"
tags = ["workloads", "yaml", "structure", "workflow"]
+++

It is best to keep every workload template self-contained within a single YAML file, including
schema, rampup, and the main steps in your testing workflow. These steps in a typical testing
workflow are controlled by tags as described below.

👉 The step names described below have been adopted as a convention within the built-in workloads
templates. It is strongly advised that new workload templates use the same naming scheme so
that they are easier to re-use..

# Automatic tags

To make tag filters more useful, every op template in NoSQLBench is given a set of automatic
tags based on the block and op template names:

- block: &lt;blockname&gt;
- name: &lt;blockname&gt;--&lt;op name&gt;

For example, if you had a block named `block42` and an op named `op007`, then you would be able
to match it with `tags=block:block42`, or `tags=name:block42--op007`, or any regex which also
matched. The difference between the first example and the second is this: There is only one op
template which will have the name shown, but multiple statements could be in block42. [^1]

👉 In previous versions of NoSQLBench, you had to add tags directly to your docs, blocks, or op
templates. This is still supported if you need, but most cases will only require that you group
statements together in named blocks. When used with the regex matching pattern demonstrated
above, you get quite a bit of flexibility without having to create boilerplate tags everywhere.


# A Standard Workflow

These steps are *very* commonly used by nb5 users. The standard test workflow described here is
understood as [lingua franca](https://en.wikipedia.org/wiki/Lingua_franca) for seasoned NoSQLBench
users.

## Schema step

The schema step is simply where you create the necessary schema on your target system. For CQL,
this generally consists of a keyspace and one or more table statements. There is no special
schema layer in NoSQLBench. All statements executed are simply statements. This provides the
greatest flexibility in testing since every activity type is allowed to control its DDL and DML
using the same machinery.

The schema step is normally executed with defaults for most parameters. This means that
operations will execute in the order specified in the workload template, serially, exactly once.
This is a welcome side effect of how the initial parameters like _cycles_ are set from the
op templates which are activated by tagging.

The nb5 way of selecting all op templates in a block is to use the built-in block name in a tag
filter, like this:

```shell
# select all op templates in the block named schema
./nb5 ... tags=block:schema ...

# select all op templates in all blocks that have a name matching the regex
./nb5 ... tags='block:schema-.*'
```

## Rampup step

When you run a performance test, it is very important to be aware of how much data is present.
Higher density tests are more realistic for systems which accumulate data over time, or which
accumulate a larger working set every day. The amount of data on the system you are testing
should recreate a realistic amount of data that you would run in production.

It is the purpose of the _rampup_ activity is to create the backdrop data on a target system that
makes a
test meaningful for some level of data density. Data density is normally discussed as average per
node, but it is also important to consider distribution of data as it varies from the least dense to
the most dense nodes in your target system.

Because it is useful to be able to add data to a target system in an incremental way, the bindings
which are used with a _rampup_ step may actually be different from the ones used for a _main_
step. In most cases, you want the _rampup_ step to create data in a way that incrementally adds to
the working set. This allows you to add some data to a cluster with `cycles=0..1M` and then
decide whether to continue adding data using the next contiguous range of cycles, with
`cycles=1M..2M` and so on.

## Main step

The main step of a performance testing scenario is the one during which you really care about
recording the metrics. This is the actual test that everything else has prepared your system for.

You will want to run your main workload for a significant amount of time. This doesn't mean a
*long* time, but it may. What is significant in terms of getting realistic results is a question
of statistical significance. If you have a very small system which you can push into
steady-state performance in 20 minutes, then 30 minutes may be enough testing time. However,
most modern systems of scale, even with a few nodes, will take longer to get reasonably accurate
measurements. It depends on how you are measuring.

[^1]: All block names must be unique and all op names within a block must be unique.
