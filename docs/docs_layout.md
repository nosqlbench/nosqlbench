---
title: "NoSQLBench Documentation Layout"
description: "Intentional layout and principles for the NoSQLBench documentation corpus."
audience: meta
diataxis: explanation
tags:
  - documentation
  - planning
component: docsys
topic: docops
status: live
owner: "@nosqlbench/docs"
generated: false
---

# NoSQLBench Documentation Layout

This doc defines the intentional layout of the core docs of the nosqlbench project.

There are multiple sources of docs:
* The built-in markdown files which accompany various SPI and modular types of the nb (nosqlbench) project:
  * bundled apps, implemented as io.nosqlbench.nb.api.apps.BundledApp
  * driver adapters, implemented as DriverAdapter
* Programmatically documented types, using annotations, for example:
  * expression functions, implemented in ExprFunctionProvider with associated io.nosqlbench.nb.api.expr.annotations
  * binding functions, implemented as io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper, with io.nosqlbench.virtdata.api.annotations
    * including: associated auto-generated io.nosqlbench.virtdata.api.processors.DocFuncData docs classes, and runtime discovery and traversal utils
* Original hand-written documentation from the doc site, cached at local/nosqlbench-build-docs in the site/content directory.
  * This is in zola-compatible format, but that is not important for this
* Living examples of docs which are tested by unit tests like UniformWorkloadSpecificationTest

# Maintained Docs Location
The set of maintained docs shall be comprised of:
* all markdown files and structure in the docs directory
* all modular, programmatic, or other docs provided by annotations or similar

## Excluded Paths
Git ignored files and .github files are all excluded from the maintained set.
Generated artifacts (for example `target/**`, build caches, or other ignored paths) are explicitly excluded.
All canonical adapter docs live in their respective source modules (e.g., `nb-adapters/*/src/main/resources/`); any duplicates produced under `target/**` are ignored.

# Docs Principles
The principles below should apply to how the docs are maintained and used.

## Live Docs
Some docs, like the Uniform Workload Specification are a living form of docs which are also exercised by unit tests during build.
This is a key principle for ensuring that all examples are meaningful and valid.

Live docs should be maintained and should continue to work during builds.

## Modular Docs
There are also certain rules about how docs are intended to be bundled closely with the thing they document.
A good example of this is where each DriverAdapter is required to have an associated markdown file in its module or a build error is thrown.

Modular docs should be maintained and should continue to work during builds.

## Labeled Docs
Each markdown file should contain a front matter section in yaml that provides tags to identify it in a taxonomic way.

Dimensional labels and tags should be added using a consistent set of terms or categories so that all docs can be related to a set of cohesive concepts.

## Linked Docs
The docs should have valid relative links between them.

## Compatible Docs
The docs should be maintained in markdown format using commonmark and github compatible syntax.

## Structured Docs
The docs should be organized in the Diátaxis framework structure.

## Projected Docs
It should be possible to take the set of maintained docs, including those which are live example docs, annotated methods and classes, and other sources, and through a programmatic organization process, assemble them into a well-structured copy of the docs, with the metadata in the from matter being used to apply topological layout to taxonomic or other cues for consistent organization.

## Enforced Checks
Every documentation requirement described here must be enforced in code. Module-specific rules (for example, “each adapter ships a `source:`-annotated markdown file in `src/main/resources`”) should be implemented as unit or module-level tests within that module. Cross-cutting requirements (such as verifying `source` paths resolve to tracked files or that ignored/generated artifacts are excluded) should be validated via integrated tests at the repo level. This ensures the rules stay executable and fail CI when broken. Before adding a new check, review the existing unit/integration suites (e.g., SPI-based adapter validation or doc exporter tests) to avoid duplicating logic.
