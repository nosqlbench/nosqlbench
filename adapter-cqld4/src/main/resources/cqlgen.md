# cqlgen

cqlgen is a default CQL workload generator

With NB5, the cqld4 driver comes with a workload generator that can be used to generate a
workload yaml from a CQL schema file.

Development on this workload generator is just starting, but it is already in a useful state.
Eventually, workload observation and monitoring methods will be used to create workloads which
more accurately emulate those in-situ.

## Inputs & Outputs

You need the cql schema for whatever keyspaces, types, and tables you want to include.
Optionally, you can provide a table stats file which is generated as
`nodetool tablestats > tablestats`.

Note: The table stats file provides results only for a single node. As such, you will want to
adjust a config parameter called `partition_multiplier` to improve accuracy of the generated
workload. Further, the file only contains traffic and data details since the last time your node
was restarted, thus it may not be representative of the overall character of your workload and data.

## Usage

A typical cqlgen command looks like this:

```
nb5 cqlgen myschema.cql myworkload.yaml myhistograms
```

1. The first option is simply the name of the .cql file containing your schema.
2. The second is the name of the output yaml. The generator will *not* overwrite this file for you.
3. The third option is an optional table stats file created with `nodetool tablestats >tablestats`.
   If provided, the reads, writes, and estimated partition counts will be used to weight the
   workload to the tables and data sizes automatically.

For now, it is almost certain that you'll need to extract the configs and tailor them as
described below. Then, when you run `nb5 cqlgen ...` the config files in the current directory will
be used.

## Workload Patterns

The initial version of the cql workload generator provides these defaults:
* All keyspaces, tables, or types which are provided on the input are included in the workload.
* All create syntax has "if not exists" added.
* All drop syntax has "if exists" added.
* All table DDL properties are ignored except for durable writes.
* The default replication settings are as for local testing with SimpleReplicationStrategy. For
  testing on a proper cluster with NetworkTopology or in Astra, you'll need to modify this in
  the configs explained below.
* All UDTs are converted to blobs. This will be replaced by a layer which understands UDTs very
  soon.
* Data bindings are created using the simplest possible binding recipes that work.
* Cardinalities on partition-specific bindings are multiplied by 10. This presumes even data
  distribution, replication factor 3, and 30 nodes. This method will be improved in the future.
* For the main phase, reads, writes, updates, and scans are included, 1 each.
  * reads select * from a fully qualified predicate.
  * writes will write to all named fields.
  * updates change all fields with a fully qualified predicate.
  * scan-10 will read up to 10 consecutive rows from a partition with a partially qualified
    predicate. This means the last clustering column is not included in the predicates. Single
    key (1 partition component) tables do not have a scan created for them.
  * When partition estimates are provided, all read and writes statements have predicates for
    the last partition component modified to modulo by the estimated partition cardinality.

## Fine Tuning

The generator uses two internal files for the purposes of setting defaults:
- cqlgen.conf - a yaml formatted configuration file.
- cqlgen-bindings.yaml

Both of these files will be read from the internal nb5 resources unless you pull them into the
local directory with these commands:

```
nb5 --copy cqlgen/cqlgen-bindings.yaml
nb5 --copy cqlgen/cqlgen.conf
```

The details of how to customize these files are included within them. The cqlgen-bindings.yaml
file contains default bindings by type. If you get UnresolvedBindingsException when trying to
generate a workload, then a binding for the type in question must be added to the
cqlgen-bindings.yaml file.

The cqlgen.conf controls much of the internal wiring of the workload generator. Modifying it
gives you the ability to enable and disable certain stages and behaviors, like:

* obfuscating all keyspace, table, and column names
* keyspaces to include by name
* tables to exclude by traffic
* setting the replcation fields
* default timeouts
* block naming and construction (which type of operations are included in each)

These are mostly controlled by a series of processing phases known as transformers.
Some transformers depend on others upstream, but if the data provided is not sufficient, they
will silently pass-through.

This is a new feature of the NoSQLBench driver. If you are an early adopter, please reach out
with ideas, or for requests and support as needed.
