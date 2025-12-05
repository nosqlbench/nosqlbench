---
title: Test Targets
description: Spin up a database (or connect to Astra) before running workloads.
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
weight: 101
---

You need a target system to run your test against. If you already have one, you can skip this
section. The tutorial assumes you are testing against a CQL-based system. If you need to start
something up, you have options:

- [Run DSE in Docker](#run-dse-in-docker)
- [Run an Astra cluster](#run-an-astra-cluster)
- [Run Apache Cassandra in Docker](#run-apache-cassandra-in-docker)

## Run DSE in Docker

You can start an instance of DSE with this one-liner:

```bash
docker run -e DS_LICENSE=accept --name my-dse -p 9042:9042 -d datastax/dse-server:6.8.17-ubi7
```

👉 If you want to see system-level metrics from your cluster, use the DSE Metrics Collector (for
DSE) or feed metrics to the Prometheus instance in your local Docker stack. See the
[DSE Metrics Collector docs](https://docs.datastax.com/en/monitoring/doc/monitoring/metricsCollector/mcExportMetricsDocker.html).

## Run an Astra Cluster

You can run a serverless cluster through DataStax Astra DB for functional testing. For help setting
up an Astra DB instance, see this
[Astra tutorial](https://github.com/datastaxdevs/workshop-intro-to-cassandra#2-create-a-table).

If you plan to follow along using Astra DB, take these steps:

- Add a keyspace named `baselines` to your Astra database (Astra does not support adding keyspaces
  through CQLSH).

  ![Keyspace](keyspace.png)

- In the connect menu of your Astra DB instance, download your secure connect bundle and note the
  path.
- In the organization settings, generate a Read/Write token and note the Client ID and Client
  Secret.

  ![Token](token.png)

### Configuring for Astra DB

The following pattern is helpful for configuring nb5 for Astra:

```
... driver=cqld4 scb=<secureconnectbundle> userfile=<userfile> passfile=<passfile> ...
```

This keeps credentials in files so they aren't exposed on the console or in logs. It is convenient
to place them in a subdirectory, e.g.:

```
... driver=cqld4 scb=creds1/scb.zip userfile=creds1/user passfile=creds1/pass ...
```

This makes it easy to manage credentials for multiple endpoints by referencing a different
directory.

👉 Keep these options handy—you may want to drop them anywhere you see `...` in later commands.

## Run Apache Cassandra in Docker

<https://hub.docker.com/_/cassandra>
