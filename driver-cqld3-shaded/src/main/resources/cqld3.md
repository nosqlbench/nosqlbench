# cqld3 driver

This is a CQL driver based on the DataStax driver for Apache
Cassandra version 3.+.

It is identical to the previous "cql" driver except for the version of
the native driver used and the exclusion of certain DSE Capabilities, such
as graph and some extended data types. This driver is meant to be used
as a bridge until we have the 4.+ driver ready for use. The 1.9 driver
which NoSQLBench included originally is no longer actively supported.

Given the similarity to the original cql driver, the docs for both are
the same with the exception of the 'cqldriver' and 'insights' options,
which are both removed here.

Once the 4.+ driver is ready and proven out (cqld4), both the cql and the
cqlv3 will be gently deprecated, but they will remain in the
NoSQLBench project until they are no longer needed.
