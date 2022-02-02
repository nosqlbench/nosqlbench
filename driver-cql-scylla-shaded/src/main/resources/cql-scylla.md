# cql scylla driver

This is a CQL driver based on the ScyllaDB driver for Scylla version 3.+.

It is similar to the DataStax "cql" driver except for some features,
specific to Scylla, such as caching control, extended data types and shard-awareness.

Given the similarity to the original cql driver, the docs for both are
the same with the exception of the 'cqldriver' and 'insights' options,
which are both removed here.

Once the 4.+ driver is ready and proven out (cqld4-scylla), both the cql and the
cqlv3 will be gently deprecated, but they will remain in the
NoSQLBench project until they are no longer needed.
