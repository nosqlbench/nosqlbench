---
title: Quick Start Example
weight: 20
---

# Quick Start Example

## Downloading

NoSQLBench is packaged directly as a Linux binary named `nb` and as
an executable Java jar named `nb.jar`.

The Linux binary is recommended, since it comes with its own
JVM and eliminates the need to manage Java downloads. Both can be obtained
at the releases section of the main NoSQLBench project:

- [NoSQLBench Releases](https://github.com/nosqlbench/nosqlbench/releases)


:::info
Once you download the binary, you may need to `chmod +x nb` to make it
executable.
:::

:::info
If you choose to use the nb.jar instead of the binary, it is recommended
to run it with at least Java 12.
:::

This documentation assumes you are using the Linux binary initiating NoSqlBench commands with `./nb`.
If you are using the jar, just replace `./nb` with `java -jar nb.jar` when running commands.

## Running

To run a simple built-in workload run:

    ./nb  cql-iot


To get a list of built-in workloads run:

    ./nb --list-workloads

:::info
Note:  this will include workloads that were shipped with nb and workloads in your local directory.
To learn more about how to design custom workloads see [designing workloads](/index.html#/docs/04_designing_workloads.html)
:::


To provide your own contact points (comma separated), add the `hosts=` parameter

    ./nb  cql-iot hosts=host1,host2


Additionally, if you have docker installed on your local system, and your user has permissions to use it, you
can use `--docker-metrics` to stand up a live metrics dashboard at port 3000.

    ./nb  cql-iot --docker-metrics


This example doesn't go into much detail about what it is doing. It is here to show you how quickly you can
start running real workloads without having to learn much about the machinery that makes it happen.

The rest of this section has a more elaborate example that exposes some of the basic options you may want to
adjust for your first serious test.
