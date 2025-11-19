---
title: Documentation Alignment Plan
tags:
  - documentation
  - planning
  - diataxis
audience: meta
diataxis: explanation
component: docsys
topic: docops
status: live
owner: "@nosqlbench/docs"
generated: false
---

# Documentation Alignment Plan

## Overview

This plan outlines how to move the current NoSQLBench documentation toward the
ideal state described in `docs/docs_layout.md`. It considers every markdown
source in the repo—including modular docs, dev notes, generated site content,
and live specifications—and provides staged actions to unify them.

## Plan Intent & Acceptance

- This is an execution charter, not a developer task backlog. It defines the
  standards, sequencing, and deliverables that implementation streams must
  satisfy; individual engineering plans should reference and comply with it.
- Acceptance criteria for every action are owned by this plan and must be fully
  automatable. Each workstream is complete only when there is a repeatable,
  machine-enforced check (CI job, bundled app, or module test) that proves the
  requirement is met—mirroring how prior compliance has been validated.
- Ownership follows module boundaries: adapters, bindings, apps, and shared
  docs remain bundled with the functionality they describe, while this plan’s
  shared automation (lint apps, registry jobs, CI gates) provides the
  cross-cutting enforcement and export pipeline.
  Rather than redistribute responsibility via registry owners, capture an
  `origin` property for every doc that records the module/path (and extraction
  method when relevant) that produced it—especially when content is repackaged
  in exports. Use `owner` metadata only when an alert-routing hook is required.
- The historical `nb-docsys` module can host new docs tooling when needed, but
  nothing in its legacy code should be treated as an implicit requirement unless
  it is already consumed by other modules; this plan defines the authoritative
  expectations going forward.
- Every action below names both the tangible deliverable and the automated test
  loop that confirms compliance, making the deliver/test pairing explicit.

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

## Alignment Targets from `docs/docs_layout.md`

- **Maintained corpus vs. exclusions**: All authored Markdown under `docs/**`
  plus modular/programmatic sources (adapter docs, annotated APIs, live specs)
  must be tracked, while generated artifacts (`target/**`, caches, `.github/**`)
  stay excluded.
- **Live docs**: Executable specs such as the Uniform Workload Specification
  must continue to run under CI so examples never drift.
- **Modular docs**: Each SPI module (bundled apps, adapters, bindings) owns a
  canonical Markdown file residing with its source and referenced via `source:`
  metadata.
- **Labeled docs**: Every Markdown file needs YAML front matter with consistent
  Diátaxis placement, tags, and ownership data.
- **Linked & compatible docs**: Content must use valid relative links and pass
  CommonMark/GitHub markdown linting.
- **Structured docs**: Diátaxis remains the organizing frame and should be
  encoded in templates and contributor guidance.
- **Projected docs**: The maintained corpus has to be programmatically merged
  into the publishable site bundle.
- **Enforced checks**: Every rule above lives in automated tests or CI sensors,
  reusing existing SPI/unit hooks whenever possible before adding new ones.

## Critical Path & Sequencing

1. **Registry Instrumentation (Action 1)** captures the canonical file list and
   metadata; every downstream task depends on this inventory being accurate.
2. **Tree Charter & Ownership (Action 2)** consumes the registry output to lock
   in maintained vs. excluded paths and assign accountable owners.
3. **Taxonomy & Front Matter (Actions 3–4)** rely on the registry + ownership
   data to retrofit metadata consistently across the corpus.
4. **Modular Standardization & Content Curation (Actions 5–6)** depend on the
   normalized metadata to reconcile adapter/devdoc sources.
5. **Live-Doc + Lint Automation (Actions 7–8)** need standardized sources to
   enforce correctness and linkage through executable tooling.
6. **Projection, Enforcement, Cleanup (Actions 9–11)** tie everything together,
   using the lint/export apps and registry checks as CI gates before artifacts
   are published or old copies removed.

## Legacy Tooling Transition

- The current `scripts/gen_docs_inventory.py`, `scripts/lint_front_matter.py`,
  and `scripts/verify_doc_sources.py` prototypes were introduced for expediency
  but rely on Python, which is not an essential dependency for NoSQLBench.
- All functionality from those scripts must migrate into the Java-based
  docsys module as bundled apps or test utilities so the plan’s automation runs
  anywhere the CLI does without extra runtime requirements.
- Once the Java replacements ship, retire the Python scripts to keep the tool
  chain single-language and aligned with the layout mandate for enforced checks.

## Action Plan

1. **Build the Documentation Registry**
   - Automate a repo-wide scan that records every `.md` file with metadata
     (path, owning module, intended audience, generated vs. authored, `source:`
     targets, Diátaxis label).
   - Include inclusion/exclusion flags so the maintained corpus matches
     `docs/docs_layout.md`.
   - Store the registry output (e.g., `docs/docs_inventory.json`) so the managed
     corpus is explicit and auditable.
   - When adding registry validation logic, first audit existing unit or SPI
     tests (adapter doc enforcement, doc exporter tests) before adding new ones.
   - Replace the legacy `scripts/gen_docs_inventory.py` with a Java bundled app
     (e.g., `nb app docs-inventory`) in the docsys module so inventory generation
     requires no external runtime.
   - **Deliverable & Test**: `docs/docs_inventory.json` (plus schema) checked in,
     and a CI job that regenerates it and fails when maintained docs are missing
     or excluded paths slip in.

2. **Codify Maintained Trees & Ownership**
   - Declare the canonical tree layout (docs root, module-local docs, annotated
     API outputs) plus the explicitly excluded paths.
   - Capture provenance via `origin` metadata (module, path, extraction method)
     so modular docs stay co-located with their functionality while still being
     tracked centrally; only use `owner` when an alert-routing hook is required.
   - Provide contributor guidance clarifying where new docs should live and how
     to register additional sources.
   - **Deliverable & Test**: A published tree charter (appendix or `docs/docs_tree.md`)
     plus a CI check that rejects markdown committed outside approved roots.

3. **Define Diátaxis & Taxonomy Mapping**
   - Decide which Diátaxis quadrant each existing doc belongs to and codify the
     mapping in `docs/`.
   - Establish canonical tag sets and front-matter keys so contributors know how
     to classify new docs.
   - Maintain the mapping and vocabulary in `docs/docs_taxonomy.md`.
   - **Deliverable & Test**: `docs/docs_taxonomy.md` plus a lint rule that fails
     when a doc declares an unknown Diátaxis quadrant, tag, or front-matter key.

4. **Normalize Front Matter**
   - Author templates for user guides, references, tutorials, explanations,
     developer notes, adapter docs, and blog posts.
   - Retrofit high-traffic docs (README, release notes, adapter guides, etc.)
     with the templates.
   - Add a lint step in CI to fail when required front matter is missing or
     malformed.
   - Fold the `scripts/lint_front_matter.py` behavior into the Java doclint app
     so there is a single enforcement surface for metadata requirements.
   - **Deliverable & Test**: Template files under `docs/templates/` and a CI lint
     run (via the bundled app) that enforces required keys on the prioritized
     doc set.

5. **Standardize Modular Docs**
   - For each adapter/binding/app module, ensure a single authoritative Markdown
     file lives beside the code with proper metadata (including `source:` and
     ownership tags).
   - Remove committed build outputs from `target/` and regenerate them during
     builds instead.
   - Update the export pipeline so modular docs are ingested into the structured
     site automatically.
   - Leverage existing SPI or load-time checks where possible (e.g., adapter
     docs verified during driver registration) before adding new enforcement.
   - Replace the `scripts/verify_doc_sources.py` check with a doclint rule (or
     module-level test) that validates every `source:` front-matter path against
     the file’s actual location.
   - **Deliverable & Test**: One committed Markdown per module plus module-level
     tests (existing SPI hooks or new unit tests) that fail when the doc or its
     `source:` metadata is missing.

6. **Curate Dev vs. User Content**
   - Review `devdocs/` and `local/*.md` to identify which pieces graduate into
     public docs versus staying internal.
   - Promote finished pieces into the Diátaxis-aligned structure; leave
     experimental sketches in a clearly labeled internal area.
   - **Deliverable & Test**: A documented promotion rubric and registry metadata
     fields (`visibility`, `origin`) validated by CI to ensure public docs live
     in the maintained tree while experimental notes stay scoped to internal
     paths with accurate provenance.

7. **Strengthen Live-Doc Workflows**
   - Catalog existing literate tests (e.g., Uniform Workload Specification) and
     ensure they run in CI.
   - Document the authoring pattern for new live docs so examples remain
     executable and self-verifying.
   - **Deliverable & Test**: A manifest of live-doc suites plus CI build stages
     that execute them (JUnit/TestNG) and fail when examples regress.

8. **Enforce Linked & Compatible Docs**
   - Build a link graph from the registry and run link-checking as part of PR
     validation to catch orphaned or broken references early.
   - Add CommonMark/spec-compliant linting (e.g., markdownlint + custom rules)
     to ensure compatibility with GitHub and the publishing toolchain.
   - Implement the lint suite as a Java bundled app so it can run anywhere the
     NoSQLBench CLI does, reusing existing repo dependencies for markdown and
     YAML parsing/formatting instead of adding bespoke tooling.
   - When additional linting capability is required, prefer well-scoped Java
     ecosystem libraries with minimal transitive chains; only add dependencies
     that keep the tooling lightweight and maintainable.
   - Define the bundled app interface (`nb app doclint --inventory docs/docs_inventory.json --checks frontmatter,links`)
     with machine-readable output (JSON + human summary) and standard exit codes
     (0 success, non-zero categorized failures) so it plugs cleanly into CI.
   - Integrate the app as a test-unit driven action: wrap it in a Maven/Surefire
     test harness (or similar) so builds fail automatically when lint rules
     break, while still allowing developers to run it locally via the CLI.
   - Emit actionable diagnostics (path + owner) so fix-forward is easy.
   - **Deliverable & Test**: The `doclint` bundled app plus a Surefire (or
     equivalent) test that invokes it during `mvn test`, causing CI to fail on
     front-matter, link, or compatibility violations.

9. **Automate Projection & Publication**
   - Extend `nb5 export-docs` (or a successor) to merge the registry sources,
     Zola content, and live-doc outputs into a single structured bundle, using
     front matter metadata for placement.
   - Gate publication on link-checking, CommonMark validation, and registry
     completeness before publishing previews or releases.
   - **Deliverable & Test**: Updated export command plus release pipeline steps
     that invoke doclint + registry verification before artifacts publish.

10. **Codify Enforced Checks**
    - Implement module-level tests for adapter/binding front matter, `source:`
      pointers, and other SPI-specific requirements.
    - Add repo-level integration tests that read the registry, verify ownership
      metadata, and ensure excluded paths stay absent.
    - Document how to extend the enforcement suite to cover new doc types so
      future rules remain executable.
    - **Deliverable & Test**: A consolidated enforcement test suite (hosted with
      the docs tooling—`nb-docsys` is acceptable but not mandatory) that
      aggregates module/SPI tests and fails CI whenever a rule documented here is
      violated.

11. **Clean Up Legacy Artifacts**
    - Remove duplicate/generated markdown from source control where safe (e.g.,
      `target/**` copies, committed `exported_docs.zip`).
    - Document ownership and update cadence for each doc bucket so maintenance
      responsibilities are clear.
    - **Deliverable & Test**: A cleanup script or git hygiene check that deletes
      the legacy artifacts and a guard (e.g., `git ls-files` assertion) to ensure
      `target/**` markdown and `exported_docs.zip` never reappear.

## Next Steps

Treat this plan as the standing standard; move straight into execution:

1. Assign doc owners for each major bucket (core product docs, adapters,
   bindings, dev guides) and capture them in the registry.
2. Prototype the documentation registry plus front-matter/link linting to
   provide immediate feedback loops for contributors.
