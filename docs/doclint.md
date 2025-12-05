---
title: Docs-Lint Usage
description: "How to run nb app docs-lint to validate metadata and links."
audience: meta
diataxis: howto
tags:
  - documentation
  - linting
component: docsys
topic: docops
status: live
owner: "@nosqlbench/docs"
generated: false
---

# Docs-Lint Usage

`nb app docs-lint` validates front matter, taxonomy fields, curation metadata,
and relative links for every document tracked in the published registry
(`nb-docsys/target/docs_inventory.json`). Use it locally before pushing changes
so metadata regressions are caught immediately.

## Running Locally

1. Regenerate the registry (and publish the build artifact) from the repo root:

   ```bash
   mvn -pl nb-docsys test
   ```

   This produces `nb-docsys/target/docs_inventory.json`.

2. Execute docs-lint against the published inventory (this writes `target/doclint-report.json`):

   ```bash
   ./nb5/nb5 docs-lint --inventory nb-docsys/target/docs_inventory.json
   ```

   If `--inventory` is omitted, docs-lint searches in order:

   1. `target/docs_inventory.json`
   2. `nb-docsys/target/docs_inventory.json` (from `mvn -pl nb-docsys test`)
   3. `docs/docs_inventory.json`

Docs-lint exits non-zero and lists files with missing front matter keys, invalid
taxonomy values, broken relative links, or lingering staging docs
(`curation_state=staging`). Detailed results are also written to
`target/doclint-report.json` for tooling to consume. The report now includes a
`by_file` map so CI output can group errors by document path while preserving
the existing `errors` count and flat `messages` list for compatibility, plus a
`link_graph` adjacency map (`<doc> -> [linked_markdown_targets]`) and an
`orphans` list (docs with no inbound links from curated paths) to help identify
coverage gaps or orphaned pages. Orphan reporting is informational only; it does
not currently fail the build.

Additional link rules enforced:
- Relative links that resolve into excluded/staging paths (`local/**`,
  `sort_docs/**`, `target/**`) are blocked.
- Anchors: when links use in-page anchors for metrics/api/architecture topics,
  the referenced heading must exist (doclint canonicalizes headings to GitHub-style
  slugs and fails when anchors point to missing headings), including `[#anchor]`
  links within the same file. Links to other Markdown files must also resolve to
  headings that exist in the target file when an anchor fragment is present.
- Reference-style links (e.g., `[label][id]` or `[label][]`) must have a
  corresponding definition (`[id]: https://example`) in the same document; missing
  definitions fail the check.

## CI Expectations

- `DocLintAppTest` runs during `mvn test`, so every build exercises docs-lint.
- The registry snapshot is schema-validated and copied into
  `nb-docsys/target/docs_inventory.json`; downstream automation should consume
  that file to ensure all stages operate on the same vetted data.
