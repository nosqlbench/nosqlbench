# Workload Definition

This directory contains the design for a standard and extensible way of
loading workload definitions into a NoSQLBench activity. It covers both
the user-facing schematic of what can be specified and the driver-facing
API which allows for a variety of driver implementations to be supported.

## Background Work

If you want to understand the rest of this document, it is crucial that
you have a working knowledge of the standard YAML format and several
examples from the current drivers. You can learn this from the main
documentation which demonstrates step-by-step how to build a workload.
Reading further in this document will be most useful for core NB
developers, but we are happy to take input from anybody.

## Overview of Workload Mapping

The purpose of this effort is to thoroughly standardize the concepts and
machinery of how operations are mapped from user configuration to
operations in flight. In the past, much of the glue logic between YAML and
an operation has been left to the NoSQLBench ActivityType -- the
high-level driver which acts as a binding layer between vendor APIs and
the NoSQLBench core machinery.

Now that there are several drivers implemented, each with their own minor
variations in how YAML could be interpreted, it's time to take stock of
the common path and codify it. The expected outcome of this effort are
several:

- NoSQLBench drivers (AKA ActivityTypes) get much easier to implement.
- Standard NB driver features are either supported uniformly, or not at
  all.
- The semantics of op templates and workload configuration are much more
  clearly specified and demonstrated for users.
- All API surface area for this facet of NB can be tested in a very
  tangible way, with docs and testing sharing one and the same examples.

While these desires are enough alone to warrant an improvement, they are
also key to simplifying the design of two new drivers which are in the
works: gRPC and the Apache Cassandra Java driver version 4. The gRPC
driver will need to have a very clearly specified logical boundary on the
NB side to keep the combined system simple enough to explain and maintain.

## Op Mapping Stages

As a workload definition is read and mapped into the form of an executable
activity in the NB process, it takes on different forms. Each stage can be
thought of as a more refined view or API through which the workload can be
seen. At each stage, specific processing is required to promote the more
generic form into a more specialized and consumable form by the next
layer.

It should be noted that mapping workload definitions to operations is not
something that needs to be done quickly. Instead, it is more important to
focus on user experience factors, such as flexibility, obviousness,
robustness, correctness, and so on. Thus, priority of design factors in
this part of NB is placed more on clear and purposeful abstractions and
less on optimizing for speed. The clarity and detail which is conveyed by
this layer to the driver developer will then enable them to focus on
building fast and correct op dispensers, which are built before the main
part of running a workload, but which are used at high speed while the
workload is running.

## Stored Form

Presently this is YAML, but it could be any format.

Each stored form requires a loader which can map its supported formats
into a raw data structure explained below.

A Workload Loader is nothing more than a reader which can read a specific
format into a data structure.

## Workload Template

**Workload templates are presented to NoSQLBench standardized data
structures.**

This is a data structure in basic object form. It is merely the most
obvious and direct in-memory representation of the contents of the stored
form. In Java, this looks like basic collections and primitive types, such
as Lists, Maps, Strings, and so on. The raw data structure form should
always be the most commodity type of representation for the target
language.

The workload template is meant to be a runtime model which can be
specified and presented to the scenario in multiple ways. As such,
scripting layers and similar integrations can build such data structures
programmatically, and provide them to the runtime directly. So long as the
programmer is aware of what is valid, providing a workload template as a
data structure should have the same effect as providing one from a yaml or
json file.

In this way, the NB workload data structure acts as a de-facto API of
sorts, although it has no methods or functions. It is simply a commodity
representation of a workload template. As such, the NoSQLBench runtime
must provide clear feedback to the user when invalid constructions are
given.

What is valid, what is not, and what each possible construction must be
codified in a clear and complete standard. The valid elements of a
workload template are documented in
[workload_templates.md](workload_templates.md), which serves as both an
explainer and a living specification. The contents of this file are tested
directly by NoSQLBench builds.

## Workload API

The workload template provides some layering possibilities which are
applied automatically for the user by the workload API. Specifically, any
bindings, params, or tags which are defined by name in an outer scope of
the structure are automatically used by operations which do not define
their own element of the same type and name. This happens at three levels:
document scope, block scope, and op scope. More details on this are in the
*designing workload* guide.

Since the workload template is meant to enable layered defaults, there is
a logical difference between the minimally-specified version of a workload
and that seen by a driver. Drivers access the workload through the lens of
the workload API, which is responsible for layering in the settings
applied to each op template.

This form of the workload is called the **rendered workload**, and is
presented to the driver as an accessible object model.

## Specifications

- [Templated Workloads](templated_workloads.md) - defines how users can
  specify workloads
- [Templated Operations](templated_operations.md) - defines how users can
  specify various types of operation templates
- [Workload API](workload_api.md) - provides a normalized
  view of operations to driver implementations
- [Rendered Command API](rendered_command.md) - provides op template views
  to driver implementations in a highly flexible way

