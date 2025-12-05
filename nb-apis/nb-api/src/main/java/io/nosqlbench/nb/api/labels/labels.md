---
title: "Labeling Results"
weight: 25
description: "API doc for labels."
tags:
  - api
  - docs
audience: developer
diataxis: reference
component: core
topic: api
status: live
owner: "@nosqlbench/devrel"
generated: false
---

# TODO
* remove appname and use jobname instead as nosqlbench
* document default appname and jobname substitution into prompush URL
* always provide scenario and step


👉 NOTE: This is a very new facility within NoSQLBench which has been added to tame the parameter
space of more advanced testing methods. It will be changing as needed until it stable and reliable.

## What are Labels?

NoSQLBench allows for your metrics and accompanying annotations to be organized in a
self-consistent way with a set of labels and tags. The labeling facility is built-in to
NoSQLBench -- Labeling is not something you add to existing tests results after using it.
Labels are innate, self-consistent, and easily leveraged to apply appropriate metadata to your
results so that they are well-defined, canonically identified, and easily extended into other
systems and views.

For example, when you name your activity, you are setting a label automatically. You are setting
a label of `activity=myactivityname`. This builds on top of the _name everything_ strategy so
that everything from your session down to your op level has a name and accompanying label. This
system is implemented efficiently -- all labeling mechanisms are applied during initialization.

Labels are defensive in nature. If you try to define another value for a label which has already
been provided for a given runtime scope, an error will be thrown. This ensures that labeling
semantics are protected and that users are informed when there would be a labeling conflict, and
likely a structural or logical error in their test setup.

Labels represent a implied hierarchic structure, and this is how they are assembled. However,
when presented, they are simply a map. They are retained in-order where possible to align with
the notion of least-specific to most-specific, making them easier to interpret and work with.

## Where are label used?

### Metrics

All metrics are labeled according to the label set provided. This supports
metric systems which use the
[Open Metrics](https://github.com/OpenObservability/OpenMetrics/blob/main/specification/OpenMetrics.md)
exposition format. The recently added push reporter builds this format out of the labels provided
where metrics are instanced.

For the current (and soon to be deprecated) graphite support, the underlying labeling system
retains control of naming semantics, and graphite names are expanded from a naming template
using the extant labels. However, the uniqueness of these names still governs metrics instancing,
and this will be resolved shortly.

### Annotations

Annotations are built-in to NoSQLBench, with the most prominent form being the Grafana-specific
annotator. When using grafana with another metrics store like
[Prometheus](https://prometheus.io/)
, or
[Victoria Metrics](https://victoriametrics.com/)
, you can have NoSQLBench send metrics
to the metrics collector and concurrently send annotations directly to Grafana. This allows
populations of metrics data to have markers for each session, scenario, and activity. These
annotations embed execution details, timelines, and labeling data so that the metrics from those
lifetimes are directly addressable using the self-same label sets.

Grafana annotations have a set of tags which uniquely identify each annotation for lookup. (NOT
to be confused with op tags, which allow control of your workload variations.) The labels are
provided as values in these tags in conjugate form, such as 'scenario:myscenario234', whereas
simple annotation tags would just have a value like 'scenario'. This may cause you to pollute
your grafana annotation tag space. For now, we are allowing it to work as-is, to see if
time-based filtering and recent views are sufficient to limit the user-facing noise and
server-side response.

### Logging

Logging events sometimes use the labels to identify errors, unique measurements, or
configuration details. Even when the label isn't shown as such, there is a direct relationship
between a labeled element and it's name. For example, a named scenario "scenario234" would be
logged as such, and also annotated with an explicit label set that includes
'scenario=scenario234' via the annotator system.

### APIs

Labels are inventory addressing systems for runtime scripting. More details on this TBD.

## Label Sources

Label names and values come from multiple sources. The runtime component hierarchy of a
NoSQLBench session provides the main structure. Users may add labels on the command line at a
high level. Metrics instruments also use labels to distinguish quantiles in a histogram. These
are just two examples.

### Automatic Labels

The labels which are part of the runtime scaffolding of NoSQLBench are provided for you
automatically. These follow the hierarchic structure of the component tree. For example,
each session is labeled, and contains multiple activity containers which are each labeled,
and so-on. The arrangement of these components follows directly from a user's intended testing
approach, and thus the labels will align naturally to usage patterns.

#### Standard session labels

The following schematic shows the standard set of labels which accompany session metrics,
annotations, etc. This includes annotations which mark the lifecycle of a session, or metrics
which are related to the local environment within which as session runs, like CPU utilization.

* `jobname:nosqlbench` - This is a standard identifier for promql based metrics stores. It was
  adopted here to streamline integration with downstream systems.
* `instance:default` - A unique app instance identifier which can be used to separate
  outputs, like metrics views.
* `node:1.2.3.4` The ip address of the first publicly-routable interface.
* `session:nEXHI88` - a session code, generated from epoch millisecond time by default, but
         user configurable when needed.

When aggregating metrics over multiple client nodes, it may be useful to have a singular session
identifier. This is possible if you set a session name on the command line. Also, if you need a
GUID for your metrics and annotations by session, then it is recommended that you inject a
suitable session name. This can be any string value which is appropriate.

#### Standard Named Scenario Labels

Since users often script testing flows in the form of _named scenarios_ in their workload files,
the names used here are quite meaningful in terms of identifying metrics and other outputs.
This is a pervasive usage pattern, and should be encouraged. As such, the following labels
are added to outputs whether or not named scenarios are being used.

* [ all previous labels ]
* `workload:afilename` - The name of the workload template file, minus the extension.
* `scenario:default` - The name of scenario selected by the user
* `step:step1` - The name of the scenario step selected by the user.

In the case that named scenarios are not being used, the values for these will be set to 'NONE'.
The net effect of this is that all metrics can be viewed with these as filtering facets without
having to interpret the labeling structure.

#### Standard Container Labels

All user-configurable testing logic in NoSQLBench runs within a container which serves as a
virtual environment around commands, activity lifecycles, APIs to manage activities, component
services, error handling, input and output. Essentially a container allows for multiple virtual
test flows to be running concurrently or in sequence. Containers are run as sub-contexts of a
session, and each one is named.

Each container will have all of the standard session labels as well as a container label:

* [ all previous labels ]
* `container:test42` - The name of the container which isolates activities and state
          from other containers, including IO buffering, activity lifecycles, etc.

#### Standard Activity Labels

Every activity runs within a container as described above. The labels which are specific to an
activity will contain all of the labels for a container, and an activity label:

* [ all previous labels ]
* `activity:optimizer1` - The name of an activity, also known as the activity _alias_ or the
  name by which an activity can be managed through the activity controller API.

#### Standard Op Labels

Sometimes you have metrics or other outputs associated with a specific named operation. In such
cases, an additional label is provided to identify the operation with the activity as such:

* [ all previous labels ]
* `op:op1` - The name of the operation as defined in the workload template. This will be
  emitted with metrics associated with the op template setting `instrument: true`.

#### Standard Metric Labels

Each metric emitted by NoSQLBench will additionally have its name expressed simply as `name`,
although when downstream systems require, it will be modified to be in the transport-encoded
form like `__name__`:

* [ all previous labels ]
* `name:metricname` provided directly for each metric which is registered at runtime.

#### User-Provided Labels

Users can inject additional labels in different places:

* Session labels are added with the `--add-labels ...` option.
* Activity labels are added with the `labels` activity parameter.
* Op template labels are added with the core `labels` op field.
* Additional labels are added with scripting extensions supporting labels.

## Label Structure

The example label set above would be created at runtime as a (ordered when possible) map:

```json5
{
  "jobname": "nosqlbench",
  "instance": "default",
  "node": "1.2.3.4",
  "session": "nEXHI88",
  "workload": "afilename",
  "scenario": "default",
  "step": "step1",
  "container": "test42",
  "activity": "optimizer1",
  "op": "op1"
}
```

It would be rendered (serialized) in whatever form is idiomatic where it is used.

**examples for metric family name and label set in openmetrics**
```
op1{jobname="nosqlbench", instance="default", node="1.2.3.4", session="nEXHI88", workload="myworkload", scenario="default", step="step1", container="test42", activity="myactivity", op="op1",name="count"}
```
OR
```
{__name__="count", jobname="nosqlbench", instance="default",node="1.2.3.4",session="nEXHI88", workload="myworkload", scenario="default", step="step1", container="test42", activity="myactivity", op="op1"}
```

**example labels as tags in a grafana annotations API call for a session **
```json
{
  "dashboardUID":"...",
  "panelId":1,
  "time":12345,
  "timeEnd":678910,
  "tags":[
    "jobname:nosqlbench",
    "instance:default",
    "node:1.2.3.4",
    "session:nEXHI88"
  ],
  "text":"Annotation Description"
}
```

