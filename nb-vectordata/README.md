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

The plan reports byte ranges, chunk-level residency, overfetch, and the
offset-index prerequisite for variable-length facets. A window that cannot be
resolved for its format is refused rather than silently fetching the whole
facet; pass `WholeFacetFallback.ALLOW` to accept that, or pass `DSWindow.ALL`
to request the whole facet outright, which needs no permission.

For a release canary against a Rust-hosted dataset, run Maven with
`-Dvectordata.canary.catalog=<catalog-url>` and
`-Dvectordata.canary.dataset=<dataset>`; optionally set
`-Dvectordata.canary.profile=<profile>`.
