---
title: Documentation Taxonomy & Diátaxis Mapping
tags:
  - documentation
  - taxonomy
  - diataxis
audience: meta
diataxis: explanation
component: docsys
topic: docops
status: live
owner: "@nosqlbench/docs"
generated: false
---

# Documentation Taxonomy & Diátaxis Mapping

## Purpose

This guide defines how NoSQLBench documentation sources map into the Diátaxis
framework and establishes a shared taxonomy for front matter. It uses the
inventory snapshot in `docs/docs_inventory.json` (currently 763 markdown files)
to ground the mapping in the actual repository.

## Diátaxis Matrix

| Quadrant    | Definition                                                                 | Typical Sources                                                                                                                      | Default Tags                                  |
|-------------|------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|
| Tutorials   | Step-by-step onboarding journeys that assume no prior context.              | `local/nosqlbench-build-docs/site/content/getting-started`, `.../workloads-101`, intro blog posts, curated demos in `nbr-demos`.       | `diataxis: tutorial`, `audience: user`        |
| How-To      | Goal-oriented procedures for specific tasks.                               | Driver adapter guides (`nb-adapters/**`), `local/.../user-guide`, `devdocs/devguide/how-tos`, CLI task recipes under `nbr`.           | `diataxis: howto`, `audience: user|operator`  |
| Explanation | Deep dives, architecture notes, RFCs, conceptual docs.                     | `devdocs/**`, `local/*.md` design notes, `nb-engine` architecture docs, background blog posts.                                        | `diataxis: explanation`, `audience: developer`|
| Reference   | Authoritative specs, APIs, parameter lists, generated docs.                | `nb-virtdata/**/docs-for-virtdata`, `nb-apis` references, `nbr/src/main/resources/*.md`, auto-exported specs, CLI help text.          | `diataxis: reference`, `audience: developer`  |

When a document spans multiple categories, favor the most restrictive quadrant
needed by the primary reader. Derived or generated copies inherit the source
classification.

## Directory-to-Diátaxis Mapping

Use these defaults when classifying files discovered in the inventory:

- `local/nosqlbench-build-docs/site/content/*`
  - `introduction`, `getting-started`, `workloads-101`, `blog` → Tutorials
  - `user-guide`, `dev-guide/how-tos`, `reference/*` → How-To or Reference based on section
- `devdocs/**` → Explanation (developer audience)
- `local/*.md` outside the doc site cache → Explanation (internal audience)
- `nb-adapters/**/resources/*.md` → How-To (user/operator) plus Reference if purely API tables
- `nb-virtdata/**/docs-for-virtdata` → Reference (developer audience)
- `nb-apis/**/docs-*` and `nb-engine/**/resources/*.md` → Reference
- `nbr/src/main/resources/*.md` → Reference with `component: nbr`
- Root-level onboarding guides (`README.md`, `DOWNLOADS.md`, `RELEASE_NOTES.md`,
  `BUILDING.md`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`) → Tutorials or How-To
  depending on intent (`README`/`DOWNLOADS` = Tutorial, others = How-To).

These mappings serve as defaults for automation; maintainers can override them
via explicit front matter when a document has a different purpose.

## Front Matter Schema

Every Markdown file must include YAML front matter with at least:

```yaml
title: "Human readable title"
description: "Optional short summary"
audience: user|operator|developer|internal|meta
diataxis: tutorial|howto|explanation|reference
tags: [ ]        # lower-case hyphenated keywords
component: core|drivers|virtdata|docsys|nbr|nb5|site|community
topic: workloads|bindings|metrics|architecture|releases|contributing|ops|api
status: draft|review|live|deprecated
generated: true|false   # omit or set false for hand-written docs
```

Additional optional keys:

- `owner`: GitHub team or individual responsible for updates.
- `source`: Path to canonical source when the file is generated.
- `related`: Array of relative links to related docs.
- `labels`: Free-form key/value map for dimensions that do not fit other fields.

## Controlled Vocabulary

### Audience
- `user`: General NB users following tutorials or task recipes.
- `operator`: Production operators running workloads in clusters.
- `developer`: Contributors extending NoSQLBench itself.
- `internal`: Experimental or private design notes.
- `meta`: Documentation about docs or governance processes.

### Component
- `core`: Engine, workloads, CLI.
- `drivers`: Adapters and integrations.
- `virtdata`: Bindings and expression functions.
- `nbr`: Exporter/runtime tooling.
- `nb5`: NB5 platform specifics.
- `docsys`: Documentation build and publishing pipeline.
- `site`: Hosted doc site content.
- `community`: Contributing, governance, code of conduct.

### Topic
- `workloads`
- `drivers`
- `bindings`
- `metrics`
- `architecture`
- `releases`
- `contributing`
- `ops`
- `api`
- `docops`

### Status
- `draft`: Not yet validated.
- `review`: Under active review.
- `live`: Authoritative and validated.
- `deprecated`: Kept for history; links should point to replacements.

## Using the Inventory

The registry at `docs/docs_inventory.json` supplies the raw list of files,
owners, and inferred audiences. Use it to:

1. Identify directories lacking required front matter.
2. Prioritize high-volume owners (e.g., `local` 212 files, `nbr` 134 files) for
   bulk updates.
3. Flag generated artifacts (`target/**`, `cache/**`, `exported_docs.zip`) that
   should reference their canonical sources.

Automation (linting, projection, publishing) should combine the registry data
with the schema here to guarantee consistent metadata across every document.
