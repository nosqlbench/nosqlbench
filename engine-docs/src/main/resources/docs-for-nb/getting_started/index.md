---
title: Quick Start Example
weight: 20
---

# Quick Start Example

## Getting NoSQLBench

NoSQLBench is packaged directly as a Linux binary named `nb` and as an executable Java jar named `nb.jar`.

## Downloading

The Linux binary is recommended, since it comes with its own JVM and eliminates the need to manage Java downloads. Both
can be obtained at the releases section of the main NoSQLBench project:

- [NoSQLBench Releases](https://github.com/nosqlbench/nosqlbench/releases)

:::info

Once you download the binary, you may need to `chmod +x nb` to make it executable. In order to run AppImage binaries,
like nb, you need to have fuse support on your system. This is already provided on most distributions. If after
downloading and executing nb, you get an error, please consult the
[AppImage troubleshooting page](https://docs.appimage.org/user-guide/run-appimages.html#troubleshooting).

:::

This documentation assumes you are using the Linux binary initiating NoSqlBench commands with `./nb`. If you are using
the jar, just replace `./nb` with `java -jar nb.jar` when running commands. If you are using the jar version, Java 14 is
recommended, and will be required soon.

## Run a cluster

This section requires you to have a CQL system to connect to.
If you don’t already have one, you can start an instance of DSE with this one-liner:

    docker run -e DS_LICENSE=accept --name my-dse -p 9042:9042 -d datastax/dse-server:6.7.7

or consult the instructions at the
[Apache Cassandra docker hub landing page](https://hub.docker.com/_/cassandra).

## Running

To run a simple built-in workload run:

    ./nb cql-iot

To get a list of built-in scenarios run:

    # Get a list of all named scenarios and parameters
    ./nb --list-scenarios

If you want a simple list of yamls which contain named scenarios, run:

    # Get a simple list of yamls containing named scenarios
    ./nb --list-workloads

:::info

Note: These commands will include workloads that were shipped with nb and workloads in your local directory. To learn
more about how to design custom workloads see
[designing workloads](/index.html#/docs/designing_workloads.html)

:::

To provide your own contact points (comma separated), add the `hosts=` parameter

    ./nb  cql-iot hosts=host1,host2

Additionally, if you have docker installed on your local system, and your user has permissions to use it, you can use
`--docker-metrics` to stand up a live metrics dashboard at port 3000.

    ./nb  cql-iot --docker-metrics

This example doesn't go into much detail about what it is doing. It is here to show you how quickly you can start
running real workloads without having to learn much about the machinery that makes it happen.

The rest of this section has a more elaborate example that exposes some of the basic options you may want to adjust for
your first serious test.
