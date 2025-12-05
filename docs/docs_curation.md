---
title: Documentation Curation Rubric
description: "How to graduate staged docs into the curated corpus."
audience: meta
diataxis: howto
tags:
  - documentation
  - curation
component: docsys
topic: docops
status: draft
owner: "@nosqlbench/docs"
generated: false
---

# Documentation Curation Rubric

Curation determines whether a Markdown file remains relevant to NoSQLBench and,
if so, moves it from staging areas (for example, `sort_docs/**`) into the curated
layout (`docs/` or the owning module).

## 1. Relevance & Currency Check
- Confirm the feature/API/module still exists in the current architecture.
- Update terminology, configuration examples, and links to match today’s repo
  structure.
- Remove or archive docs that only describe deprecated experiments.

## 2. Placement & Metadata
- Choose the destination:
  - `docs/` for shared/how-to/governance content.
  - The owning module’s tree (adapter binding, API) for module-specific docs.
- Apply the front-matter template with accurate `diataxis`, `audience`,
  `component`, `topic`, `status`, and `origin` values.
- Add/refresh tags and `source:` pointers for generated content.

## 3. Registry & Automation
- Run `nb app docs-inventory --root .` and commit the updated
  `docs/docs_inventory.json`.
- Confirm the generated entry shows `visibility` and `curation_state`
  (`public/curated`, `internal`, or `staging`) that match the rubric.
- Ensure module or repo tests that enforce front matter, `source:`, and link
  integrity still pass.

Docs that fail step 1 stay in staging until rewritten or explicitly archived.

## 4. Handling `local/**` and Other Scratch Areas
- Treat `local/**` as internal scratch by default; do not publish or mirror its
  contents into `docs/site/**` without a deliberate curation decision.
- When a `local/*.md` note graduates, move it into `devdocs/` (or the owning
  module) with proper front matter and provenance (`origin.module`, `origin.path`)
  so the registry reflects its new home.
- If a `local/**` file is obsolete, delete it rather than copying it into the
  curated tree. Keep the registry clean by re-running `docs-inventory` after
  removals.
