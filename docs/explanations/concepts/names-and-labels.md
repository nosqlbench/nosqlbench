+++
title = "Names and Labels"
description = "Understanding NoSQLBench's naming and labeling architecture"
weight = 30
template = "docs-page.html"

[extra]
quadrant = "explanations"
topic = "concepts"
category = "architecture"
tags = ["labels", "naming", "architecture", "concepts"]
+++

Labels are used to identify everything you can configure as a user, and as a result the specific
details of where your results come from. Labels are expressed by NoSQLBench as details in
metrics, annotations, log lines, error messages, and so on.

## Everything is Named

Users interact directly with key components of NoSQLBench, such as scenarios, workload templates,
op templates, and metrics. Whether configuring a component or analyzing results of a test, it is
essential that all components are clearly identified in context. This means that users can
configure key elements of a test _by name_, just as they can look up results in metrics views
_by name_.

In cases where users do not provide a name for a component, element, or operation, a name is
created for them based on the surrounding structure, so at least error messages, log lines, and
other forms of output are specific and relatable to the configuration and workload. Users should
not be wondering "Which op template does this error pertain to?", nor should they be wondering
"How do I label the results of this test so that they don't get mixed up with other results?"
The naming and labeling systems are there to provide clear and useful identifiers so this
doesn't happen.

## Everything is Labeled

The runtime context of a single operation in NoSQLBench has layers. An operation is executed for
a given cycle, which is run within an activity, which is an independent process within a
scenario, which executes with global options. This means that it is not sufficient to only know
the name of an operation to isolate it from all others of the same name. You must also know
which activity and which scenario it runs within. Imagine you are looking for the results of a
specific test run within a dashboard, and there are multiple concurrent results available. The
context of the operation is required information to be able to look at and use specific results.

Thus, the naming scheme in NoSQLBench is also extended to be used as a labeling system. For any
key element that a user can interact with, it will know its label set. Each unique label set
uniquely identifies a single and distinct component within the runtime.

Naming for all key elements is provided as a set of labels. Strictly speaking, the label
set is unordered, however it is maintained in construction order by default to make
reading and reasoning about the layers and nesting easier.

## Everything is Hierarchic

There are various levels of labeling which are combined for each level of nesting in
the runtime. At the outermost layer, there are fewer labels, with more specific labels added as
you go deeper.

## Runtime Layers

A comprehensive view of runtime layers is given below. Those which are called out like
__[this]__ are provided as standard labels for any metrics, logging, or error conditions as
appropriate. They others may be added to specific context views when needed.

* __[session]__ - Each runtime session has a unique name by default. A session is a single
  invocation of NoSQLBench, including global options, scenario or workload selection, etc.
  * __[scenario]__ - The scenario is the top-level process within the NoSQLBench runtime. It is
    represented by a scenario script which is either synthesized for the user or provided
    directly by the user. It's value is the selected scenario from a named scenario, or just the
    session name if invoked as an ad-hoc scenario.
    * __[activity]__ - Within a scenario, zero or more activities will run. An activity is a
      separate iterative process that runs within a scenario. Activities operate on a set of
      inputs called cycles, by default an interval of long values.
      * _thread_ - An activity has its own thread pool, and each thread operates on cycles from
        the activity's cycle source known as an _input_.
        * _cycle range_ - Each thread in an activity iterates over a set of cycles as governed by
          the _stride_, which simply aligns the micro-batching size around a logically defined
          sequence.
          * _cycle_ - Each activity thread iterates over the cycles in it's current cycle
            range.
            * __[op name]__ - For a given cycle, a specific deterministic operation is synthesized
              and executed within the owning thread, and doing any additional logic such as
              error handling is applied as specified by the user.
              * _space_ - Each op can use a named context which remains stateful for the
                duration of the activity. This is handled by default for most testing scenarios,
                but can be customized for some powerful testing capabilities when needed.

## Auxiliary Labels

While some of the elements above are labeled as a standard within the runtime, there results of
testing may need to be queried, indexed, or aggregated by additional labels which explain the
purpose or parameters of a specific test run. These are provided by users in accordance with
how that particular layer is normally configured.

### User-Provided Labels

Users can add additional labels to be added to the label set for every single element in a
session by providing them on the command line.

### Automatic Labels

Some labels are added for you automatically to describe the usage context of a session, etc.

__appname__

A default label of `appname` is always provided with a value of `nosqlbench`. This by itself
uniquely identifies metrics which were produced by nosqlbench, which is useful in shared metrics
settings.

__workload__

The name of the workload template which was used in the scenario, if any.

__usermode__

This is set to a value like `named_scenario` or `script` or `adhoc` depending on how the
scenario was invoked.

__step__

The name of the related step from a named scenario if that is how the scenario was invoked.

__alias__

This is deprecated. Use __activity__ in combination with other labels instead.

## Labels vs Tags

There are two distinct facilities within NoSQLBench for identifying and managing op templates
and their related downstream effects.

__Labels__

Labels are meant to be nominal for __what something is__, and are applied using a well-defined
naming schema as a way to express in detail which consequences are related to which
configurations, in detail, and over time.

__Tags__

Tags are meant to be useful filters for specifying __which things should be used in a test__,
and are applied ad-hoc within workload templates for the purposes of customizing scenarios
around specific operations to be included.

Each layer is configured and informed by a set of inputs. Each layer adds a specific set of
labels, one or more.

## Key APIs

The way that labels are supported in NB code is through a labeling API.

### NBLabeledElement

NBLabeledElement is a property decorator. It signifies any element which has a set of labels
known as NBLabels, accessed with the `getLabels()` method. In nearly every case, the labels for
an element are computed on the fly by combining the parent labels with some named property of
the current element. This is why many runtime types require an NBLabeledElement in their
constructor as a requirement.

### NBLabels

NBLabels provides convenient construction patterns to allow for composing more detailed label sets.

For practical usage of the labeling system, see the [labeling guides](../../guides/workload-design/labeling/_index.md).
