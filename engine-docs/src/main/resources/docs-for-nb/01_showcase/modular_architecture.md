---
title: Modular Architecture
weight: 11
---

# Modular Architecture

The internal architecture of NoSQLBench is modular throughout.
Everything from the scripting extensions to the data generation functions
is enumerated at compile time into a service descriptor, and then discovered
at runtime by the SPI mechanism in Java.

This means that extending and customizing bundles and features is quite
manageable.

It also means that it is relatively easy to provide a suitable
API for multi-protocol support. In fact, there are several drivers
avaialble in the current NoSQLBench distribution. You can list them
out with `./nb --list-drivers`, and you can get help on
how to use each of them with `./nb help <name>`.

This also is a way for us to encourage and empower other contributors
to help develop the capabilities and reach of NoSQLBench as a bridge
building tool in our community. This level of modularity is somewhat
unusual, but it serves the purpose of helping users with new features.


