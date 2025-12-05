---
title: Built-In Adapters
description: Driver-Discovery
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 1
---

# Built-In Adapters

NoSQLBench supports a variety of different operations. For operations like
sending a query to a database, a native driver is typically used with help of the
DriverAdapter API. For basic operations, like writing the content of a templated
message to stdout, no native driver is needed, although the mechanism of stdout
is still implemented via the same Adapter API. In effect, if you want to
allow NoSQLBench to understand your op templates in a new way, you add an Adapter
and program it to interpret op templates in a specific way.

Each op template of an activity can be configured to use a specific adapter. The `driver=...`
parameter sets the default adapter to use for all op templates in an activity. However,
this can be overridden per op template with the `driver` field.

# Discovering Driver Adapters

NoSQLBench comes with some drivers built-in. You can discover these by running:

    nb5 --list-drivers

Each one comes with its own built-in documentation. It can be accessed with this command:

    nb5 help <driver>

This section contains the per-driver documentation that you get when you run the above command.
These driver docs are  auto-populated when NoSQLBench is built, so they are exactly the same as
you will see with the above command, only rendered in HTML.

# External Adapter jars

It is possible to load an adapter from a jar at runtime. If the environment variable `NBLIBDIR`
is set, it is taken as a library search path for jars, separated by a colon. For each element in the
lib paths that exists, it is added to the classpath. If the element is a named .jar file, it is
added. If it is a directory, then all jar files in that directory are added.
