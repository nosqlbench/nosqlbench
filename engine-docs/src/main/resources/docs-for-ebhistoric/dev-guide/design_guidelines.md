---
title: Design Guidelines
weight: 34
menu:
  main:
    parent: Dev Guide
    identifier: Design Guidelines
    weight: 12
---

These guidelines are partially aspirational. As the project evolves, attempts will be made to
codify these guidelines and measure them on a per-release basis.

## ActivityType Naming

Each activity type should be named with a single lowercase name that is accurate and stable. Any activity type
implementations submitted to the nosqlbench project may be changed by the project maintainers to ensure this.

## ActivityType Documentation

Each activity type should have a file which provides markdown-formatted documentation for the user. This documentation
should be in a markdown format that is clean for terminal rendering for when users have *only* a terminal to read
with.

The single file should be hosted in the classpath under the name of the activity type with a `.md` extension. For example,
the `tcpclient` activity type has documentation in `tcpclient.md` at the root of the classpath.

This allows for users to run `help tcpclient` to get that documentation.

### ActivityType Parameters

The documentation for an activity type should have an explanation of all the activity parameters that are unique to it.
Examples of each of these should be given. The default values for these parameters should be given. Further, if
there are some common settings that may be useful to users, these should be included in the examples.

### Statement Parameters

The documentation for an activity type should have an explanation of all the statement parameters that are unique to it.
Examples of each of these should be given. The default values for these parameters should be given. 
 
## Parameter Use

Activity parameters *and* statement parameters must combine in intuitive ways.

### Additive Configuration

If there is a configuration element in the activity type which can be modified in multiple ways that are not mutually exclusive, each time that
configuration element is modified, it should be done additively. This means that users should not be surprised when
they use multiple parameters that modify the configuration element with only the last one being applied. 

### Parameter Conflicts

If it is possible for parameters to conflict with each other in a way that would provide an invalid configuration when both are applied,
or in a way that the underlying API would not strictly allow, then these conditions must be detected by the activity type, with
an error thrown to the user explaining the conflict.

### Parameter Diagnostics

Each and every activity parameter that is set on an activity *must* be logged at DEBUG level with the 
pattern `ACTIVITY PARAMETER: <activity alias>` included in the log line, so that the user may verify applied parameter settings.
Further, an explanation for what this parameter does to the specific activity *should* be included in a following log line.

Each and every statement parameter that is set on a statement *must* be logged at DEBUG level with the
pattern `STATEMENT PARAMETER: <statement name>: ` included in the log line, so that the user may verify applied statement settings.
Further, an explanation for what this parameter does to the specific statement *should* be included in a following log line.

  

