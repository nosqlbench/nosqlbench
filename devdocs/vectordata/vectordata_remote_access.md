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

To watch the fetch overlap the run instead of preceding it, ask for the
background form — give it a range big enough and enough cycles to
outlast the download:

```bash
nb5 vectordata_demo dataset=mydataset:myprofile \
  prefetch=background records=100000 cycles=200
```

`prefetch` selects which prefetch call does the work: `load` (the
default) blocks on `prefetchCycles` before cycle 0, `background` hands
back a handle from `prefetchCyclesBackground` and meters it while the
run proceeds, and `none` leaves reads to demand-page. `records` sets how
many ordinals the demo spans, `cycles` how long it runs. The meter is
silent when there is nothing to fetch, so a second run against a warm
cache prints nothing — point `VECTORDATA_HOME` at an empty directory to
see it fetch again.

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

# The same, in the background: the handle comes back immediately and
# the fetch overlaps whatever runs next. Both background forms meter
# from the handle, so the download reports itself either way.
# {{= prefetchCyclesBackground("example:default", "base_vectors", 0, 1000000).plan().requests()}}
# {{warmup = prefetchBackground("example:default", "base_vectors", "[0..1M)")}}
# {{= warmup.join().rangesFetched()}}

# What a filtered ground truth can pay out, before anything is loaded —
# see "Reading recall on a filtered search" below:
# {{= groundTruthCoverage("example:default", "prefiltered_neighbor_indices")}}
# {{= requireAttainableRecall("example:default", "prefiltered_neighbor_indices", 0.95).ceiling()}}

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

# Opaque records — a slab of metadata — bound to parameters rather than
# rendered: one field typed as the record types it, a map of named
# fields in bind order, a form the facet declares, and a predicate's
# comparands typed from the metadata fields they constrain. Each read
# costs the page holding the record, never the file:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{RecordField('example:default','metadata_content','id');Stringify()}}"
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{RecordFields('example:default','metadata_content','id,tag');Stringify()}}"
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{RecordForm('example:default','metadata_content','row');Stringify()}}"
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{PredicateParams('example:default','metadata_predicates','metadata_content');Stringify()}}"

# The other contract — rendering, for a statement whose shape changes
# per cycle: a predicate as a CQL WHERE clause with its comparands
# inlined, or a metadata record in any vernacular:
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{RecordText('example:default','metadata_predicates','cql')}}"

# And the prepared form of the same: a fragment per predicate — the
# shape's WHERE clause with markers, the comparands as values, the
# fingerprint as the form key — which the cqld4 adapter prepares once
# per shape (see cql_vector_predicated for the whole workload):
java -jar nb5.jar run driver=stdout cycles=10 \
  "op={{PredicateClause('example:default','metadata_predicates','metadata_content')}}"
```

Prefetch modes on every vector mapper: `eager` (default; aliases
`prebuffer`, `true`), `background`, `none` (aliases `demand`, `false`).
The record mappers take none: a slab is paged, and a record fetches the
page it sits in, so they are demand-paged by construction. Warm a slab
ahead of a run with `prefetch("ds:profile", "metadata_content", "[0..100k)")`.

A binding window says **which records to warm**, and nothing else —
indices stay absolute record ordinals.
`BaseVectors('ds:profile','[50000..100000)')` warms records 50000
through 99999, and a run of `cycles=50000..100000` addresses exactly
those records. Reading outside the window is not an error; it just
demand-pages, as it would with no window. Ordinals mean the same thing
here, in `VariableFacet`, and in `prefetchCycles`. The window is
translated into dataset coordinates for the fetch, so the warmed bytes
are the bytes the reader will expose.

## Reading recall on a filtered search

A predicated search is verified against a **filtered** ground truth: for
each query, the exact top-k among the base vectors that satisfy that
query's predicate. That ground truth is only as deep as the predicate's
match set, and on a small profile the match set is often shallower than
k. Read this section before holding a system to a recall number from a
sized profile, because the number can be low for a reason that has
nothing to do with the system.

### How recall degrades at low-cardinality strata

A predicate of selectivity `s` over a slice of `N` base vectors matches
about `s x N` rows. The ground truth for that query holds the top-k
among those rows and no more, so when `s x N < k` the row is padded to k
with `-1` sentinels: entries that name no vector and that no search can
return. The strict recall measure divides the hits by k regardless, so
a query with `m < k` attainable neighbors can score at most `m / k` even
when the search returns every one of them.

Averaged over a query set, that gives the profile a **ceiling**: the
mean of `min(m, k) / k` across its queries, which is the recall@k a
perfect search would score. A published dataset carries a ladder of
selectivities meant for its full size; cut the same predicates to a
small slice and the ladder walks off the bottom of it. On a million-row
slice a selectivity of `1e-3` still yields about a thousand matches per
predicate, comfortably above k = 100; on a hundred-thousand-row slice
the same ladder's `1e-4` rung yields ten, `1e-5` yields one, and
everything below yields nothing. The mixed ladder on a 100k slice
therefore reads like this, measured against a local Cassandra:

| Profile | Queries with all k | Queries with none | Ceiling | Measured recall@100 |
|---|---|---|---|---|
| 100k, mixed ladder | 916 of 10,000 | 3,860 | 0.1428 | 0.1428 |
| 300k, uniform `1e-3` | 7,902 of 10,000 | 0 | 0.9881 | not run |
| 1m, uniform `1e-3` | 10,000 of 10,000 | 0 | 1.0000 | 0.9999 |

The first row is the cascade: a search that found **every** attainable
neighbor reads at 14 %, and a threshold written for the third row
fails it. The middle row shows why the arithmetic is only a first
guess: `1e-3 x 300k` is 300 matches on average, yet a fifth of its
queries still fall short of k, because a selectivity is a band the
generator aims for and the per-predicate counts spread around it. The
survey, not the multiplication, decides.

Three consequences follow, and the tooling encodes each.

### Survey before loading

The ground-truth facet alone says what the profile can pay out, and it
is a few megabytes against the hours a load can take. `groundTruthCoverage`
reads it and reports the query count, how many queries have all k
neighbors, how many have none, and the ceiling; `requireAttainableRecall`
refuses the run when the ceiling is below what you will accept, naming
the ceiling and what to do instead:

```bash
# Survey any profile from the CLI without loading a row:
java -jar nb5.jar run driver=stdout cycles=1 \
  "op={{= groundTruthCoverage('example:100k', 'prefiltered_neighbor_indices')}}"

# The predicated workload does this itself in its derived parameters and
# refuses below min_attainable (default 0.95) before its first phase:
java -jar nb5.jar cql_vector_predicated default.schema_ks default.schema default.rampup default.search_and_verify \
  driver=cqld4 dataset=example:100k hosts=... localdc=...
#   -> attainable recall@100 ceiling is 0.1428, below the required 0.95 ...

# A smoke run of the binding path on a padded profile, on purpose:
java -jar nb5.jar cql_vector_predicated ... dataset=example:100k min_attainable=0
```

### Report the attainable measure beside the strict one

`RelevancyFunctions.attainable_recall("recall_attainable", k)` leaves the
sentinels out of the denominator: of the neighbors that exist for the
query, the share found in the first k results. A query with no
attainable neighbor scores 1.0, since nothing could be found and
nothing was missed; how many such queries there are is the ground
truth's business and the survey has already said. The predicated
workload reports both, so the two together tell the story: on the 100k
row above, `recall` at the ceiling and `recall_attainable` at 1.0 means
the search recovered everything there was. On a complete profile the
two measures agree, and the strict one is the number to publish.

### Choose the profile by what it can pay out

Hold a system to strict recall only on a profile whose survey is at or
near 1.0. A uniform predicate set at a size where its selectivity
yields matches well above k is where to look, and the selector says
which candidates to survey:

```java
// every uniform set from a million rows up, size-ordered, each surveyed
TestDataGroup group = Catalog.of(CatalogSources.defaults()).openGroup("example");
for (String profile : group.select("family=uniform,base_count>=1m")) {
    VectorReader<?> gt = group.profile(profile).openFacet("prefiltered_neighbor_indices");
    System.out.println(GroundTruthCoverage.of(profile, gt));   // ... ceiling 1.0000 is the one to hold to
}
```

A single-profile surface such as `dataset("example:...")` takes a
selector too, but refuses one that names more than one profile; the set
surfaces are `TestDataGroup.select` and `Catalog.openProfiles`.

A mixed ladder on a slice far smaller than the size it was cut for is a
smoke test of the binding path, not a recall measurement; run it with
`min_attainable=0` and read `recall_attainable`. Since the ceiling is a
property of the ground truth and not of the store under test, it is
worth recording once per profile alongside the dataset, so a regression
suite compares each run against the ceiling its profile has rather than
against 1.0.

## Verifying that warming happened

- Plan again after a prefetch: `prefetchPlan(...)` over the same window
  must report `isResident()` and `bytesToFetch() == 0`.
- Count `Range` requests at the test server across reads inside a
  prefetched window: the count must not move.
- Assert the consent gate: a window against a facet with no ordinal
  mapping must throw under `REFUSE` (message carries the facet size)
  and fetch whole under `ALLOW`.
