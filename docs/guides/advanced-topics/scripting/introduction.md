+++
title = "Scripting Introduction"
description = "Understanding NoSQLBench's scripting capabilities"
weight = 10
template = "docs-page.html"

[extra]
quadrant = "guides"
topic = "advanced"
category = "scripting"
tags = ["scripting", "javascript", "automation", "advanced"]
+++

## Intro

The NoSQLBench runtime is a combination of a scripting runtime and a workload execution machine.
This is not accidental. With this particular arrangement, it has been possible to build
sophisticated tests across a variety of scenarios. In particular, logic which can observe and react
to the system under test can be powerful. With this approach, it becomes possible to break away from
the conventional run-interpret-adjust cycle which is all too often done by human hands.

The approach that enables this is based on a few key principles:

1. NoSQLBench is packaged by default for users who want to use pre-built testing configurations.
2. The whole runtime is modular and designed for composition.
3. The default testing configurations are assembled from the modules components as needed.
4. Users can choose to build their own testing configurations from these modules.
5. When a user moves from using pre-built configurations to custom configurations, is an incremental
   process.

## Design Motive

Why base the internal logic on a scripting engine?

The principles described above apply all the way to the scripting layer. Every NoSQLBench scenario
is after-all, a script. For users who just need to run the pre-package configurations, it doesn't
matter that a scripting engine is at the core. For others who need to create advanced testing logic,
it is a crucial enabler. This feature allows them to build on the self-same concepts and components
that other NoSQLBench users are already familiar with and using. This provides common ground that
pays for itself in terms of usability, clarity, and a shared approach to testing at different levels
of detail.

## Machinery, Controls & Instruments

All the heavy lifting is left to Java and the core NoSQLBench runtime. This includes the iterative
workloads that are meant to test the target system. This is combined with a control layer which is
provided by GraalVM. This division of responsibility allows the high-level test logic to be "script"
and the low-level activity logic to be "machinery". While the scenario script has the most control,
it also is the least busy relative to activity workloads. The net effect is that you have the
efficiency of the iterative test loads in conjunction with the open design palette of a first-class
scripting language. You aren't having to buy test flexibility at the expense of testing speed or
efficiency. You get the best of both worlds, working together.

Essentially, the drivers are meant to handle the workload-specific machinery. They also provide
dynamic control points and parameters which special to each driver. This exposes a full feedback
loop between a running scenario script and the activities that run under its control. The scenario
is free to read the performance metrics from a live activity and make changes to it on the fly.

## Getting Started

For users who want to tap into the programmatic power of NoSQLBench, it's easy to get started by
using the `--show-script` option. For any normal command line that you might use with NoSQLBench,
this option causes it to dump the scenario script to stdout and exit instead of running the
scenario.

You can store this into a file with a `.js` extension, and then use a command line like

    nb5 script myfile.js

to invoke it. This is exactly the same as running the original command line, only with a couple of
extra steps that let you see what it is doing directly in the scenario script.
