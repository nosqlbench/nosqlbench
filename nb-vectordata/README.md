# nb-vectordata

`nb-vectordata` is the standalone Java programmatic-access implementation of
the vectordata dataset format. It is intentionally limited to catalog,
manifest, vector-reader, transport, cache, and integrity-verification APIs;
workload generators and legacy command-line tooling are not part of this
module.

The compatibility contract is [RUST_COMPATIBILITY.md](RUST_COMPATIBILITY.md).
The implementation checkpoints and validation evidence are maintained in
[PORTING_PLAN.md](PORTING_PLAN.md).

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

For a release canary against a Rust-hosted dataset, run Maven with
`-Dvectordata.canary.catalog=<catalog-url>` and
`-Dvectordata.canary.dataset=<dataset>`; optionally set
`-Dvectordata.canary.profile=<profile>`.
