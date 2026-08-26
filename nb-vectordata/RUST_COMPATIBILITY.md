# vectordata-rs compatibility contract

This module implements the programmatic dataset-access surface of
`vectordata-rs` (baseline: crate 1.8.0, source commit `96d787722b39e60ceebf141045a2f8156b1ce592`).
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
  records with `IDXFOR__<file>.i32` or `.i64` index sidecars. Both sidecar
  layouts are accepted: `N+1` entries ending in an end-of-data sentinel,
  and the Rust walk-built form of `N` record starts with no sentinel.
- Windows: the full `DSWindow` grammar — `..` bounds, open ends, `(`/`]`
  bound adjustment, count/size suffixes (`K`/`M`/`G`/`T`, `KB`…`TiB`,
  compound `1g24m`), comma-separated intervals, and the structural
  window-suffix sugar on source strings (`base.fvec[0..1M)`). Intervals
  that select no records are rejected at parse. Readers apply the first
  interval and clamp both bounds to the data.
- Prefetch: caller-supplied record windows on any facet via
  `prefetchPlan`, `prefetch`, and `prefetchInBackground` on
  `TestDataView`, with `WholeFacetFallback` consent gating, chunk-level
  `RangeFill` residency accounting, chunk-adjacency range coalescing, an
  offset-index cache scoped to the view's facet handle, and an empty
  window meaning the whole facet (a request, never a degrade).
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

Facet names resolve through the `StandardFacet` canonical/alias table:
`base_vectors`, `query_vectors`, `neighbor_indices`, `neighbor_distances`,
`metadata_*`, and the `prefiltered_*`/`postfiltered_*` families are canonical,
with the Rust shorthand aliases (`base`, `train`, `query`, `gt`,
`ground_truth`, `metadata_indices`, `predicate_results`, `filtered_*`, ...)
accepted everywhere a facet is named — manifest keys, legacy `knn_entries`
keys, and lookup by name. The resolved facet map is keyed canonically.
A facet source is resolved relative to its manifest, while a
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

Prefetch semantics — scalar windows at the element stride, planning that
never rebuilds a remote offset index (`degradesToFullDownload` plus the
consent gate instead), sentinel-tolerant sidecar parsing with
`prerequisiteBytes` as record starts × 8, size-directed sidecar probing,
and atomic persistence of locally walked indexes — follow the Rust
source directly as of the baseline commit above.

Remaining representation differences:

- **A non-range-capable remote facet degrades a window** rather than
  planning a partial fetch it cannot perform. Rust reaches the same end
  state by downloading such a facet whole at open; Java opens lazily, so
  the plan reports the honest cost and `WholeFacetFallback` decides.
- **The offset-index cache is view-scoped**, not per open call: holding
  a `TestDataView` is how a caller says it will ask repeatedly, matching
  the lifetime intent of the Rust facet handle.
- **Unbounded interval ends are `Long.MAX_VALUE`** rather than `u64::MAX`
  and clamp to the facet, which no supported payload can exceed.
