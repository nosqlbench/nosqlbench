---
title: What's New - NB5
description: A list of nb5 release highlights
weight: 50
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
---

# Updates in Progress

👉 **Docs track the 5.25.x mainline (Java 25 EA).** 👈

Welcome to NB5! This release represents a massive leap forward. There are so many improvements
that should have gone into smaller releases along the way, but here we are. We've had our heads
down, focusing on new APIs, porting drivers, and fixing bugs, but it's time to talk about the
new stuff!

For those who are experienced NB5 users, this will have few (but some!) surprises. For
those of you who are NB (4 or earlier) users, NB5 is a whole different kind of testing tool. The
changes allow for a much more streamlined user and developer experience, while also offering
additional capabilities never seen together in a systems testing tool.

Everything mentioned here will find its way into the main docs site before were done.

We've taken some care to make sure that there is support for earlier workloads where at all
possible. If we've missed something critical, please let us know, and we'll patch it up ASAP.

This is a narrative overview of changes for NB5 in general. Individual
[releases](https://github.com/nosqlbench/nosqlbench/releases) will have itemized code changes
listed individually.

# Artifacts

## nb5

The main bundled artifact is now named **nb5**. This version of NoSQLBench is a
significant departure from the previous limitations and conventions, so a new name was fitting.
It also allows you to easily have both on your system if you are maintaining test harnesses.
This is a combination of the NoSQLBench core runtime module `nbr` and all the bundled driver
adapters which have been contributed to the project.

## Packaging

The code base for nb5 is more modular and adaptable. The core runtime module `nbr` is now
separate, including only the core diagnostic driver which is used in integration tests. This allows
for leaner and meaner integration tests.

## drivers

We've ported many drivers to the nb5 APIs. All CQL support is now being provided by
[Datastax Java Driver for Apache Cassandra](https://docs.datastax.com/en/developer/java-driver/4.14/).
In addition, multiple contributors are stepping up to provide new drivers for many systems
across the NoSQL ecosystem.

# Project

Significant changes were made for the benefit of both users and developers.

## Team

We've expanded the developer team which maintains tools like NoSQLBench. This should allow us to
make improvements faster, focus on users more, and bring more strategic capabilities to the project
which can redefine how advanced testing is done.

## WYSiWYG Docs

We've connected the integration and specification tests to the documentation in a way that
ties examples everything together. If the examples and integration tests that are used on this
site fail, the build fails. Otherwise, the most recent examples are auto exported from the main
code base to the docs site. This means that test coverage will improve examples in the docs,
which will stay constantly up to date. Expect coverage of this method to improve with each
release. Until we can say _What You See Is What You Get_ across all nb5 functions and examples,
we're not done yet.

## Releases

Going forward we'll enforce stricter release criteria. Interim releases will be flagged as
prerelease unless due diligence checks have been done and a peer review finds a prerelease
suitable for promotion to a main release. Once flagged as a normal release, CI/CD tools can pick
up the release from the github releases area automatically.

We have a set of release criteria which will be published to this site and used as a blueprint for
releases going forward. More information on how releases are managed can be found in our
[Contributing](../dev-guide/contributing/_index.md) section. This will include testing coverage, 
static 
analysis, and further integrated testing support.

## Documentation

This doc site is a significant step up from the previous version. It is now more accessible,
more standards compliant, and generally more user-friendly. The dark theme is highly usable.
Syntax highlighting is much easier on the eyes, and page navigation works better! The starting
point for this site was provided by the [abridge](https://github.com/Jieiku/abridge) theme by
[Jieiku](https://github.com/Jieiku).

# Architecture

The impetus for a major new version of NoSQLBench came from user and developer needs. In
order to provide a consistent user experience across a variety of testing needs, the core
machinery needed an upgrade. The APIs for building drivers and features have been redesigned to
this end, resulting in a vast improvement for all who use or maintain nb5 core or drivers.

These benefits include:

* Vastly simplified driver contributor experience
* Common features across all implemented DriverAdapters
* Interoperability between drivers in the same scenario or activity
* Standard core activity params across all drivers, like `op=...`
* Standard metrics semantics across all drivers
* Standard highly configurable error handler support
* Standard op template features, like named start and stop timers
* Standard diagnostic tools across all drivers, like `dryrun=...`

The amount of _Standard_ you see in this list is directly related to the burden removed from
both nb5 users and project contributors.

### Component tree & dimensional metrics
- Runtime objects (sessions, contexts, activities, ops) live in a component tree. Each node carries immutable labels that roll up through parents, giving stable scope for metrics, annotations, and logging.
- Metrics use a dimensional model (OpenMetrics/Prometheus style) instead of graphite hierarchies. Labels from the component tree make every metric unique without name-munging. VictoriaMetrics is the preferred default collector, but any OpenMetrics-compatible backend can be used.
- Annotations and diagnostics follow the same label set, so test artifacts (metrics, logs) can be correlated by session/context/activity without bespoke wiring.

### Java runtime & concurrency
- NB5 modernized on the latest Java (now 25 EA; previously 21+) to take advantage of virtual threads and improved `java.util.concurrent` primitives. The rate limiter and async dispatch paths were reworked to avoid pinning and to scale more linearly with core counts.

Some highlights of these will be described below, with more details in the user guide.

* The [error handlers](../user-guide/error-handlers.md) mechanism is now fully generalized across all
  drivers.
  It is also chainable, with specific support for handling each error type with a specific chain of
  handlers, or
  simply assigning a default to all of them as before.
* The rate limiter is more efficient. This should allow it to work better in some scenarios
  where inter-core contention was a limiting factor.
* It is now possible to dry-run an activity with `dryrun=op` or similar. Each dryrun option goes
  a little further into a normal run so that incremental verification of workloads can be done.
  For example, the `dryrun=op` option uses all the logic of a normal execution, but it wraps
  the op implementation in a no-op. The results of this will tell you how fast the client can
  synthesize and dispatch operations when there is no op execution involved. The measurement
  will be conservative due to the extra wrapping layer.
* Thread management within activities is now more efficient, more consistent, and more real-time.
  Polling calls were replaced with evented calls where possible.
* Only op templates which are active (selected and have a positive ratio) are resolved at
  activity initialization. This improves startup times for large workload with subsets of
  operations enabled.
* Scenario orchestration uses named command contexts instead of implicit scenarios; commands within a context share state (activities, metrics) while allowing multiple contexts in one run when needed.
* Native analysis commands (like `optimo`, `findmax`, `stepup`) are Java-native instead of scripts, making them debuggable and more maintainable.
* Native drivers (like the CQL Java Driver) now have their driver instance and object graph
  cached, indexed by a named op field called `space`. By default, this is wired to return
  `default`, thus each unique adapter will use the same internal object graph for execution.
  This is how things worked for most drivers before. However, if the user specifies that the
  space should vary, then they simply assign it a binding. This allows for advanced driver
  testing across a number of client instances, either pseudo-randomly or in lock-step with
  specific access patterns. If you don't want to use this, then ignore it and everything works
  as it did before. But if you do, this is built-in to every driver by design.
* The activity parameter `driver` simply sets the default adapter for an activity. You can set
  this per op template, and run a different driver for every cycle. This field must be static on
  each op template, however. This allows for mixed-mode workloads in the same op sequence.
* Adapters can be loaded from external jars. This can help users who are building adapters and want
  to avoid building the full runtime just for iterative testing.
* The phase loop has been removed.
* Operations can now generate more operations associated with a cycle. This opens the door to
* There is a distinct API for implementing dynamic activity params distinctly from
  initialization params.

# Ergonomics

## Console

* ANSI color support in some places, such as in console logging patterns. The `--ansi` and
  `--console-pattern` and `--logging-pattern` options work together. If a non-terminal is
  detected on stdout, ANSI is automatically disabled.
* The progress meter has been modified to show real-time, accurate, detailed numbers
  including operations in flight.

## Discovery

* Discovery of bundled assets is now more consistent, supported with a family of `--list-...`
  options.

## Configuration

* Drivers know what parameters they can be configured with. This allows for more
  intelligent and useful feedback to users around appropriate parameter usage. If you get a
  param name wrong, nb5 will likely suggest the next closest option.
* S3 Urls should work in most places, including for loading workload templates. You only need to 
  configure your local authentication first.

## Templating

Much of the power of NB5 is illustrated in the new ways you can template workloads. This
includes structured data, dynamic op configuration, and driver instancing, to name a few.

* The structure of op templates (the YAML you write to simulate access patterns) has been
  standardized around a strict set of specification tests and examples. These are documented
  in-depth and tested against a specification with round-trip validation.
* Now, JSON and Jsonnet are supported directly as workload template formats. Jsonnet allows you to
  see the activity params as external variables.
* All workload template structure is now supported as maps, in addition to the other structural
  forms (previously called workload YAMLs). All of these forms automatically de-sugar into the
  canonical forms for the runtime to use. This follows the previous pattern of "If it does what
  it looks like, it is valid", but further allows simplification of workloads with inline
  naming of elements.
* In addition to workload template structure, op templates also support arbitrary structure
  instead of just scalar or String values. This is especially useful for JSON payload modeling.
  This means that op templates now have a generalized templating mechanism that works for all
  data structures. You can reference bindings as before, but you can also create collections and
  string templates by writing fields as they naturally occur, then adding `{bindings}` where you
  need.
* All op template fields can be made dynamic if an adapter supports it. It is up to the adapter
  implementor to decide which op fields must be static.
* Op template values auto-defer to configured values as static, then dynamic, and then
  configured from activity parameters as defaults. If an adapter supports a parameter at the
  activity level, and an op form supports the same field, then this occurs automatically.
* Tags for basic workload template elements are provided gratis. You no longer need to specify the
  conventional tags. All op templates now have `block: <blockname>` and `name:
  <blockname>--<name>` tags added. This works with regexes in tag filtering.
* Named scenarios now allow for `nb5 <workload-file> <scenario-name>.<scenario-step> ...`. You can
  prototype and validate complex scenarios by sub-selecting the steps to execute.
* You can use the `op="..."` activity parameter to specific a single-op workload on the
  command line, as if you had read it from a workload YAML. This allows
  for one-liner tests streamlined integration, and other simple utility usage.
* Binding recipes can now occur inline, as `{{/*Identity()*/}}`. This works with the op
  parameter above.
* You can now set a minimum version of NoSQLBench to use for a workload. The `min_version: "4.17.
  15"` property is checked starting from the most-significant number down. If there are new core
  features that your workload depends on, you can use this to avoid ambiguous errors.
* Template vars like `<<name:value>>` or `TEMPLATE(name,value)` can set defaults the first time they
  are seen. This means you don't have to update them everywhere. A nice way to handle this is to
  include them in the description once, since you should be documenting them anyway!
* You can load JSON files directly. You can also load JSONNET files directly! If you need to
  sanity check your jsonnet rendering, you can use `dryrun=jsonnet`.
* All workload template elements can have a description.

## Misc Improvements
(some carry over from pre-nb5 features)

* Argsfile support for allowing sticky parameters on a system.
* Tag filters are more expressive, with regexes and conjunctions.
* Some scenario commands now allow for regex-style globbing on activity alias names.
* Startup logging now includes details on version, hardware config, and scenario commands for 
  better diagnostics and bug reports.
* The logging subsystem config is improved and standardized across the project.
* Test output is now vectored exclusively through logging config.
* Analysis methods are improved and more widely used.

# Deprecations and Standards

* NB5 depends on Java 17. Going forward, major versions will adopt the latest LTS java release.
* Dependencies which require shading in order to play well with others are not supported. If you
  have a native driver or need to depend on a library which is not a good citizen, you can only
  use it with NB5 by using the external jar feature (explained elsewhere). This includes the
  previous CQL drivers which were the `1.9.*` and `3.*.*` version. Only CQL driver 4.* is
  provided in nb5.
* Dependencies should behave as modular jars, according to JPMS specification. This does not
  mean they need to be JPMS modules, only that the get halfway there.
* Log4J2 is the standard logging provider in the runtime for NoSQLBench. An SLF4J stub
  implementation is provided to allow clients which implement against the SLF4J API to work.
* All new drivers added to the project are based on the Adapter API.

# Works In Progress

* These docs!
* Bulk Loading efficiency for large tests
* Linearized Op Modeling
    * We now have a syntax for designating fields to extract from op results. This is part of the
      support needed to make _client-side joins_ and other patterns easy to emulate.
* Rate Limiter v3
* VictoriaMetrics Integration
    * Labeled metrics need to be fed to a victoria metrics docker via push. This approach will
      remove much of the pain involved in using prometheus as an ephemeral testing apparatus.
