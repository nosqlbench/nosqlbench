+++
title = "Set Up Test Target"
description = "Start a database instance for testing with NoSQLBench"
weight = 101
template = "docs-page.html"

[extra]
quadrant = "tutorials"
topic = "getting-started"
category = "setup"
tags = ["cassandra", "docker", "astra", "test-target", "setup"]
testable = true
+++

# Set Up a Test Target

You need a target system to run your test against. If you already have one, you can skip this section.

This guide assumes you're testing against a CQL-based system (Cassandra/DSE/Astra). For other databases, see the relevant [driver documentation](../../reference/drivers/).

## Option 1: Run DSE in Docker

Start DataStax Enterprise with a single command:

```bash
docker run -e DS_LICENSE=accept --name my-dse -p 9042:9042 -d datastax/dse-server:6.8.17-ubi7
```

This starts DSE 6.8.17 and exposes the CQL port (9042) on localhost.

**Monitoring:** For system-level metrics, you can use:
- [DSE Metrics Collector](https://docs.datastax.com/en/monitoring/doc/monitoring/metricsCollector/mcExportMetricsDocker.html)
- Prometheus integration for real-time metrics

## Option 2: Run Astra Cluster (Cloud)

DataStax Astra DB provides a serverless Cassandra cluster for functional testing.

### Setup Steps

1. **Create Astra Database**
   - Visit [Astra DB](https://astra.datastax.com/)
   - Create a new database
   - [Follow this tutorial](https://github.com/datastaxdevs/workshop-intro-to-cassandra#2-create-a-table) for detailed steps

2. **Create Keyspace**
   - Add a keyspace named `baselines` (Astra doesn't support keyspace creation via CQL)
   - Use the Astra UI to create the keyspace:

   ![Creating keyspace in Astra](/images/keyspace.png)

3. **Download Credentials**
   - **Secure Connect Bundle**: Download from the Connect tab
   - **Token**: Generate a Read/Write token in Organization Settings
   - Save Client ID and Client Secret

   ![Generating token in Astra](/images/token.png)

### Configure NoSQLBench for Astra

Use this connection pattern:

```bash
./nb5 <workload> <scenario> \
  driver=cqld4 \
  scb=<path-to-secure-connect-bundle.zip> \
  userfile=<path-to-userfile> \
  passfile=<path-to-passfile>
```

**Recommended:** Organize credentials in a directory:

```
creds/
├── scb-mydb.zip
├── userfile
└── passfile
```

Then reference them simply:

```bash
./nb5 <workload> <scenario> \
  driver=cqld4 \
  scb=creds/scb-mydb.zip \
  userfile=creds/userfile \
  passfile=creds/passfile
```

This makes managing credentials for multiple endpoints straightforward.

## Option 3: Run Apache Cassandra in Docker

Start Apache Cassandra 4.x:

```bash
docker run --name cass4 -p 9042:9042 -d cassandra:latest
```

**Verify Cassandra is ready:**

```bash
docker container logs cass4
```

Wait for the log message indicating Cassandra is accepting connections.

**More info:** [Cassandra on Docker Hub](https://hub.docker.com/_/cassandra)

## Verify Connection

Test your connection with a simple NoSQLBench command:

```bash
# For local Cassandra/DSE
./nb5 activities/baselines/cql-iot hosts=localhost

# For Astra
./nb5 activities/baselines/cql-iot \
  driver=cqld4 \
  scb=creds/scb.zip \
  userfile=creds/userfile \
  passfile=creds/passfile
```

If the workload runs without errors, your connection is working.

## Next Steps

- **[Built-in Scenarios](02-scenarios.md)** - Explore pre-built workloads
- **[Example Results](03-example-results.md)** - See what NoSQLBench outputs
- **[CQL Quickstart](../workloads/cql-quickstart.md)** - Run a complete CQL tutorial

## Related Documentation

- **[CQL Driver Reference](../../reference/drivers/cqld4.md)** - Detailed cqld4 driver configuration
- **[Driver Discovery](../../reference/drivers/)** - All available drivers
