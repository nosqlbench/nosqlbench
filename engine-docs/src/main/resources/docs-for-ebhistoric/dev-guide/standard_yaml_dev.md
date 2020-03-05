---
title: YAML Config API
weight: 12
menu:
  main:
    parent: Dev Guide
    identifier: configfiles-api
    weight: 22
---

In the nosqlbench 2.* and newer versions, a standard YAML configuration format
is provided that makes it easy to use for any activity that requires statements,
tags, parameters and data bindings.  This section describes how to use it as a
developer*. Developers should already be familiar with the user guide for the
YAML config format first.

## Simple Usage

    StrInterpolater interpolator = new StrInterpolater(activityDef);
    String yaml_loc = activityDef.getParams().getOptionalString("yaml").orElse("default");
    StmtsDocList sdl = StatementsLoader.load(logger, yaml_loc, interp, "activities");

This loads the yaml at file path *yaml_loc*, while transforming template variables
with the interpolator, searching in the current directory and in the "activities"
subdirectory, and logging all diagnostics.

What you do next depends on the activity type. Typically, an activity will instantiate
an SequencePlanner to establish an operation ordering. See the *stdout* activity type
for an example of this.

## Implementation Notes

The getter methods on this API are intended to provide statements. Thus, all
access to bindings, params, or tags is provided via the StmtDef type.
It is possible to get these as aggregations at the block or doc level for activity
types that can make meaningful use of these as aggregations points. However,
it is usually sufficient to simply access the StmtDef iterator methods, as all
binding, tag, and param values are templated and overridden automatically for you.
within the API.

## On Bindings Usage

It is important to not instantiate or call bindings that are not expected to be
used by the user. This means that your statement form should use named anchors
for each and every binding that will be activated, *or* a clear contract with
the user should be expressed in the documentation for how bindings will be
resolved to statements.

## Named Anchors

The format of named anchors varies by activity type. There are some conventions
that can be used in order to maintain a more uniform user experience:

- String interpolation should use single curly braces when there are no local
  conventions.
- Named anchors in prepared statements or other DB activity types should simply
  add a name to the existing place holder, to be filtered out by the activity type
  before being passed to the lower level driver.
