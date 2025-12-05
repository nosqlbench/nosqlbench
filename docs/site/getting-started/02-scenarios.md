---
title: Scenarios
description: Discover and run built-in scenarios before customizing activities.
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
weight: 102
---

👉 The commands in this section are examples to show you how the command line works with nb5.

## Run a Scenario

To run a simple built-in scenario:

```bash
./nb5 cql-keyvalue host=<addr> localdc=<localdcname>
```

To run the same scenario against Astra, add the credentials and secure connect bundle:

```bash
./nb5 cql-keyvalue scb=<scb.zip> userfile=<client-id-file> passfile=<client-secret-file> ...
```

Regardless of which form you use, keep the Astra options handy—you may want to drop them anywhere
you see `...` below.

This example shows how you can call on a completely pre-built workload template by simply using it
as the first argument. `cql-keyvalue` is a workload description hosted within the binary at
`activities/cql-keyvalue.yaml`. You must still provide endpoint and authentication details, but
those shouldn’t be hard-coded in the workload template anyway.

Behind the scenes the scenario:

1. Initializes your schema.
2. Loads background data to provide a realistic scenario.
3. Runs a main activity against the background data.

How this works is explained throughout this guide. For now, just know that all of these details are
open for you to change simply by modifying the workload template.

## Discover Scenarios

To list built-in scenarios run:

```bash
./nb5 --list-scenarios
```

To list workload templates (each may contain multiple named scenarios):

```bash
./nb5 --list-workloads
```

👉 These commands include workloads that shipped with nb5 and any workloads in your local
directory. To learn more about custom workloads, see [Workloads 101](https://docs.nosqlbench.io/workloads-101/).

You can also include the examples path prefix—which shows many more—by running:

```bash
./nb5 --list-workloads --include examples
```

When learning about bindings, first-time users should run the command above to find examples for
inspiration.

## Compose Scenarios

You can run a different named scenario or step explicitly:

```bash
# Run a specific named scenario
./nb5 cql-keyvalue astra ...

# Run a specific step of a specific named scenario
./nb5 cql-keyvalue astra.schema ...

# Run a series of specific steps
./nb5 cql-keyvalue astra.schema astra.rampup ...
```

If you don’t specify which steps to run, they all run serially in the order they are defined. If you
don’t specify which named scenario to run, `default` is used.

## Example Activities

The examples above show how to call entire scenarios (multiple steps with defaults). You can also
skip named scenarios and invoke the activities directly.

> If you only plan to use built-in scenarios you can skip the rest of this page. If you want to know
> how to drill down into the steps and test them individually, continue on.

### Create a Schema

We’ll start by creating a simple schema in the database:

```sql
CREATE KEYSPACE baselines
 WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}
 AND durable_writes = true;

CREATE TABLE baselines.keyvalue (
    key text PRIMARY KEY,
    value text
);
```

Run the following command (replacing `...` with your endpoint details). If you’re using Astra, pass
the options described in the [test target](./01-test-target.md) section:

```bash
./nb5 run driver=cqld4 workload=cql-keyvalue tags=block:schema ...
```

This follows the basic command pattern of all nb5 commands. The first bare word is a command, and
all assignments after it are parameters. `run` tells NoSQLBench to run an activity.

- `driver=cqld4` specifies the driver (DataStax Java Driver for CQL).
- `workload=cql-keyvalue` specifies the workload definition file.
- `tags=block:schema` runs the op templates tagged `block:schema` (the DDL portion of the workload).

`...` should be the endpoint and authentication details from earlier examples. Verify the result in
`cqlsh` (or DataStax Studio) with `DESCRIBE KEYSPACE baselines`.

### Load Some Data

Before running a test of typical access patterns you need background data. The `stdout` driver helps
you preview the generated statements:

```bash
./nb5 run driver=stdout workload=cql-keyvalue
```

Now write some data to the database:

```bash
./nb5 run driver=cql workload=cql-keyvalue tags=block:rampup cycles=100k ... --progress console:1s
```

- `tags=block:rampup` runs the block with INSERT statements.
- `cycles=100k` runs 100,000 operations. Choose a suitably large number for real tests.

👉 The `cycles` parameter is a range of values, not just a quantity. The next sections of the guide
explain how to shape and partition those cycles.
