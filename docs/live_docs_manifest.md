---
title: Live Docs Manifest
description: "Mapping between executable documentation suites and the tests that keep them current."
audience: meta
diataxis: reference
tags:
  - documentation
  - planning
component: docsys
topic: docops
status: live
owner: "@nosqlbench/docs"
generated: false
---

# Live Docs Manifest

Live docs are Markdown specifications whose examples are executed as part of the build. This
manifest records where each suite lives, which test enforces it, and how to run it locally. Treat
the suites as **opaque collections of docs** (projection decides which files fall into each
category) but keep the intent of every suite transparent so contributors know how to extend it.

## Current Suites

| Suite | Doc Sources | Owning Module / Test | Purpose | How to Run |
|-------|-------------|-----------------------|---------|------------|
| Uniform Workload Specification | `nb-apis/adapters-api/src/main/java/workload_definition/*.md` (projected under `docs/site/reference/workload_definition/**`) | `nb-apis/adapters-api/src/test/java/io/nosqlbench/adapters/api/activityconfig/specifications/UniformWorkloadSpecificationTest.java` (uses `SpecTest`) | Validates every YAML/JSON/code fence in the workload spec so the documented forms always parse and normalize correctly. | `mvn -pl nb-apis/adapters-api test -Dtest=UniformWorkloadSpecificationTest` (also runs via `mvn test`). |

## Adding a Live Doc Suite

1. Author the documentation in the owning module (or under `docs/site/**` if it is not tied to a
   single module) with complete front matter (`origin`, `source`, taxonomy data). Do **not**
   duplicate content already owned by another module.
2. Build a `SpecTest` (or comparable harness) that loads the rendered `.md` files and exercises the
   examples. Keep the suite’s behavior opaque: the manifest should only describe the category’s
   purpose while metadata determines which docs feed it.
3. Wire the test into the module’s `mvn test` phase and ensure it fails when examples regress.
4. Add an entry to the table above noting the doc pattern, owning test, and the command developers
   can run locally.
5. Update `docs/docs_progress.md` with the new evidence so interrupted curation can resume without
   rediscovery.

Keeping the manifest current guarantees that every executable spec is inventoried and that the CI
pipeline can prove the docs still run.
