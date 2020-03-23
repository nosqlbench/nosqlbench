---
title: Portable Workloads
weight: 2
---

# Portable Workloads

All of the workloads that you can build with NoSQLBench are self-contained
in a workload file. This is a statement-oriented configuration file that
contains templates for the operations you want to run in a workload.

This defines part of an activity - the iterative flywheel part that is
run directly within an activity type.  This file contains everything needed
to run a basic activity -- A set of statements in some ratio. It can be
used to start an activity, or as part of several activities within a scenario.

## Standard YAML Format

The format for describing statements in NoSQLBench is generic, but in a
particular way that is specialized around describing statements for a workload.

That means that you can use the same YAML format to describe a workload
for kafka as you can for Apache Cassandra or DSE.

The YAML structure has been tailored to describing statements, their
data generation bindings, how they are grouped and selected, and the
parameters needed by drivers, like whether they should be prepared
statements or not.

Further, the YAML format allows for defaults and overrides with a
very simple mechanism that reduces editing fatigue for frequent users.

You can also template document-wide macro paramers which are taken
from the command line parameters just like any other parameter. This is
a way of templating a workload and make it multi-purpose or adjustable
on the fly.

## Experimentation Friendly

Because the workload YAML format is generic across activity types,
it is possible to ask one acivity type to interpret the statements that are
meant for another. This isn't generally a good idea, but it becomes
extremely handy when you want to have a very high level activity type like
`stdout` use a lower-level syntax like that of the `cql` activity type.
When you do this, the stdout activity type _plays_ the statements to your
console as they would be executed in CQL, data bindings and all.

This means you can empirically and substantively demonstrate and verify
access patterns, data skew, and other dataset details before you
change back to cql mode and turn up the settings for a higher scale test.

