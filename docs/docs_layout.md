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
* Original hand-written documentation from the doc site, cached at `local/nosqlbench-build-docs/site/content`.
  * The `local/**` tree is a transient scratch area; copy curated docs into the maintained roots before enforcing them.
* Living examples of docs which are tested by unit tests like UniformWorkloadSpecificationTest

# Maintained Docs Location
The set of maintained docs shall be comprised of:
* all markdown files and structure in the `docs/` directory (this is the curated
  root for all hand-authored documentation that is not co-located with a module)
* all modular, programmatic, or other docs provided by annotations or similar

### Cached Site vs. Canonical Sources
The `local/nosqlbench-build-docs/site/content/**` tree is a staging artifact. When curating it:

* Only promote **non-generated** or otherwise unique Markdown into `docs/site/**`. Treat “cached” pages that merely mirror module-owned docs as disposable.
* Adapter/binding/API references remain with the module that generates them (for example, `nb-adapters/**/src/main/resources/*.md`). Do **not** duplicate those files under `docs/site/**`; instead, ensure the registry metadata (`origin`, `source`, taxonomy fields) describes where they belong so the doc export step can place them automatically.
* If the curated site needs to mention a module doc, add a short authored page that summarizes the concept or links to the canonical source, but never copy the body wholesale. Treat each rendered category (reference, guide, blog, etc.) as **opaque with respect to specific instances**: the layout describes the purpose/pattern of that category, not a hard-coded list of entries. Metadata stays orthogonal so the projection system can populate categories dynamically.
* The front-matter metadata is the contract between taxonomy (what the doc is about) and topology (where it is rendered). Keep that metadata accurate so projection tooling can assemble the publishable site without re-ingesting cached copies.

## Excluded Paths
Git ignored files and .github files are all excluded from the maintained set.
Generated artifacts (for example `target/**`, build caches, or other ignored paths) are explicitly excluded.
`local/**` is scratch space and stays excluded even if it temporarily holds Markdown.
`sort_docs/**` is a historic holding bin; treat it as staging that must be curated into the maintained tree before publication.
All canonical adapter docs live in their respective source modules (e.g., `nb-adapters/*/src/main/resources/`); any duplicates produced under `target/**` are ignored.

## Ownership Model
There are no organic documentation owners. Accountability follows the module (adapter, binding, bundled app, exporter, etc.) that implements the functionality being described. Every document must declare `origin` metadata that ties it back to that module/path, and that provenance—not an individual maintainer—defines who must keep it accurate. Use the optional `owner` field only when a module-level GitHub team or automation hook needs to receive alerts; never point it at specific developers.

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
It should be possible to take the set of maintained docs, including those which
are live example docs, annotated methods and classes, and other sources, and
through a programmatic organization process, assemble them into a
well-structured copy of the docs, with the metadata in the front matter being
used to apply topological layout to taxonomic or other cues for consistent
organization. The canonical bundling step (`nb5 docs-export`) must remain
format-neutral—emitting only Markdown plus metadata guided by this taxonomy ↔
topology mapping. Any static-site–specific transformations (Zola, etc.) should
layer on top of that canonical export via separate commands. Those renderer
commands are the correct place to concentrate SSG-specific behavior—including
obtaining or packaging the rendering tool itself—so long as the coupling stays
inside that renderer. For example, `docs-render-zola` may download, verify, and
cache the Zola binary (via Sigstore) **and** clone the abridge theme from its
Git repository before copying it into the build, because those mechanics apply
solely to the Zola projection layer; they must not leak back into the canonical
exporter or any other renderer.

## Enforced Checks
Every documentation requirement described here must be enforced in code. Module-specific rules (for example, “each adapter ships a `source:`-annotated markdown file in `src/main/resources`”) should be implemented as unit or module-level tests within that module. Cross-cutting requirements (such as verifying `source` paths resolve to tracked files or that ignored/generated artifacts are excluded) should be validated via integrated tests at the repo level. This ensures the rules stay executable and fail CI when broken. Before adding a new check, review the existing unit/integration suites (e.g., SPI-based adapter validation or doc exporter tests) to avoid duplicating logic.

All such enforcement must run inside the main build (e.g., Maven) and be hooked in via unit-test wrappers or other native build phases. Do **not** introduce external CI/CD services, PR bots, or bespoke pipelines solely to validate documentation artifacts; the primary build remains the single gate for doc validity and integrity.
