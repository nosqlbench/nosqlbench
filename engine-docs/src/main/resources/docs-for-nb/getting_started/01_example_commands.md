---
title: 01 Commands
weight: 2
---

# Example Commands

Let's run a simple test against a cluster to establish some basic familiarity with the NoSQLBench.

## Create a Schema

We will start by creating a simple schema in the database. From your command line, go ahead and execute the following
command, replacing the `host=<host-or-ip>` with that of one of your database nodes.

```
./nb run driver=cql workload=cql-keyvalue tags=phase:schema host=<host-or-ip>
```

This command is creating the following schema in your database:

```cql
CREATE KEYSPACE baselines
 WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}
 AND durable_writes = true;

CREATE TABLE baselines.keyvalue (
    key text PRIMARY KEY,
    value text
)
```

Let's break down each of those command line options.

`run` tells nosqlbench to run an activity.

`driver=...` is used to specify the activity type (driver). In this case we are using `cql`, which tells nosqlbench to
use the DataStax Java Driver and execute CQL statements against a database.

`workload=...` is used to specify the workload definition file that defines the activity.

In this example, we use `cql-keyvalue` which is a pre-built workload that is packaged with nosqlbench.

`tags=phase:schema` tells nosqlbench to run the yaml block that has the `phase:schema` defined as one of its tags.

In this example, that is the DDL portion of the `cql-keyvalue` workload. `host=...` tells nosqlbench how to connect to
your database, only one host is necessary.

If you like, you can verify the result of this command by decribing your keyspace in cqlsh or DataStax Studio with
`DESCRIBE KEYSPACE baselines`.

## Load Some Data

Before running a test of typical access patterns where you want to capture the results, you need to make the test more
interesting than loading an empty table. For this, we use the rampup phase.

Before sending our test writes to the database, we will use the `stdout` activity type so we can see what nosqlbench is
generating for CQL statements.

Go ahead and execute the following command:

    ./nb run driver=stdout workload=cql-keyvalue tags=phase:rampup cycles=10

You should see 10 of the following statements in your console

```
insert into baselines.keyvalue (key, value) values (0,382062539);
insert into baselines.keyvalue (key, value) values (1,774912474);
insert into baselines.keyvalue (key, value) values (2,949364593);
insert into baselines.keyvalue (key, value) values (3,352527683);
insert into baselines.keyvalue (key, value) values (4,351686621);
insert into baselines.keyvalue (key, value) values (5,114304900);
insert into baselines.keyvalue (key, value) values (6,439790106);
insert into baselines.keyvalue (key, value) values (7,564330072);
insert into baselines.keyvalue (key, value) values (8,296173906);
insert into baselines.keyvalue (key, value) values (9,97405552);
```

NoSQLBench deterministically generates data, so the generated values will be the same from run to run.

Now we are ready to write some data to our database. Go ahead and execute the following from your command line:

    ./nb run driver=cql workload=cql-keyvalue tags=phase:rampup host=<host-or-ip> cycles=100k --progress console:1s

Note the differences between this and the command that we used to generate the schema.

`tags=phase:rampup` is running the yaml block in `cql-keyvalue` that has only INSERT statements.

`cycles=100k` will run a total of 100,000 operations, in this case, 100,000 writes. You will want to pick an
appropriately large number of cycles in actual testing to make your main test meaningful.

:::info
The cycles parameter is not just a quantity. It is a range of values. The `cycles=n` format is short for
`cycles=0..n`, which makes cycles a zero-based range. For example, cycles=5 means that the activity will use cycles
0,1,2,3,4, but
not 5. The reason for this is explained in detail in the Activity Parameters section.
:::

These parameters are explained in detail in the section on _Activity Parameters_.

`--progress console:1s` will print the progression of the run to the console every 1 second.

You should see output that looks like this

```
cql-keyvalue: 0.00%/Running (details: min=0 cycle=1 max=100000)
cql-keyvalue: 0.00%/Running (details: min=0 cycle=1 max=100000)
cql-keyvalue: 0.32%/Running (details: min=0 cycle=325 max=100000)
cql-keyvalue: 1.17%/Running (details: min=0 cycle=1171 max=100000)
cql-keyvalue: 2.36%/Running (details: min=0 cycle=2360 max=100000)
cql-keyvalue: 3.65%/Running (details: min=0 cycle=3648 max=100000)
cql-keyvalue: 4.61%/Running (details: min=0 cycle=4613 max=100000)
cql-keyvalue: 5.59%/Running (details: min=0 cycle=5593 max=100000)
cql-keyvalue: 7.14%/Running (details: min=0 cycle=7138 max=100000)
cql-keyvalue: 8.87%/Running (details: min=0 cycle=8868 max=100000)
...
cql-keyvalue: 100.00%/Finished (details: min=0 cycle=100000 max=100000)
```

## Run the main test phase

Now that we have a base dataset of 100k rows in the database, we will now run a mixed read / write workload, by default
this runs a 50% read / 50% write workload.

    ./nb run driver=cql workload=cql-keyvalue tags=phase:main host=<host-or-ip> cycles=100k cyclerate=5000 threads=50 --progress console:1s

You should see output that looks like this:

```
Logging to logs/scenario_20190812_154431_028.log
cql-keyvalue: 0.50%/Running (details: min=0 cycle=500 max=100000)
cql-keyvalue: 2.50%/Running (details: min=0 cycle=2500 max=100000)
cql-keyvalue: 6.70%/Running (details: min=0 cycle=6700 max=100000)
cql-keyvalue: 11.16%/Running (details: min=0 cycle=11160 max=100000)
cql-keyvalue: 14.25%/Running (details: min=0 cycle=14250 max=100000)
cql-keyvalue: 18.41%/Running (details: min=0 cycle=18440 max=100000)
cql-keyvalue: 22.76%/Running (details: min=0 cycle=22760 max=100000)
cql-keyvalue: 27.27%/Running (details: min=0 cycle=27300 max=100000)
cql-keyvalue: 31.81%/Running (details: min=0 cycle=31810 max=100000)
cql-keyvalue: 36.34%/Running (details: min=0 cycle=36340 max=100000)
cql-keyvalue: 40.90%/Running (details: min=0 cycle=40900 max=100000)
cql-keyvalue: 45.48%/Running (details: min=0 cycle=45480 max=100000)
cql-keyvalue: 50.05%/Running (details: min=0 cycle=50050 max=100000)
cql-keyvalue: 54.36%/Running (details: min=0 cycle=54360 max=100000)
cql-keyvalue: 58.91%/Running (details: min=0 cycle=58920 max=100000)
cql-keyvalue: 63.40%/Running (details: min=0 cycle=63400 max=100000)
cql-keyvalue: 66.96%/Running (details: min=0 cycle=66970 max=100000)
cql-keyvalue: 71.61%/Running (details: min=0 cycle=71610 max=100000)
cql-keyvalue: 76.11%/Running (details: min=0 cycle=76130 max=100000)
cql-keyvalue: 80.66%/Running (details: min=0 cycle=80660 max=100000)
cql-keyvalue: 85.22%/Running (details: min=0 cycle=85220 max=100000)
cql-keyvalue: 89.80%/Running (details: min=0 cycle=89800 max=100000)
cql-keyvalue: 94.46%/Running (details: min=0 cycle=94460 max=100000)
cql-keyvalue: 98.93%/Running (details: min=0 cycle=98930 max=100000)
cql-keyvalue: 100.00%/Finished (details: min=0 cycle=100000 max=100000)
```

We have a few new command line options here:

`tags=phase:main` is using a new block in our activity's yaml that contains both read and write queries.

`threads=50` is an important one. The default for nosqlbench is to run with a single thread. This is not adequate for
workloads that will be running many operations, so threads is used as a way to increase concurrency on the client side.

`cyclerate=5000` is used to control the operations per second that are initiated by nosqlbench. This command line option
is the primary means to rate limit the workload and here we are running at 5000 ops/sec.

## Now What?

Note in the above output, we see `Logging to logs/scenario_20190812_154431_028.log`.

By default nosqlbench records the metrics from the run in this file, we will go into detail about these metrics in the
next section Viewing Results.
