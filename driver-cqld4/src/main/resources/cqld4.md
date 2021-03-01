# cqld4 driver

This is the newly revamped (alpha) driver for cql which uses
the OSS Driver version 4. As there was a significant restructuring
of the APIs between CQL driver 4 and previous versions, this driver
is not a derivative of the previous NoSQLBench CQL driver which
was based on the version 1.9 native driver. Instead, it is a
clean and separate implementation which aims to use the features
of version 4* of the native driver directly.

This means that many features that advanced testers may have been
used to (the syntactical sugar, the surfacing of advanced configuration
properties in simple ways, and so on) will have to be redesigned to
fit with version 4 of the driver. Most users who do basic testing with
direct CQL syntax should see few issues, but advanced testers will need
to consult this documentation specifically to understand the differences
between `cqld4` NB features and `cql` NB features.


