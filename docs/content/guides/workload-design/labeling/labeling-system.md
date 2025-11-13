+++
title = "Understanding Labels"
description = "How the NoSQLBench labeling system organizes test results"
weight = 10
template = "page.html"

[extra]
quadrant = "guides"
topic = "workload-design"
category = "labeling"
tags = ["labels", "metadata", "metrics", "organization"]
+++

👉 This provides an intro and overview of the built-in labeling system in NoSQLBench 5.21+.

## What are Labels?

NoSQLBench allows for your metrics and accompanying annotations to be organized in a
self-consistent way with a set of labels and tags. The labeling facility is built-in to
NoSQLBench -- Labeling is not something you add to existing tests results after using it.
Labels are innate, self-consistent, and easily leveraged to apply appropriate metadata to your
results so that they are well-defined and easily repurposed in other systems.

For example, when you name your activity, you are setting a label, even if you didn't know it
already. You are setting a label of `activity=myactivityname`. This builds on top of the _name
everything_ strategy so that everything from your session down to your op level has a name and
accompanying label. This system is implemented efficiently as well, with all labeling
mechanisms being applied explicitly during initialization phases, rather than lazily during an
activity.

Labels are defensive in nature. If you try to define another value for a label which has already
been provided for a given runtime scope, an error will be thrown. This ensures that labeling
semantics are protected and that users are informed when there would be a labeling conflict. This
helps avoid structural or logical errors in their test setup.

Labels represent a implied hierarchic structure, and this is how they are assembled. The
tree-like structure of components in the runtime allows for labels to be established from
the trunk to the branches, with each label attaching to a specific component in the tree.
However, when presented for a given component, they are simply a map, including all labels which
are _inherited_ from the parent components above. They are retained in the ordered map form
where possible for the sake of easy configuration and usage.

## Label Sources

Labels are assigned from multiple sources.

### Automatic Labels

Labels which are part of the runtime scaffolding of NoSQLBench are provided for you, as
_automatic labels_. The examples below show standard labels, and an implied nesting of values.
Nesting relationships here imply runtime component relationships and lifecycles. For example,
you may expect multiple distinct session labels per node label by default, but session
uniqueness is only guaranteed within that node label scope.

Examples:

* `jobname:nosqlbench` The top level name of the metrics bucket for downstream systems. This
  label is always set, but you can override it as explained below, although generally, it should
  not be changed.
  * `instance:default` The sub-bucket used for downstream systems. This should be set when you want
    to isolate your results from others, particularly when doing specialized testing which
    should not co-mingle results with others.
    * `node:1.2.3.4` The ip address of the first publicly-routable interface. When combined with
      the session identifier, this tuple is usually sufficient as a proxy for a GUID. If your
      needs for session identification are finer-grained, then generate a GUID and provide it
      with `--add-labels` for each run.
      * `session:nEXHI88` An identifier for each session, basically each time you run
        NoSQLBench. This identifier is a compact version of the millisecond epoch timestamp.
        As a user, you can override this value if you need to change from "multiple sessions per
        node", to "multiple nodes per session" for the purposes of coherent metrics aggregation.
        * `container:smoketest` The name of the container within which any commands are run.
          Every command in nb runs within a container. For named scenarios, the container name
          is taken automatically as the step name.
        * `workload:myworkload` __IFF using named scenarios__, the name of your workload template.
        * `scenario:myscenario` __IFF using named scenarios__, the name of the scenario.
        * `step:rampup` __IFF using named scenarios__, the step name.
          * `activity:rampup` The name of your activity, derived by default from the step name
            for named scenarios, or set directly with the alias activity param.
            * [`op:anopname`] __ONLY for op-specific metrics, as opposed to activity or session
              level metrics__. The name of your operation, only provided for metrics which are
              tied to
              a specific op template, such as when using the op field `instrument:true`.
              * `name:metricname` provided directly for each metric which is registered at runtime.

Notice that some automatic labels are not always included, such as `workload`, `scenario`, and
`step`. This is because these labels are only meaningful in the case that you are using named
scenarios. It is strongly recommended that in any downstream views you use and require these labels,
and encourage your users to use named scenarios as a rule. Ad-hoc activity construction often
leads to inconsistency and ambiguity in testing flows, and named scenarios provide a good
template format to avoid this.

Metrics, annotations, and logging details can be emitted for any level of labeling, but the
labels up to and including `session` should be considered the minimum set.

### User-Provided Labels

Users can inject additional labels in different places:

* Session labels are added with the `--add-labels ...` option. Multiple of these can be provided
  in the form of `--add-labels name:value,...`.
* Activity labels are added with the `labels` activity parameter.
* Op template labels are added with the core `labels` op field.
* Additional labels are added with scripting extensions supporting labels.

## Label Structure

For an op-specific metrics, the example label set above would be created at runtime as a simple map
(shown as JSON for illustration purposes):

```json
{
  "jobname": "nosqlbench",
  "instance": "default",
  "node": "1.2.3.4",
  "session": "nEXHI88",
  "container": "smoketest",
  "workload": "myworkload",
  "scenario": "myscenario",
  "step": "rampup",
  "activity": "rampup",
  "op": "anopname",
  "name": "metricname"
}
```

It would be rendered (serialized) in whatever form is idiomatic where it is used.

**Example for metric family name and label set in OpenMetrics:**

```
metricname{jobname="nosqlbench",node="1.2.3.4",session="nEXHI88",container="smoketest",workload="myworkload",scenario="myscenario",step="rampup",activity="rampup",op="anopname"}
```

OR

```
{__name__="metricname",jobname="nosqlbench",node="1.2.3.4",session="nEXHI88",container="smoketest",workload="myworkload",scenario="myscenario",step="rampup",activity="rampup",op="anopname"}
```

**Example labels as tags in a Grafana annotations API call for an activity:**

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
    "session:nEXHI88",
    "container:smoketest",
    "workload:myworkload",
    "scenario:myscenario",
    "step:rampup",
    "activity:rampup"
  ],
  "text":"Annotation Description"
}
```

## Where are Labels Used?

### Metrics

All metrics are labeled according to the label set provided. This supports metric systems which use the [OpenMetrics](https://github.com/OpenObservability/OpenMetrics/blob/main/specification/OpenMetrics/)
exposition format. The recently added push reporter builds this format out of the labels provided
where metrics are instanced.

As of 5.21 and newer versions, graphite support is deprecated. This is a necessary change
because of the limitations in the graphite metrics naming scheme which directly conflict with
modern and more manageable practices for metrics warehousing. Dimensional metrics labels are
simply more robust, more expressive, and a more correct way of describing the source of
telemetry and logging data.

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

## Why Labeling Standards Matter

In order to make sure that your metrics and annotations have the necessary labels
so that the data is traceable, identifiable, and contextual, you may want to set
some label validation rules. These rules simply say which label names are required
and which are not allowed.

Some specific examples for why this is important:

* You want to look the results of a test which were run 6 months ago, there are 37 other sets of
  metrics data from the same time frame. Either your data is labeled so you can distinguish the
  studies, or you're just out of luck. You could have simply required a single label with a
  unique identifier in your standard to avoid this.
* You want to aggregate results across a set of nodes. Either your data is distinguished by node,
  or you have bad data. You could simply require a node label to be provided, within the context
  of your other labels. The validity of your aggregating metrics depends directly on this being
  stable and distinctly addressable.

See [Labeling Controls](labeling-controls/) for how to implement label validation and filtering.
