---
title: Scripting
weight: 10
---

# Scripting with NoSQLBench

NoSQLBench is designed to be used as both a turnkey testing system as well as a toolkit for advanced
testing. The approach that enables this is based on a few key principles:

1. NoSQLBench is packaged by default for users who want to use pre-built testing configurations.
2. The whole runtime is modular and designed for composition.
3. The default testing configurations are assembled from the modules components as needed.
4. Users can choose to build their own testing configurations from these modules.
5. When a user moves from using pre-built configurations to custom configurations,
   is an incremental process.

Why base the internal logic on a scripting engine?

The principles described above apply all the way to the scripting layer. Every NoSQLBench
scenario is after-all, a script. For users who just need to run the pre-package
configurations, the fact a scripting engine is at the core is an implementation detail that
doesn't matter. For others who need to create advanced testing logic, this feature
allows them to build on the self-same concepts and components that other NoSQLBench users
are already familiar with and using. This common ground pays for itself in terms of reusability,
clarity, and a shared approach to testing at different levels of detail.

## Unique Capabilities

Unlike some other systems which define what a user is allowed to do during a scenario with something
like a DSL, NoSQLBench comes with no limitations. In other words, rather than pick a set of behaviors
from a limited list of DSL verbs, you can do anything you want during a scenario as long as it can
be expressed in Javascript.

That said, if you want to use a DSL within NoSQLBench, it doesn't prevent you from doing so. It just
doesn't come with a DSL to tell you what you can (and can't) do. Instead, it comes with a set of
scripting libraries and extensions that have proven useful for advanced testing scenarios.

NoSQLBench scripting is supported with realtime interaction between the scripting environment
and the running scenario. Activities, metrics, and control variables that are needed to dynamically
interact with a running workload are all wired in and ready to go.

Contributors can add to the scripting runtime by adding extensions to NoSQLBench. These extensions
are generally added to the integrated tests with full-roundtrip content checking to ensure that
they perform exactly as expected.

## Getting Started

For users who want to tap into the programmatic power of NoSQLBench, it's easy to get started by
using the `--show-script` option. For any normal command line that you might use with NoSQLBench,
this option causes it to dump the scenario script to stdout and exist instead of running the scenario.

You can store this into a file with a `.js` extension, and then use a command line like

    nosqlbench script myfile.js

to invoke it. This is exactly the same as running the original command line, only with a couple of
extra steps that let you see what it is doing directly in the scenario script.

