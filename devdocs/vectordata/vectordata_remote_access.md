# Testing remote vectordata access

How to exercise remote dataset access end to end: the Java API in
`nb-vectordata`, expr functions in workload templates, and inline op
bindings on the CLI — including precache/prefetch in both profile-based
and caller-named ordinal-range forms.

## Quick smoke test

[`remote_smoke_test.sh`](remote_smoke_test.sh) checks the base, query,
and neighbor-index facets of one profile by eagerly warming exactly the
records each check reads — the same chunks demand paging would fetch,
cheap even against a billion-record facet, with the plan announced and
download progress emitted on stderr while the bytes move — and aborts
on the first failure:

```bash
VECTORDATA_CATALOG='https://your.host/path/catalog.yaml' \
  DATASET=mydataset:myprofile bash remote_smoke_test.sh
```

`DATASET` is `<dataset>` or `<dataset>:<profile>` (the profile defaults
to `default`); `NB` overrides how nosqlbench is invoked (default `nb5`,
e.g. `NB='java -jar nb5.jar'`). `VECTORDATA_CATALOG` outranks any
`catalogs.yaml` in the config home, and the script isolates
`VECTORDATA_HOME` under `/tmp/vdtest` so it never touches
`~/.config/vectordata` or your real cache.

## Watch a download happen

`vectordata_demo` is a workload bundled in nb5 — no checkout, no
database. It uses the stdout driver, so the only thing it exercises is
dataset access: it prints what it derived from the dataset, fetches a
record range with a live meter, and reads records back.

```bash
export VECTORDATA_CATALOG='https://your.host/path/catalog.yaml'
export VECTORDATA_HOME=/tmp/vdtest     # empty dir ⇒ nothing is cached yet
nb5 vectordata_demo dataset=mydataset:myprofile
```

That is the scenario form — the workload name stands alone, with no
`run` in front of it (`run` is itself a command, so `nb5 run
vectordata_demo` fails). The equivalent long form names the full path:

```bash
nb5 run workload=activities/examples/vectordata_demo.yaml dataset=mydataset:myprofile
```

`nb5 --cat vectordata_demo` prints it and `nb5 --copy vectordata_demo`
drops an editable copy in the working directory.

The meter goes to stderr while the bytes move:

```
[vectordata] mydataset:myprofile:base_vectors: fetching 29.0 MiB in 1 range(s)
[vectordata] mydataset:myprofile:base_vectors: 12.0 MiB / 30.0 MiB
[vectordata] mydataset:myprofile:base_vectors: fetch complete (29.0 MiB)
```

To watch the fetch overlap the run instead of preceding it, turn off the
load-time prefetch and let the binding warm in the background:

```bash
nb5 vectordata_demo dataset=mydataset:myprofile \
  prefetch=false mode=background cycles=1000
```

Parameters: `cycles`, `records` (how many ordinals the demo spans),
`prefetch` (`true|false`), and `mode` (`none|eager|background`). The
meter is silent when there is nothing to fetch, so a second run against
a warm cache prints nothing — point `VECTORDATA_HOME` at an empty
directory to see it fetch again.

Beyond the script's checks:

```bash
# any other facet by name, standard or custom:
nb5 run driver=stdout cycles=3 threads=1 format=readout \
  "op={{Facet('mydataset:myprofile','metadata_results','','none');Stringify()}}"

# caller-named ordinal range, warmed in the background while cycles run
nb5 run driver=stdout cycles=3 threads=1 format=readout \
  "op={{BaseVectors('mydataset:myprofile','[0..1k)','background');Stringify()}}"

# whole-profile warm-up (the eager default) — fetches the profile's
# full window before the first cycle, so size it deliberately
nb5 run driver=stdout cycles=3 threads=1 format=readout \
  "op={{BaseVectors('mydataset:myprofile');Stringify()}}"
```

## Publishing a test dataset

A dataset is a directory (or HTTP prefix) holding a `dataset.yaml` and
its facet files:

```yaml
name: example
profiles:
  default:
    base_vectors: base.fvec           # canonical keys or aliases (base:, query:, gt:, ...)
    query_vectors: query.fvec
    neighbor_indices: gt.ivecs
    metadata_content: meta.ivecs      # vvec, with IDXFOR__meta.ivecs.i32 sidecar
  first100k:
    base_vectors: base.fvec[0..100k)  # a profile-declared window
```

For remote serving, place the files behind any HTTP server that honors
`Range` requests. Publish a `<file>.mref` Merkle sidecar next to each
facet to get chunk-verified sparse caching; without one, a range-capable
server still gets chunked caching, and a server that ignores `Range`
falls back to one full transfer. Variable-length (vvec) facets should
publish their `IDXFOR__<file>.i32|.i64` offset sidecar — either layout
(N starts, or N+1 entries ending in the payload size) is accepted.

## Pointing a test at it

Catalog resolution order: `-Dvectordata.catalog=<path-or-url>`, then
`$VECTORDATA_CATALOG`, then `catalogs.yaml`/`catalog.yaml` under
`$VECTORDATA_HOME` or `~/.config/vectordata`. A catalog lists datasets:

```yaml
datasets:
  - name: example
    path: example/dataset.yaml        # or an absolute http(s) URL
```

In Java tests, isolate the cache so nothing touches your real config:

```java
VectorDataSettings settings = VectorDataSettings.builder()
    .cacheDirectory(temporaryDir.resolve("cache")).build();
TestDataView view = Catalog.of(CatalogSources.of(catalogUri), settings)
    .openProfile("example:default");
```

## Java API

Reads fault chunks in on demand; prefetch warms them ahead of time.

```java
// Whole-facet precache: an empty window requests everything and needs
// no consent — that is a request, not a fallback.
view.prefetch("base_vectors", DSWindow.ALL, WholeFacetFallback.REFUSE);

// Profile-based: a windowed profile clips its readers, and warming the
// declared window fetches only those bytes.
TestDataView first100k = catalog.openProfile("example:first100k");
String declared = first100k.facet("base_vectors").orElseThrow().window();
first100k.prefetch("base_vectors", DSWindow.parse(declared), WholeFacetFallback.REFUSE);

// Ordinal-range based: the window is the caller's to choose — a
// profile window is a convenience, not a fence.
DSWindow window = DSWindow.parse("[5M..6M)");           // or "[0..1K, 5K..6K]"
PrefetchPlan plan = view.prefetchPlan("base_vectors", window);
plan.bytesToFetch();          // chunk-granular cost, before anything moves
plan.overfetchBytes();        // alignment + gap-bridging bytes nobody asked for
plan.isResident();            // already warm?
plan.degradesToFullDownload();// window unresolvable for this format/source

view.prefetch("base_vectors", window, WholeFacetFallback.REFUSE);

// Background: plan up front, fetch overlapping the reads.
PrefetchHandle handle = view.prefetchInBackground("base_vectors", window,
    WholeFacetFallback.REFUSE);
handle.plan().requests();     // known immediately
// ... read while it fetches; reads that overtake it fault chunks themselves
handle.cancel();              // granular to a range; fetched ranges stay cached
PrefetchReport report = handle.join();  // rethrows worker failures
```

An unresolvable window (no vvec offset index published, unmappable
format) is refused under `REFUSE` with the facet size in the message;
pass `WholeFacetFallback.ALLOW` to consent to the whole-facet fetch.
Planning never downloads data to price a transfer.

For test-harness patterns — an embedded `com.sun.net.httpserver` serving
fixtures with `.mref` sidecars, counting `Range` requests to prove reads
inside a prefetched window fetch nothing further — see
`PrefetchRemoteIntegrationTest` and `RemoteCacheIntegrationTest` in
`nb-vectordata`.

## Expr examples

`{{...}}` sigils in a workload evaluate at load time when they carry the
expr forms (`{{= expr}}`, `{{name = expr}}`, `{{@name}}`); anything else
passes through untouched. The vectordata expr functions:

```yaml
# One-call warm-up: every facet the profile declares, each to the
# window the profile declares for it. This is the form to reach for —
# it never fetches more than the profile describes.
# {{= prefetchProfile("example:default")}}

# A cycle range, in the coordinates an activity already speaks:
# {{= prefetchCycles("example:default", "base_vectors", 0, 100000).rangesFetched()}}

# An explicit window, priced first:
# {{= prefetchPlan("example:default", "base_vectors", "[0..100k)").bytesToFetch()}}
# {{= prefetch("example:default", "base_vectors", "[0..100k)").rangesFetched()}}

# Background warm-up captured in an expr variable, joined later:
# {{warmup = prefetchBackground("example:default", "base_vectors", "[0..1M)")}}
# {{= warmup.join().rangesFetched()}}

# Readers, for expr-driven checks:
# {{= baseVectors("example:default").count()}}
# {{= facet("example:default", "metadata_results").count()}}
# {{= windowedFacet("example:default", "base_vectors", "[10..20)").count()}}
# {{= variableFacet("example:default", "metadata_content").count()}}
```

Diagnose expr processing with `dryrun=exprs` on the activity to dump the
expression-processed workload and context.

### Three things to know when prefetching from a workload

**Expressions evaluate when the workload loads, not when a block runs.**
The whole document is expression-processed before ops are parsed, so a
prefetch written into an op body fires on every activity start —
including `run tags='block:drop'` — no matter what tags that op carries.
That is fine for an up-front warm-up, but it is not a way to tie
fetching to a phase. To warm only when a phase actually uses the data,
let the bindings do it: `BaseVectors('ds:profile','[0..1M)','eager')`
warms when that binding resolves, which happens only for blocks that use
it, and `'background'` lets the fetch overlap the run.

**An empty window means the whole facet, not the profile's window.**
`prefetch(spec,"base_vectors","")` on a sized profile fetches the entire
shared base file — which may be terabytes — because an empty window is a
request for everything rather than a fallback. Use `prefetchProfile`,
which resolves each facet to its declared window, or pass the window
explicitly.

**An assignment sigil renders its value.** `{{p = "ds:" + profile}}`
substitutes the assigned value into the document at that spot, so
writing assignments in an op body leaves stray text in the op. Put them
in `description:` (where they read as documentation) and reference them
with `{{@p}}` where the value belongs — the workload example below does
exactly this.

For a complete workload where every dataset-shaped parameter is derived
this way, see
`nb-adapters/adapter-cqld4/src/main/resources/activities/baselinesv2/cql_vector_dataset.yaml`:
the only dataset input is `dataset='<dataset>:<profile>'`, and the
schema's vector dimensions and similarity function, the rampup and
search cycle counts, the ANN `LIMIT`, and the recall verifier's `k` all
come from the dataset through expr assignments — with the bindings
reading through the vectordata mappers, which warm the profile's base
window eagerly before the first cycle.

## Inline op examples

Binding recipes in op templates address the same surface. Smoke-test a
remote dataset from the CLI with the stdout driver:

```bash
# Whole-facet precache (default), then read:
java -jar nb5.jar run driver=stdout cycles=10 threads=1 format=readout \
  "op={{BaseVectors('example:default');Stringify()}}"

# Demand-paged, no warm-up:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{BaseVectors('example:default',false);Stringify()}}"

# Profile-based window: readers are clipped to the profile's window and
# a windowless warm-up fetches only that window's bytes:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{BaseVectors('example:first100k');Stringify()}}"

# Ordinal-range based: clip to a caller-named window and warm exactly
# those records before the run starts:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{BaseVectors('example:default','[0..100k)');Stringify()}}"

# Same window, warmed on another thread while cycles proceed:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{BaseVectors('example:default','[0..100k)','background');Stringify()}}"

# Any facet by name (canonical or alias), including scalars:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{Facet('example:default','metadata_results');Stringify()}}"

# Variable-length records; the window warms through the offset index
# while indices stay absolute:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{VariableFacet('example:default','metadata_content','[0..10k)','background');Stringify()}}"
```

Prefetch modes on every mapper: `eager` (default; aliases `prebuffer`,
`true`), `background`, `none` (aliases `demand`, `false`). A binding
window is in the reader's own coordinates — after any profile window —
and is shifted to absolute records for the fetch, so the warmed bytes
are exactly the bytes the clipped reader exposes.

## Verifying that warming happened

- Plan again after a prefetch: `prefetchPlan(...)` over the same window
  must report `isResident()` and `bytesToFetch() == 0`.
- Count `Range` requests at the test server across reads inside a
  prefetched window: the count must not move.
- Assert the consent gate: a window against a facet with no ordinal
  mapping must throw under `REFUSE` (message carries the facet size)
  and fetch whole under `ALLOW`.
