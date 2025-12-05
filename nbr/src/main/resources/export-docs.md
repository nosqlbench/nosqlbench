---
source: nbr/src/main/resources/export-docs.md
title: "Docs-Export"
description: "Doc for docs-export."
tags:
  - docs
audience: developer
diataxis: reference
component: docsys
topic: architecture
status: live
owner: "@nosqlbench/docs"
generated: false
---

# docs-export

This is the built-in app that allows NB5 to export docs for integration into other systems.
During a build of NB5, this app is used to auto-inject much of the content into the doc site.
Invoking it as `nb5 docs-export --inventory docs/docs_inventory.json --doclint-report target/doclint-report.json`
creates (or overwrites) a file called `exported-docs.zip`, containing the markdown source files for
binding functions, bundled apps, specifications, and so on, **plus** a `metadata/` folder that embeds
the validated inventory and doclint report so downstream tooling can trust the exported bundle.
The command now requires the referenced inventory and doclint files to exist and enforces that the
doclint report contains zero errors before emitting the bundle.

Using this mechanism ensures that:
1. NB5 contributors who build drivers, bindings, or other modular pieces can use idiomatic forms of
   documentation, allowing them to work in one code base.
2. Contributors are encouraged to make integrated tests follow a literate programming pattern,
   so that they work as examples as well as verification tools.
3. Users of NB5 will never see documentation which is included in integrated tests, because any
   failing test will prevent a failed build from being published or considered for a release.

## Command Options

```
nb5 docs-export [--zipfile exported_docs.zip] \\
                [--inventory nb-docsys/target/docs_inventory.json] \\
                [--doclint-report nb-docsys/target/doclint-report.json] \\
                [-f|--force]
```

- `--zipfile` controls the output artifact path (defaults to `exported_docs.zip`).
- `--inventory` defaults to `nb-docsys/target/docs_inventory.json` (vetted by `mvn -pl nb-docsys test`);
  if missing, the exporter falls back to `target/docs_inventory.json` and then `docs/docs_inventory.json`.
- `--doclint-report` defaults to `nb-docsys/target/doclint-report.json`; if missing, it falls back to
  `target/doclint-report.json`. The command fails when the report shows any errors and embeds the
  report when it is clean. Error output now uses the `by_file` map (when present) to group top issues
  per document for quicker triage.
- `-f` / `--force` overwrites an existing output zip instead of failing.

This is a relatively new mechanism that will be improved further.
