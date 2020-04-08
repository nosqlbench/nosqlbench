---
title: Modular Architecture
weight: 11
---

# Modular Architecture

The internal architecture of NoSQLBench is modular throughout. Everything
from the scripting extensions to data generation is enumerated at compile
time into a service descriptor, and then discovered at runtime by the SPI
mechanism in Java.

This means that extending and customizing bundles and features is quite
manageable.

It also means that it is relatively easy to provide a suitable API for
multi-protocol support. In fact, there are several drivers available in
the current NoSQLBench distribution. You can list them out with `nb
--list-drivers`, and you can get help on how to use each of them with `nb
help <driver name>`.

This also is a way for us to encourage and empower other contributors to
help develop the capabilities and reach of NoSQLBench. By encouraging
others to help us build NoSQLBench modules and extensions, we can help
more users in the NoSQL community at large.


