+++
title = "CQL Quickstart"
description = "Step-by-step guide to running your first Cassandra workload with NoSQLBench"
weight = 10
template = "page.html"
date = 2023-03-28

[extra]
quadrant = "tutorials"
topic = "workloads"
category = "cassandra"
tags = ["cql", "cassandra", "quickstart", "getting-started"]
testable = true
author = "Jeff Banks (DataStax)"
+++

# CQL Quickstart

Welcome to the NoSQLBench Quick Byte, the first session in a "Getting Started" series for NoSQLBench. This session introduces the Cassandra Query Language (CQL) starter workload available in NoSQLBench v5.

## What You'll Learn

- Setting up a Cassandra test environment with Docker
- Running a pre-built NoSQLBench workload
- Understanding workload structure (scenarios, bindings, blocks)
- Customizing workloads for your needs

## Prerequisites

- Docker installed ([download here](https://www.docker.com/))
- NoSQLBench v5.17.2 or later ([latest release](https://github.com/nosqlbench/nosqlbench/releases/latest))
- Basic command-line familiarity

## Setup

### 1. Get NoSQLBench

Download the latest NB5 release:

```bash
# Download from GitHub releases
wget https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5
chmod +x nb5

# Verify installation
./nb5 --version
```

See [Installation](../getting-started/installation/) for other download options.

### 2. Run Cassandra

Start Cassandra 4.x using Docker:

```bash
docker run --name cass4 -p 9042:9042 -d cassandra
```

Verify Cassandra is running:

```bash
docker container logs cass4
```

Wait for the log message indicating Cassandra is ready to accept connections.

## Running the Workload

### 1. Verify the Workload

Check that the cql-starter workload is available:

```bash
./nb5 --list-workloads | grep cql-starter
```

Expected output:
```
/activities/baselines/cql-starter.yaml
```

### 2. Run the Default Scenario

Execute the workload with the default scenario:

```bash
./nb5 activities/baselines/cql-starter.yaml default hosts=localhost localdc=datacenter1
```

This command:
- Loads the `cql-starter.yaml` workload
- Runs the `default` scenario
- Connects to Cassandra at `localhost:9042`
- Uses `datacenter1` as the local datacenter

### 3. Examine the Results

Connect to Cassandra and view the generated data:

```bash
docker container exec -it cass4 cqlsh
```

```cql
SELECT * FROM starter.cqlstarter;
```

You should see data inserted by the workload with UUIDs, messages, and timestamps.

## Understanding the Workload

The cql-starter workload demonstrates the core structure of NoSQLBench workload templates.

### Workload Structure

```yaml
description: >2
  A cql-starter workload.
  * Cassandra: 3.x, 4.x.
  * DataStax Enterprise: 6.8.x.
  * DataStax Astra.

scenarios:
  default:
    schema: run driver=cql tags==block:schema threads==1 cycles==UNDEF
    rampup: run driver=cql tags==block:rampup cycles===TEMPLATE(rampup-cycles,1) threads=auto
    main: run driver=cql tags==block:"main.*" cycles===TEMPLATE(main-cycles,10) threads=auto

params:
  a_param: "value"

bindings:
  machine_id: ElapsedNanoTime(); ToHashedUUID() -> java.util.UUID
  message: Discard(); TextOfFile('data/cql-starter-message.txt');
  time: ElapsedNanoTime(); Mul(1000); ToJavaInstant();
  ts: ElapsedNanoTime(); Mul(1000);

blocks:
  schema:
    ops:
      create-keyspace: |
        CREATE KEYSPACE IF NOT EXISTS <<keyspace:starter>>
        WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '<<rf:1>>'}

      create-table: |
        CREATE TABLE IF NOT EXISTS <<keyspace:starter>>.<<table:cqlstarter>> (
          machine_id UUID,
          message text,
          time timestamp,
          PRIMARY KEY ((machine_id), time)
        ) WITH CLUSTERING ORDER BY (time DESC);

  rampup:
    params:
      cl: <<write_cl:LOCAL_QUORUM>>
    ops:
      insert-rampup: |
        INSERT INTO <<keyspace:starter>>.<<table:cqlstarter>> (machine_id, message, time)
        VALUES ({machine_id}, {rampup_message}, {time}) USING TIMESTAMP {ts};

  main-read:
    params:
      ratio: <<read_ratio:1>>
      cl: <<read_cl:LOCAL_QUORUM>>
    ops:
      select-read: |
        SELECT * FROM <<keyspace:starter>>.<<table:cqlstarter>>
        WHERE machine_id={machine_id};

  main-write:
    params:
      ratio: <<write_ratio:9>>
      cl: <<write_cl:LOCAL_QUORUM>>
    ops:
      insert-main: |
        INSERT INTO <<keyspace:starter>>.<<table:cqlstarter>>
        (machine_id, message, time) VALUES ({machine_id}, {message}, {time})
        USING TIMESTAMP {ts};
```

### Key Concepts

**Scenarios** define execution phases:
- `schema` - Create keyspace and tables
- `rampup` - Load initial data (not measured)
- `main` - Run measured workload

**Bindings** generate synthetic data:
- `machine_id` - UUID from elapsed nanoseconds
- `message` - Text loaded from external file
- `time` - Timestamp from elapsed time

**Blocks** contain CQL operations:
- Template variables: `<<keyspace:starter>>`
- Binding references: `{machine_id}`
- Ratios: `ratio: <<write_ratio:9>>` (90% writes, 10% reads)

**Parameters** make workloads configurable:
- `TEMPLATE(rampup-cycles,1)` - Default to 1 cycle
- `<<rf:1>>` - Replication factor default to 1

## Customization

### Copy the Workload

Create your own copy to customize:

```bash
./nb5 --copy cql-starter
```

This creates a local `cql-starter.yaml` file you can edit.

### Modify Scenarios

Edit the file to uncomment the rampdown phase:

```yaml
scenarios:
  default:
    schema: run driver=cql tags==block:schema threads==1 cycles==UNDEF
    rampup: run driver=cql tags==block:rampup cycles===TEMPLATE(rampup-cycles,1) threads=auto
    main: run driver=cql tags==block:"main.*" cycles===TEMPLATE(main-cycles,10) threads=auto
    rampdown: run driver=cql tags==block:rampdown threads==1 cycles==UNDEF  # Uncommented
```

### Run Custom Workload

```bash
./nb5 ./cql-starter.yaml default hosts=localhost localdc=datacenter1
```

### Add Verbosity

See more details with verbose flags:

```bash
./nb5 ./cql-starter.yaml default hosts=localhost localdc=datacenter1 -vv
```

Verbosity levels:
- `-v` - Info level
- `-vv` - Debug level
- `-vvv` - Trace level

### Scale Up

For real testing, increase cycles significantly:

```bash
./nb5 ./cql-starter.yaml default \
  hosts=localhost \
  localdc=datacenter1 \
  rampup-cycles=1000000 \
  main-cycles=10000000
```

## Next Steps

Now that you've run your first CQL workload:

1. **[Explore Binding Functions](../../reference/bindings/)** - Learn about data generation
2. **[Understand Workload Structure](../../reference/workload-yaml/)** - Deep dive into YAML specification
3. **[Create Custom Workloads](../../guides/workload-design/)** - Design your own test scenarios
4. **[HTTP Quickstart](http-quickstart/)** - Try HTTP/REST testing

## Want to Contribute?

NoSQLBench is open source! See our [Contributing Guide](../../development/contributing/) to get involved.

---

*This tutorial uses the cql-starter workload included with NoSQLBench. The workload is designed for local testing with minimal cycles. For production-scale testing, increase cycle counts significantly.*
