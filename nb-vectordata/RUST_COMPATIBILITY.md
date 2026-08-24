# vectordata-rs compatibility contract

This module implements the programmatic dataset-access surface of
`vectordata-rs` (baseline: crate 1.7.1, source commit `1249310078785dbb59444f1c9bac14247767c286`).
When this document and an older Java implementation disagree, the Rust
implementation's current behavior and its format tests are normative.

## Supported data model

- Catalogs: JSON or YAML `catalog.json`/`catalog.yaml` entry lists and legacy
  flat `knn_entries.yaml`/`datasets.yaml` entries. A directory resolves in
  Rust order: `catalog.json`, `catalog.yaml`, then `knn_entries.yaml`.
- Dataset manifests: a `dataset.yaml`-shaped document (identified by its
  top-level `profiles` field) or a legacy flat entries document, regardless of
  an explicit YAML/YML filename. A dataset directory or URL resolves in Rust
  order: `dataset.yaml`, then `knn_entries.yaml`.
- Data: scalar, fixed-dimension xvec records, and variable-dimension vvec
  records with `IDXFOR__<file>.i32` or `.i64` index sidecars.
- Types: signed and unsigned 8/16/32/64-bit integers plus f16, f32, and f64.
- Sources: local files, `file:` URIs, and HTTP(S) URLs.
- Remote caching: sparse range caching, `.mref` SHA-256 Merkle verification,
  cache promotion to a local mapped reader, and full-transfer fallback when a
  server cannot service byte ranges.
- Cache setup: `cache_dir:` in `settings.yaml` wins. With
  `$VECTORDATA_HOME`, the default is `$VECTORDATA_HOME/cache` and no settings
  file is created. Otherwise the client uses an absolute `$XDG_CACHE_HOME` or
  `~/.cache` candidate, auto-persists `cache_dir:` only when the home
  filesystem is the largest writable mount (or mount discovery is unavailable),
  and requires an explicit setting when a larger writable mount is elsewhere.

## Deliberate compatibility decisions

`metadata_results` is canonical; `metadata_indices` and `predicate_results`
are accepted aliases. `prefiltered` is canonical and `filtered` is accepted as
a legacy alias. A facet source is resolved relative to its manifest, while a
catalog entry's `path` is resolved relative to its catalog. Explicit client
settings win over environment and settings-file values.

Malformed manifests, unsupported extensions, invalid windows, malformed index
sidecars, invalid Merkle references, inconsistent data lengths, unavailable
profiles, and failed integrity checks fail with a `VectorDataException`; they
are never silently repaired.

The dispatch behavior above is covered by deterministic local tests and an
embedded HTTP-server integration test, so Rust precedence is stable across
releases instead of depending on a filename convention.

Automatic cache setup is tested exclusively with temporary, explicit settings
roots. The test suite never writes a developer's real `~/.config/vectordata`.

## Known source-level clarifications

The Rust storage code permits a no-`.mref` HTTP fallback even where older
documentation only described full transfer. This Java implementation supports
that fallback. It also preserves custom manifest facets and supports float
typed reads, which are part of the advertised API contract.
