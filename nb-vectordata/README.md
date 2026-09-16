# nb-vectordata

`nb-vectordata` is the standalone Java programmatic-access implementation of
the vectordata dataset format. It is intentionally limited to catalog,
manifest, vector-reader, record-codec, transport, cache, and
integrity-verification APIs; workload generators and legacy command-line
tooling are not part of this module.

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

## Selecting profiles

The second argument to `open` is a **selector**: a bare profile name as it
always was, or an expression over the profile's automatic `profile` tag, its
structural fields (`base_count`, `maxk`, `partition`, `inherits`), and its
declared `attributes:`. `null` means `default`; `profile=*` means every
profile. A single-profile surface refuses a set, and the set surface returns
every match in the dataset's size order:

```java
TestDataView one = catalog.open("tessera", "size=10m,predicates=uniform-2");   // exactly one, or an error
List<TestDataView> ladder = catalog.openProfiles("tessera", "family=uniform,selectivity=1e-3..1e-2");
TestDataView spec = catalog.openProfile("https://host/ds:profile=^10m-.*$");    // the head is found by its shape
TestDataGroup group = catalog.openGroup("tessera");
List<String> names = group.select("or(10m,20m)");                              // size-ordered
group.prebuffer(names, WholeFacetFallback.REFUSE, profile -> meter.forProfile(profile), total -> warn(total));
```

Values are read by their spelling — `^…`/`…$` a regular expression, `*`/`?`/`[`
a glob, `lo..hi` a half-open interval, `10m`/`128mi`/`1e-3` numbers under the
window count rule, `true`/`false`, anything else or anything quoted a literal
— and every comparison folds case. A `,` is AND; `and(…)`, `or(…)`, and
`not(…)` nest; an absent attribute matches nothing, `not(key=x)` included. A
catalog entry resolves the same selector before anything is fetched
(`entry.select(...)`) and states the dataset's `formatVersion()`, so a
consumer can refuse a listed dataset it cannot read. A failure is a
`SelectionException` whose `kind()` is `SYNTAX`, `NO_MATCH`, `AMBIGUOUS`, or
`NO_DEFAULT`, with the profiles on offer in the message.

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
slab's tail, and read by ordinal as records (below).

## Reading a slab facet's records

A facet holds either runs of a fixed-width element or opaque records of
their own length. Ask which before choosing a reader; each refuses the
other shape by naming the reader that opens it:

```java
if (view.facetShape("metadata_content") == FacetShape.RECORDS) {
    RecordFacet facet = view.openFacetRecords("metadata_content");
    Records<ANode> nodes = facet.decode(Codecs.ANODE);                 // stage 1: the record as it is
    Records<String> json = facet.decode(Codecs.text(Vernacular.JSON)); // stage 2: rendered
    Records<Object> trees = facet.decode(Codecs.TREE);                 // plain maps and lists
    Records<String> byName = facet.decode(Codecs.byName("readout"));

    ANode first = nodes.get(0);
    if (first instanceof ANode.M m && view.facetShape("metadata_predicates") == FacetShape.RECORDS) {
        ANode p = view.openFacetRecords("metadata_predicates").decode(Codecs.ANODE).get(0);
        boolean hit = PredicateEvaluator.evaluate(((ANode.P) p).node(), m.node());
    }
    RecordFacet schema = facet.namespace("schema");                    // a sibling document, same files
}
```

A record decodes by its own leader byte — `0x01` an `MNode`, `0x02` a
`PNode` — so a facet holding both reads without the caller saying which.
Rendering and parsing are available directly through `Vernaculars`,
in every vernacular the reference names (`json`, `jsonl`, `yaml`, `sql`,
`sqlite`, `cql`, `cddl`, `readout`, their `-schema` and `-value` forms,
and `display`); a `PNode`'s display form reads back with `PNodeDisplay`.
Reading one record from a remote facet fetches the page that holds it,
not the file. A series of slabs is one facet of records in shard order.

## Binding records to parameters

Rendering produces text for a human; binding produces named, typed values
for a driver, with the statement prepared once and only values moving per
cycle. The layout is learned once, a binder is compiled against it, and
the per-record path never looks at a field name:

```java
RecordFacet facet = view.openFacetRecords("metadata_content");
Layout layout = Layout.discover(facet);                     // once: names, and what each binds as
Binder binder = Binder.select(layout, "id", "tag")          // template order
    .withOverrides(Map.of("id", "pk"));                     // parameter names, not field names
List<String> parameters = binder.parameters();              // [pk, tag] — prepare the statement
List<BindType> types = binder.types();                      // [int64, text]

for (long ordinal : cycle) {                                // cycle → ordinal is the caller's policy
    binder.bindEach(facet.recordBytes(ordinal), (slot, field) -> {
        if (slot == 0) statement.setLong(slot, field.longValue());
        else statement.setString(slot, field.stringValue());
    });
}

List<Form> offered = Forms.of(facet);                       // declared in the `forms` namespace, or one implicit form
Binder row = Forms.byName(facet, "row").binder(layout);

PredicateBinder where = PredicateBinder.compile(template, layout);   // shape once, typed from the fields
where.bindEach(predicates.recordBytes(ordinal), (condition, comparands) -> { /* condition.parameter(), condition.bindType() */ });
```

A `Field` is a view over the record's own bytes; its primitive accessors
copy nothing, and `field.value()` materializes the AST view for a caller
that wants structure. A `Half` binds as a float, a `DateTime` as text, and
a container's element type as undetermined — the bind types answer what
a value *is*, which the schema vernaculars, right about DDL, do not.

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
plans decompose a window across the shards it touches and fetch only those,
and a `FacetDescriptor` reports its `shardCount()` before the series is
realized.

## Format versions, layers, and tags

`format_version` is a minimum reader requirement, and an absent field means
`1` with the dataset held to it: a manifest that never said what it is is
read as single-file, pre-inheritance, pre-tag, and content that needs more
is refused naming the version to declare. Version `2` is a multi-file facet;
version `3` is a dataset whose profiles state their parents — `inherits:`
on every profile but `default`, every parent real — and may declare a
`profile_tags` schema:

```yaml
format_version: 3
profile_tags:
  size: ~                # a naming tag: no default, set per profile
  predicates: ~
  family: stratified     # carried by every profile, but not a name
profiles:
  default:
    base_vectors: base.fvec
    metadata_predicates: profiles/base/predicates.slab
  10m:                   # a size layer: the unfiltered benchmark at ten million
    inherits: default
    base_count: 10000000
    neighbor_indices: profiles/10m/neighbor_indices.ivecs
    attributes: { size: 10m }
  10m-uniform-2-1e-2:    # a predicate set at the layer's size
    inherits: 10m
    metadata_predicates: profiles/10m-uniform-2-1e-2/predicates.slab
    metadata_results: profiles/10m-uniform-2-1e-2/results.slab
    attributes: { size: 10m, predicates: uniform-2, selectivity: 1e-2, family: uniform }
```

What a profile inherits depends on the axis of the step, which is derived
from `base_count`: a child whose count differs from its parent's is a size
step — the base facets arrive re-cut to `[0..base_count)`, and the per-size
outputs (ground truth, `metadata_results`) do not cross — while a child at
its parent's count inherits everything it does not override, `base_count`
included. `group.profileTags()` and `group.formatVersion()` expose the
schema and the version a caller may need before choosing a profile.

For a release canary against a Rust-hosted dataset, run Maven with
`-Dvectordata.canary.catalog=<catalog-url>` and
`-Dvectordata.canary.dataset=<dataset>`; optionally set
`-Dvectordata.canary.profile=<profile>`.
