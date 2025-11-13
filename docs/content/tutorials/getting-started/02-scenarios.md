+++
title = "Built-in Scenarios"
description = "Explore and run NoSQLBench's pre-built workload scenarios"
weight = 102
template = "page.html"

[extra]
quadrant = "tutorials"
topic = "getting-started"
category = "scenarios"
tags = ["scenarios", "workloads", "built-in", "cli"]
testable = true
+++

# Built-in Scenarios

Learn how to discover and run NoSQLBench's pre-built workload scenarios.

**Note:** The commands in this section are examples showing how the command line works with nb5.

## Run a Scenario

To run a simple built-in scenario:

```bash
./nb5 cql-keyvalue host=<addr> localdc=<localdcname>
```

For Astra, add credentials and secure connect bundle:

```bash
./nb5 cql-keyvalue \
  scb=<scb.zip> \
  userfile=<file-with-client-id> \
  passfile=<file-with-client-secret>
```

**Tip:** Keep these connection options handy - you'll use them in place of `...` throughout this guide.

### How It Works

This example shows how to call a pre-built workload template by name. `cql-keyvalue` is hosted within the binary at `activities/cql-keyvalue.yaml`.

The scenario automatically:
1. Initializes your schema
2. Loads background data for realistic testing
3. Runs a main activity against the data

You provide endpoint and authentication details, which should **not** be in the workload template.

## Discover Scenarios

### List All Scenarios

```bash
./nb5 --list-scenarios
```

This shows all named scenarios across all workload templates.

### List Workload Templates

```bash
./nb5 --list-workloads
```

This shows workload template files that contain scenarios.

**Note:** A single workload template can contain multiple named scenarios. The commands above scan bundled workloads and local files.

### Include Examples

```bash
./nb5 --list-workloads --include examples
```

This shows many more workloads, including educational examples. **Recommended** for first-time users learning about bindings.

**Tip:** For custom workload design, see the [Workload YAML Specification](../../reference/workload-yaml/).

## Compose Scenarios

Run specific scenarios or scenario steps:

```bash
# Run a specific named scenario
./nb5 cql-keyvalue astra ...

# Run a specific step of a scenario
./nb5 cql-keyvalue astra.schema ...

# Run multiple specific steps
./nb5 cql-keyvalue astra.schema astra.rampup ...
```

- If you don't specify steps, all steps run serially in definition order
- If you don't specify a scenario name, `default` is used

## Example Activities

You can skip named scenarios and invoke activities directly. This section shows how to drill down to individual steps.

**Skip ahead?** If you just want to use built-in scenarios, jump to [Example Results](03-example-results.md).

### Create a Schema

First, create a simple schema:

```sql
CREATE KEYSPACE baselines
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}
  AND durable_writes = true;

CREATE TABLE baselines.keyvalue (
    key text PRIMARY KEY,
    value text
)
```

Run the schema creation activity:

```bash
./nb5 run driver=cqld4 workload=cql-keyvalue tags=block:schema ...
```

**Command breakdown:**

- `run` - Run an activity
- `driver=cqld4` - Use the CQL driver (DataStax Java Driver 4.x)
- `workload=cql-keyvalue` - Use the pre-built cql-keyvalue workload
- `tags=block:schema` - Run only ops tagged with `block:schema` (DDL portion)
- `...` - Your endpoint/authentication details from earlier

**Verify:** Check your keyspace in cqlsh:
```cql
DESCRIBE KEYSPACE baselines;
```

### Load Some Data

Before running tests, load background data to make scenarios realistic.

**Preview with stdout driver:**

```bash
./nb5 run driver=stdout workload=cql-keyvalue
```

You should see 10 INSERT statements like:

```sql
insert into baselines.keyvalue (key, value) values (0,382062539);
insert into baselines.keyvalue (key, value) values (1,774912474);
insert into baselines.keyvalue (key, value) values (2,949364593);
...
```

**Note:** NoSQLBench deterministically generates data - values are the same from run to run.

**Load data into database:**

```bash
./nb5 run driver=cql workload=cql-keyvalue \
  tags=block:rampup \
  cycles=100k \
  ... \
  --progress console:1s
```

**New options:**

- `tags=block:rampup` - Run the rampup block (INSERT statements only)
- `cycles=100k` - Run 100,000 operations (100,000 writes)

**Tip:** The `cycles` parameter is a range: `cycles=n` is short for `cycles=0..n`. So `cycles=5` means cycles 0,1,2,3,4 (not including 5). Pick an appropriately large number for realistic background data.

**Progress output:**

```
cql-keyvalue (remaining,active,completed)=(99377,50,623) 001%
cql-keyvalue (remaining,active,completed)=(98219,50,1782) 002%
cql-keyvalue (remaining,active,completed)=(97009,50,2991) 003%
...
cql-keyvalue (remaining,active,completed)=(0,0,100000) 100% (last report)
```

### Run the Main Activity

Now run a mixed read/write workload (50% read, 50% write by default):

```bash
./nb5 run driver=cql workload=cql-keyvalue \
  tags='block:main.*' \
  cycles=50k \
  cyclerate=5000 \
  threads=50 \
  ... \
  --progress console:1s
```

**New options:**

- `tags=block:main.*` - Run the main block (read and write queries)
- `threads=50` - Use 50 concurrent threads (default is 1, insufficient for high throughput)
- `cyclerate=5000` - Limit to 5000 ops/sec (primary rate limiting mechanism)
- `cycles=50k` - Run 50,000 total operations

**Output:**

```
Configured scenario log at logs/scenario_20230113_135200_029.log
cql-keyvalue (remaining,active,completed)=(47168,1,2832) 006%
cql-keyvalue (remaining,active,completed)=(42059,2,7941) 016%
cql-keyvalue (remaining,active,completed)=(37107,2,12895) 026%
...
cql-keyvalue (remaining,active,completed)=(0,0,50000) 100% (last report)
```

## Now What?

NoSQLBench records metrics from your run in a log file (shown in output):
```
Configured scenario log at logs/scenario_YYYYMMDD_HHMMSS_XXX.log
```

Next, learn how to interpret these results:

- **[Example Results](03-example-results.md)** - Understand NoSQLBench output
- **[Reading Metrics](04-reading-metrics.md)** - Analyze performance metrics

## Key Concepts

- **Workload Template** - YAML file defining scenarios, bindings, and operations
- **Scenario** - Named sequence of steps (schema, rampup, main, etc.)
- **Activity** - Single execution step within a scenario
- **Tags** - Select which operations to run (e.g., `tags=block:schema`)
- **Cycles** - Range of values determining how many operations to execute
- **Threads** - Concurrency level for the workload
- **Cyclerate** - Operations per second limit

## Related Documentation

- **[Workload YAML Specification](../../reference/workload-yaml/)** - Complete YAML format reference
- **[CQL Driver](../../reference/drivers/cqld4.md)** - CQL driver details
- **[CLI Reference](../../reference/cli/)** - All command-line options
