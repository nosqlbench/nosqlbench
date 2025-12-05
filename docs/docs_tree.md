---
title: Documentation Tree Charter
description: "Approved roots, module coupling rules, and enforcement hooks for NoSQLBench docs."
audience: meta
diataxis: explanation
tags:
  - documentation
  - governance
component: docsys
topic: docops
status: live
owner: "@nosqlbench/docs"
generated: false
---

# Documentation Tree Charter

This charter defines **where** Markdown sources may live, **which module**
controls them, and **how** automation verifies compliance. It implements the
layout principles from `docs/docs_layout.md` and action 2 of
`docs/docs_plan.md`.

## Maintained Roots

Only the locations below are considered part of the maintained corpus. Anything
outside these roots is ignored by registry, lint, and projection tooling and
will eventually be deleted.

| Root | Description | Module Coupling | Notes |
|------|-------------|-----------------|-------|
| Repo root (specific files) | Onboarding + governance anchors such as `README.md`, `DOWNLOADS.md`, etc. | `docsys` | Only the files listed in `DocumentationTreeTest.ROOT_FILES` are permitted at repo root. |
| `docs/**` | Curated root for all hand-authored docs that do not live inside a module (governance, contributor guides, templates, registry artifacts, curated write-ups, etc.). | `docsys` | Treat this as the default landing zone for handmade content before projection/publishing. |
| `devdocs/**` | Internal/explanatory design material. | `docsys` + owning subsystem | Promotion into public docs requires updating the registry’s `visibility` fields. |
| `nb-adapters/*/src/main/resources/**/*.md` | Adapter docs, workload baselines, curated guides. | Specific adapter module | Each adapter ships one canonical README plus supplemental topics; `origin` must reference the exact path. |
| `nb-virtdata/**/docs-for-virtdata/**/*.md` | Binding/reference docs generated via annotations. | `virtdata` | Generated files require `generated: true` plus `source:` pointing at the generator. |
| `nb-engine/**/resources/**/*.md` | Engine + CLI docs embedded with runtime code. | `nb-engine` | Keep beside the feature to preserve load-time validation. |
| `nb-apis/**/docs-*/**/*.md` | API reference, Uniform Workload Specification, etc. | Owning API module | Live-doc specs must be executable during CI. |
| `nb-spectest/**` | Spec-test fixtures and doc-driven test harness inputs. | `nb-spectest` | Even though these live under test resources, they remain part of the maintained corpus. |
| `nbr/src/main/resources/**/*.md` | Exporter/runtime docs (e.g., `docs-export`). | `nbr` | Validated by module tests such as `DocSourceMetadataTest`. |
| `nb5/**/resources/**/*.md` | NB5 platform docs. | `nb5` | Same front-matter requirements as adapters. |
| `nbr-demos/**/docs/**/*.md` | Demo-specific walkthroughs. | `nbr-demos` | Document any generated assets via `source:`. |
| `sort_docs/**` | Historic holding bin for docs awaiting curation. | `docsys` + owning subsystem | Treat as staging: items live here only until the curation process (to be defined) places them in a permanent root. No additional linting applies inside this tree yet. |

## Excluded Paths

- Generated artifacts (`target/**`, caches, build outputs, `exported_docs.zip`).
- Git-ignored paths and `.github/**`.
- `local/**` is a transient scratch area and remains excluded even when it contains Markdown. Content must be curated into one of the maintained roots before it is enforced.

The `docs-inventory` app enforces these exclusions automatically.

## Module-Coupled Ownership Rules

1. **`origin` is mandatory**: Every Markdown file must declare an `origin`
   structure (module path + extraction method) in its front matter. This is the
   single source of accountability; there are no free-floating doc owners.
2. **Optional `owner` field**: When automation needs to notify a team, set
   `owner` to the relevant GitHub team for that module (e.g.,
   `@nosqlbench/adapter-maintainers`). Do not list individual developers.
3. **Module-local edits**: Additions or rewrites must occur within the module
   root that implements the functionality. If you need a shared doc, add it to
   `docs/` and reference the appropriate modules from the `origin` metadata.
4. **One canonical copy**: Generated duplicates (e.g., under `target/**`) are
   disallowed. Regenerate them during builds instead.

## Contributor Checklist

1. Choose the correct root from the table above and place the Markdown file
   there.
2. Add front matter using `docs/front_matter_template.md`, ensuring `origin`
   points at the module path, `generated` is set appropriately, and required
   Diátaxis/audience/component fields remain valid.
3. Update or regenerate `docs/docs_inventory.json` (`nb app docs-inventory
   --root .`) and commit the change. CI will fail if the snapshot drifts.
4. Run the relevant module tests (adapter SPI checks, live-doc suites, etc.) so
   `source:` metadata and executable examples stay correct.

## Enforcement Hooks

- **Registry validation**: `DocInventoryAppTest` (nb-docsys) regenerates
  `docs/docs_inventory.json` during `mvn test` and fails when the checked-in
  snapshot is stale or contains unexpected paths.
- **Module-level tests**: Individual modules (e.g., `nbr`) must continue running
  their front-matter/`source:` validation tests.
- **Upcoming checks**: The planned `doclint` bundled app will verify taxonomy,
  front matter, and link integrity for all maintained roots defined here.

Future automation or lint rules must reference this charter when rejecting new
paths or explaining why a doc belongs to a specific module.
