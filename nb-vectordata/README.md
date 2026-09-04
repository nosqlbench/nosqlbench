# nb-vectordata

`nb-vectordata` is the standalone Java programmatic-access implementation of
the vectordata dataset format. It is intentionally limited to catalog,
manifest, vector-reader, transport, cache, and integrity-verification APIs;
workload generators and legacy command-line tooling are not part of this
module.

The compatibility contract is [RUST_COMPATIBILITY.md](RUST_COMPATIBILITY.md).

## Minimal use

```java
Catalog catalog = Catalog.of(CatalogSources.defaults());
TestDataView view = catalog.open("my-dataset", "default");
VectorReader<float[]> base = view.baseVectors();
float[] vector = base.get(0);
```

Local paths and HTTP(S) URLs are both accepted for manifests, catalogs, and
facet sources. HTTP data is range-cached and uses a `.mref` sidecar whenever a
server provides one.

## Prefetching a window

Any facet can be warmed for a record range the caller names — a profile's
`window:` is a convenience, not a fence:

```java
DSWindow window = DSWindow.parse("[0..1K, 5K..6K]");
PrefetchPlan plan = view.prefetchPlan("base_vectors", window);   // cost, before fetching
PrefetchReport done = view.prefetch("base_vectors", window, WholeFacetFallback.REFUSE);
PrefetchHandle bg = view.prefetchInBackground("base_vectors", window, WholeFacetFallback.REFUSE);
bg.join();
```

The plan reports byte ranges — each qualified by the shard it lies in —
chunk-level residency, overfetch, and the offset-index prerequisite for
variable-length facets. A window that cannot be resolved for its format is
refused rather than silently fetching the whole facet; pass
`WholeFacetFallback.ALLOW` to accept that, or pass `DSWindow.ALL` to request
the whole facet outright, which needs no permission.

## Prebuffering a profile

`prebuffer` drives every facet of a profile to resident state, each fetched
**against the window it declares** — so a sized profile over a
multi-terabyte base pulls the records it can address and nothing more:

```java
view.prebuffer(WholeFacetFallback.REFUSE, (cached, total) -> meter.update(cached, total));
```

Every facet is planned before any is fetched. A declared window the format
cannot map is refused under `REFUSE`, exactly as a requested window would be;
`ALLOW` accepts the whole facet instead. Slab facets — paged metadata,
addressable as `m.slab` or by namespace as `m.slab:content` — are planned
and fetched by the pages a window spans, located through the index in the
slab's tail; this module does not decode their records.

## Facets spread across several files

A facet may be a series of files forming one dense ordinal space, in either
of the reference forms:

```yaml
format_version: 2
profiles:
  default:
    base_vectors:                      # uniform: names follow the pattern
      source: base_vectors__NNNN.fvec
      shard_stride: 1000000
      shard_count: 12
      record_count: 11412003
    metadata_content:                  # explicit: files named as they are
      source:
        - corpus-a.u8[0..1M]=1M
        - corpus-b.u8[500K..1500K]=1M
        - corpus-b.u8[3M..3250K]=250K
      record_count: 2250000
  10m:
    base_count: 10000000               # inherits the base, windowed to 10M
```

Readers present the same surface whatever the layout: `count()` is the
series total and `get(o)` reads from the shard that owns ordinal `o`. A
window in the facet's `window:` (or as a suffix on the uniform pattern) is
in facet ordinals and clips the series, not a shard; a window on an explicit
entry is in that file's ordinals and carves the shard out of it. Prefetch
plans decompose a window across the shards it touches and fetch only those.

For a release canary against a Rust-hosted dataset, run Maven with
`-Dvectordata.canary.catalog=<catalog-url>` and
`-Dvectordata.canary.dataset=<dataset>`; optionally set
`-Dvectordata.canary.profile=<profile>`.
