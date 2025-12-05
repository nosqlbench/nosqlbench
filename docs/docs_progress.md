---
title: Documentation Alignment Progress
description: "Execution log for keeping docs/docs_plan.md aligned with docs/docs_layout.md."
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

# Documentation Alignment Progress

_Last updated: 2025-11-21T16:30Z_

## Current Snapshot

- Registry and taxonomy groundwork exists, with Java-based inventory/docs-lint and attached artifacts.
- Curation rubric defined; staging bins drained, `local/**` now empty after triage.
- Modular docs have scattered `source:` enforcement in nbr; broader enforcement still pending.

## Status by Action

| # | Action | Status | Evidence | Next Step |
|---|--------|--------|----------|-----------|
| 1 | Build the documentation registry | Guarded | `DocInventoryAppTest` regenerates the inventory, validates it against `docs/docs_inventory.schema.json`, copies the blessed JSON into `nb-docsys/target/docs_inventory.json`, and attaches it as a Maven artifact (`classifier=docs-inventory`) alongside the docs-lint report. `nb app docs-lint` reads the vetted artifact by default. | Surface schema failures per-field in test output and ensure downstream consumers use the attached artifacts by default. |
| 2 | Codify maintained trees & module coupling | Guarded | `docs/docs_tree.md` documents the approved roots/exclusions, and `DocumentationTreeTest` blocks markdown outside the chartered paths and fails when `origin.module` disagrees with the owning root (excluding `local/**`). | Surface module-level summaries from the guard and reuse the logic inside doclint. |
| 3 | Define Diátaxis & taxonomy mapping | In progress | `docs/docs_taxonomy.md` documents the matrix; taxonomy enforcement is partly covered via doclint front matter checks. | Implement a lint rule that validates `audience`, `diataxis`, `component`, `topic`, and tag vocabulary. |
| 4 | Normalize front matter | In progress | `docs/front_matter_template.md` exists; legacy Python lint is deprecated and not used, while doclint enforces required keys on covered paths. | Finish porting any remaining Python logic into Java BundledApps, add doc-type templates, and retrofit high-traffic docs. |
| 5 | Standardize modular docs | In progress | `DocSourceMetadataTest` (nbr module) validates `source:` fields; no global “one canonical doc per module” audit yet. | Use the registry to list every adapter/binding/app doc, delete committed `target/**` copies, and add module-level tests for required docs/metadata. |
| 6 | Curate dev vs. user content | In progress | `docs/docs_curation.md` defines the rubric; registry captures `visibility` + `curation_state`; curated site trees live under `docs/site/**`; doclint fails on `curation_state=staging`. `local/**` is now empty (all notes triaged into `devdocs/sketches/**` or `devdocs/metrics/**`). | Refine the moved `devdocs/sketches/**` entries with updated front matter/origin (or delete obsolete ones) and keep flagging any reintroduced `sort_docs/**` files. |
| 7 | Strengthen live-doc workflows | In progress | `docs/live_docs_manifest.md` catalogs the Uniform Workload Specification suite; tests run under `mvn test`. | Add new suites as they appear and keep commands current. |
| 8 | Enforce linked & compatible docs | In progress | `nb app docs-lint` validates path/origin integrity, front matter/taxonomy, blocks links into excluded roots (`local/**`, `sort_docs/**`, `target/**`), adds anchor validation for metrics/api/architecture topics (including `#fragment` self-links), checks reference-style links have in-doc definitions, and emits JSON reports (`target/doclint-report.json`) with a `by_file` map, `link_graph` adjacency, and `orphans` list; `DocLintAppTest` surfaces per-file errors. | Use the link graph/orphans to flag under-linked docs and add CommonMark/ref-link coverage for more doc buckets as they come online. |
| 9 | Automate projection & publication | In progress | `nb5 docs-export` embeds the vetted inventory + docs-lint report (defaulting to `nb-docsys/target` artifacts) and `docs-render-zola` renders the canonical export with the abridge theme, isolated from exporter logic. | Wire render/export into the publish flow so bundles come from the canonical export. |
|10 | Codify enforced checks | In progress | `DocSourceMetadataTest` exists; no consolidated enforcement suite yet. | Create an aggregated enforcement module (or reuse `nb-docsys`) to run registry, doclint, and module tests under `mvn test`. |
|11 | Clean up legacy artifacts | Not started | Generated bundles like `exported_docs.zip` can still appear; no hygiene guard yet. | Remove committed artifacts and add guards to block `target/**` markdown and `exported_docs.zip` from Git. |

## Immediate Focus

1. Continue link-graph/CommonMark hardening (reference links, anchors) using the new link graph and orphan reporting.
2. Wire attached registry/doclint artifacts into downstream consumers and surface per-field schema failures in test output.
3. Defer `devdocs/sketches/**` cleanup until the lint/link work is stable; revisit at the end of the plan.
