---
title: Config Methods
description: detailed explanation of all available configuration methods in nb5
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
weight: 15
---

To configure a NoSQLBench Scenario to do something useful, you have to provide parameters to it.
This can occur in one of several ways. This section is a guide on all the ways you can 
configure an nb5 scenario.

👉 Most users will not need to understand all the ways you can parameterize nb5. If you are doing 
lots of customization or scenario design, then the details of this section may be useful. 
Otherwise, the examples are a better starting point, particularly the built-in scenarios. 

# Global Options

The command line is used to configure both the overall runtime (logging, etc.) and
individual scenarios or activities. Global options can be distinguished from scenario commands and
their parameters because global options always start with a `-single` or `--double-hyphen`.

You can co-mingle global options with scenario or activity params. They will be parsed out 
without disturbing the sequence of those other options.

# Script Params

## params object

When you run a scenario script directly, like `nb5 script mysript.js` , you can provide 
params to it, just as you can with an activity. These params are provided to the scripting 
environment in a service object called `params`.

## template variables

Further, template variables are expanded in this script before evaluation just as with workload 
templates. So, you can use `TEMPLATE(varname,value)` or `<<varname:value>>` to create textual 
parameters which will be recognized on the command line.

# Scenario Commands

Any command line argument which is not a global option (starting with a `-` or `--`), is a 
scenario command. These are all described in [CLI Scripting](../../cli-scripting.md). 

Most of the time, when you are running scenario commands, they are being used to start, modify, 
or stop an activity. **Scenario commands are all about managing activities.** So, in practice, 
most scenario commands are activity commands.

## Activity Params

When you run an activity in a scenario with `run` or `start`, every named parameter after the
command is an activity param. [Core Activity Params](../../core-activity-params.md) 
allow you to initialize your workload. There are a few ways that these parameters work together.

- A default driver may be specified. This is the most common way to use nb5.
- A workload or op may be specified. Either will provide a (possibly empty) list of op templates.
- Each op template which is found will be interpreted with the selected driver, even if this is 
  a locally assigned driver in the op template itself.
- Each active driver, according to the active op templates, will enable new activity params 
  above and beyond the core activity params. For example, by assigning the driver `stdout` to an 
  activity or an op template directly, the param `filename` becomes available. This activity 
  param will apply *only* to those activity instances for which it is valid. The other drivers 
  will not see the parameter. In this way, a single set of activity params can be used to 
  configure multiple drivers.
- In special cases, when there are no op templates, and this wasn't because of tag filtering, a 
  driver may synthesize ops according to their documented behavior. The `stdout` driver does 
  this. These drivers are given a view of the raw workload template from which to build 
  fully-qualified op templates.

👉 **Depending on the driver param, additional activity params may become available.**

👉 **The driver param is actually assigned per op template.**

# Named Scenario Params

It is common to use the [Named Scenarios](../../../workloads-101/11-named-scenarios.md) 
feature to 
bundle up multiple activity workflows into a single command. When you do this, it is 
possible to apply params on the command line to the named scenario. In effect, this means you are 
applying a single set of parameters to possibly multiple activities, so there is a one-to-many 
relationship.

For this reason, the named scenarios have a form of parameter locking that allows you to drop 
your param overrides from the command line into exactly the places which are intended to be 
changeable. Double equals is a soft (quiet) lock, and triple equals is a hard (verbose) lock. 
Any parameter provided after a workflow will replace the same named parameters in the named 
scenario steps which are not locked.

# Op Template Fields

Activities, *all activities*, are based on a set of op templates. These are the
[YAML](https://yaml.org/), [json](https://www.json.org/), [jsonnet](https://jsonnet.org/), or direct 
data structures which follow the [workload definition](../../../workloads-101/02-workload-template-layout.md)
standard. These schematic values are provided to the selected driver to be mapped to native 
operations.

Generally, you define all the schematic values (templates, bindings, other config properties) 
for an op template directly within the workload template. However, there are cases where it 
makes sense to *also* allow those op fields to be initialized with the activity params.

This is easy to enable for driver developers. All that is required is that the op field has a compatible 
config model entry that matches the name and type. This also allows the driver adapter to 
describe the parameter, indicate whether the parameter is required or has defaults.

A good example of this is the `consistency_level` activity param for the cqld4 driver. If the 
user sets this value when the cqld4 driver is active, then all op templates will take their 
default from this.

👉 **Depending on the driver param, op templates will be interpreted a certain way.**

## Standard Activity Params

Some parameters that can be specified for an activity are standardized in the NoSQLBench design.
These include parameters like `driver`, `alias`, and `threads`. Find more info on standard 
params at [Standard Activity Params](../../core-activity-params.md).

## Dynamic Activity Params

Some driver params are instrumented in the runtime to be dynamically changed during a scenario's 
execution. This means that a scenario script can assign a value to an activity parameter after 
the activity is started. Further, these assignments are treated like events which force the 
activity to observe any changes and modify its behavior in real time.

This is accomplished with a two-layer _configuration_ model. The initial configuration for an 
activity is gated by a type model that knows what parameters are required, what the default 
values are, and so on. This configuration model is an aggregate of all the active drivers in 
an activity.

A second configuration model, called the _reconfiguration_ model, can expose a set of the 
original config params for modification. In this way, driver developers can allow for a variety 
of dynamic behaviors which allow for advanced analysis of workloads without restarts. These 
parameters are not meant for casual use. They are generally used in advanced scripting scenarios.

Parameters that are dynamic should be documented as such where they are described.

## Template Variables

If you need to provide general-purpose overrides to a in a workload template, then you
may use a mechanism called _template variables_. These are just like activity parameters, but they
are set via macro and can have defaults. This allows you to easily template workload properties 
in a way that is easy to override on the command line or via scripting.

