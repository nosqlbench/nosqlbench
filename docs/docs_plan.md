---
title: Documentation Alignment Plan
tags:
  - documentation
  - planning
  - diataxis
---

# Documentation Alignment Plan

## Overview

This plan outlines how to move the current NoSQLBench documentation toward the
ideal state described in `docs/docs_layout.md`. It considers every markdown
source in the repo—including modular docs, dev notes, generated site content,
and live specifications—and provides staged actions to unify them.

## Current State Snapshot

- `docs/` holds only the layout manifesto; there is no canonical tree yet for the
  full maintained corpus.
- Root-level onboarding docs (e.g., `README.md`, `DOWNLOADS.md`, release notes)
  lack YAML front matter or consistent tagging.
- `devdocs/` contains ~50 internal RFCs and work-in-progress guides but no
  Diátaxis mapping.
- `local/nosqlbench-build-docs/site/content/` mirrors the published Zola site
  and already uses front matter, but it is disconnected from the source modules.
- Modular docs are scattered:
  - Adapters (`nb-adapters/**/resources/*.md`) often lack front matter and
    include TODO text such as “Remove prior to merge”.
  - Binding docs under `nb-virtdata/**/docs-for-virtdata` already include YAML.
  - Engine/app docs exist under `nb-engine`, `nbr`, `nb-apis`, etc.
- Build artifacts duplicate markdown under `target/` directories, so sources and
  generated copies coexist in Git.

## Action Plan

1. **Create a Documentation Registry**
   - Automate a repo-wide scan that records every `.md` file with metadata
     (path, owning module, intended audience, generated vs. authored).
   - Store the registry output (e.g., `docs/docs_inventory.json`) so the managed
     corpus is explicit and auditable.

2. **Define Diátaxis & Taxonomy Mapping**
   - Decide which Diátaxis quadrant each existing doc belongs to and codify the
     mapping in `docs/`.
   - Establish canonical tag sets and front-matter keys so contributors know how
     to classify new docs.

3. **Normalize Front Matter**
   - Author templates for user guides, references, tutorials, explanations,
     developer notes, adapter docs, and blog posts.
   - Retrofit high-traffic docs (README, release notes, adapter guides, etc.)
     with the templates.
   - Add a lint step in CI to fail when required front matter is missing or
     malformed.

4. **Standardize Modular Docs**
   - For each adapter/binding/app module, ensure a single authoritative Markdown
     file lives beside the code with proper metadata.
   - Remove committed build outputs from `target/` and regenerate them during
     builds instead.
   - Update the export pipeline so modular docs are ingested into the structured
     site automatically.

5. **Curate Dev vs. User Content**
   - Review `devdocs/` and `local/*.md` to identify which pieces graduate into
     public docs versus staying internal.
   - Promote finished pieces into the Diátaxis-aligned structure; leave
     experimental sketches in a clearly labeled internal area.

6. **Strengthen Live-Doc Workflows**
   - Catalog existing literate tests (e.g., Uniform Workload Specification) and
     ensure they run in CI.
   - Document the authoring pattern for new live docs so examples remain
     executable and self-verifying.

7. **Automate Projection & Publication**
   - Extend `nb5 export-docs` (or a successor) to merge the registry sources,
     Zola content, and live-doc outputs into a single structured bundle.
   - Run link-checking and CommonMark validation before publishing previews or
     releases.

8. **Clean Up Legacy Artifacts**
   - Remove duplicate/generated markdown from source control where safe (e.g.,
     `target/**` copies, committed `exported_docs.zip`).
   - Document ownership and update cadence for each doc bucket so maintenance
     responsibilities are clear.

## Next Steps

1. Assign doc owners for each major bucket (core product docs, adapters,
   bindings, dev guides) to parallelize the normalization work.
2. Prototype the documentation registry and front-matter linter to provide
   immediate feedback loops for contributors.
