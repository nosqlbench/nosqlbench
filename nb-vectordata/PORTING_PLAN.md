# `nb-vectordata` Programmatic Access Porting Plan

## Document control

| Field | Value |
|---|---|
| Status | Core implementation in progress; release gates remain |
| Overall implementation progress | 0 of 9 phase gates complete; baseline implementation is present |
| Current phase | Phase 6 — deterministic interoperability hardening |
| Last updated | 2026-08-24 |
| Target module | `io.nosqlbench:nb-vectordata:${revision}` |
| Target directory | `nb-vectordata/` |
| Normative Rust baseline | `vectordata` 1.7.1, commit `1249310078785dbb59444f1c9bac14247767c286` |
| Legacy Java reference | `datatools-vectordata` 0.1.25, commit `3f633142aa5c570c3921239c3d82591d3f036c26` |

Status values used throughout this document:

- `[ ]` Not started
- `[-]` In progress
- `[x]` Complete, with evidence recorded
- `[!]` Blocked; the blocker must be recorded in the progress log

An item is not complete merely because code has been written. It is complete only when its stated verification evidence exists.

## 1. Executive summary

`nb-vectordata` will be a root-level, independently consumable Java library implementing the programmatic read-access portion of the Rust `vectordata` contract. Its prescribed user path is:

```text
catalog sources
  -> catalog
    -> dataset group
      -> profile view
        -> typed facet reader
```

The library will support local and remotely hosted Rust-produced datasets without requiring callers to construct facet URLs, select transports, or manage cache files. Transport selection will be internal and deterministic:

```text
local path or file URI
  -> memory-mapped local storage

HTTP(S) or public S3 source
  -> published .mref available
       -> sparse, Merkle-verified cache
  -> no .mref, byte ranges available
       -> sparse, TLS-trusted chunk cache
  -> no byte-range support
       -> full transfer followed by local mapped access
```

This is a selective port. The legacy Java module contains many unrelated capabilities and must not be copied wholesale. Only code that directly supports the Rust programmatic access contract is eligible for reuse.

## 2. Scope boundary

### 2.1 In scope

- Catalog configuration and discovery.
- Loading canonical `catalog.json` and `catalog.yaml` documents.
- Loading `dataset.yaml` from local paths and HTTP(S) locations.
- `knn_entries.yaml` fallback for jvector-compatible catalogs.
- Dataset attributes, named profiles, facet manifests, profile inheritance, partition profiles, sized-profile windows, and facet aliases.
- Typed access to scalar, uniform-vector, and variable-vector files.
- Local memory-mapped reads.
- Remote HTTP(S) reads, public `s3://` normalization, redirects, bearer authentication, retries, and byte-range probing.
- `.mref` parsing and per-chunk SHA-256 verification.
- Resumable cache state required for remote access.
- Strict prebuffer operations, progress reporting, cache inspection, and promotion to mapped local reads.
- Thread-safe concurrent access and source-level sharing.
- Migration of the NoSQLBench `virtdata-lib-vectors` consumers from `datatools-vectordata`.
- Documentation and tests sufficient for external Maven consumers.

### 2.2 Explicitly out of scope

- A `vectordata` CLI, shell completion, or TUI.
- Dataset generation, import, transformation, derivation, analysis, or publishing.
- KNN computation or verification engines.
- Predicate evaluation.
- MNode, PNode, ANode, slab dialect, SQL, or CQL codecs beyond exposing a declared facet as raw content where appropriate.
- HDF5, NumPy, or Parquet import pipelines.
- Dataset push, backup, restore, endpoint administration, or token issuance.
- General-purpose Merkle creation commands or scheduling frameworks.
- Micrometer, Prometheus, JMH, scheduler simulations, or performance dashboards.
- The legacy Java event sink, CLI, download facade, layout-v1, metadata backend, and predicate packages.
- Source compatibility with every class ever published by `datatools-vectordata`.

### 2.3 Legacy reuse rule

A legacy class or algorithm may be ported only when all of the following are true:

1. It directly implements an in-scope access behavior.
2. Its behavior matches the frozen Rust compatibility contract.
3. Its dependencies can be removed or are also in scope.
4. It has tests proving the behavior independently of the old module.
5. Its API naming does not force legacy concepts into the new public surface.

Likely reuse candidates are limited to Merkle geometry/serialization ideas, xvec record arithmetic, and selected test fixtures. Legacy discovery, downloader, profile, and vector-view types should be treated as behavioral references rather than copied APIs.

## 3. Source-of-truth policy

### 3.1 Reference precedence

When references disagree, use this order:

1. An explicit cross-language compatibility decision recorded in this document.
2. Observable behavior pinned by a Rust/Java conformance test.
3. The Rust source at the pinned compatibility commit.
4. Rust API/system-reference documentation at that commit.
5. Legacy Java behavior, only when it agrees with the higher-priority sources.

No behavior should be inferred solely from stale examples or old artifact APIs.

### 3.2 Known contract discrepancies to resolve in Phase 0

| Topic | Current discrepancy | Required resolution |
|---|---|---|
| Version examples | Rust docs still show dependency version `0.25`; the linked crate is 1.7.1 | Pin compatibility to the source commit, not the example version |
| Remote source without `.mref` | Older prose says direct HTTP and no cache; current source uses a sparse chunk cache and later mmap promotion | Adopt current source behavior |
| Cache defaults | Some docs say no fallback; current source supports `$VECTORDATA_HOME/cache` and conditional home-cache bootstrap | Define and test the exact Java resolution order |
| Custom facets | Public prose promises custom facet discovery; the legacy Rust profile model retains primarily fixed fields | Preserve unknown profile facet entries and expose them through the generic API |
| Typed floats | The public element taxonomy includes f16/f32/f64; the current Rust integer conversion path does not fully implement them | Implement the advertised typed behavior and record the intentional divergence until Rust is corrected |
| Windowed prebuffer | A strict full-residency statement conflicts with range-only prebuffering for sized views | Define success as full residency of the declared view window; do not claim the whole backing file is mapped |
| Filtered facet labels | Some comments transpose E and F; canonical tables define prefilter as F and postfilter as E | Use canonical YAML keys and semantics, with regression tests |
| `.ivec` naming | Singular legacy forms have inconsistent uniform/variable treatment in places | Freeze an extension/shape table and reject ambiguous malformed data explicitly |

### 3.3 Compatibility artifacts

Phase 0 must establish these repository-owned artifacts:

- `nb-vectordata/RUST_COMPATIBILITY.md`
- `nb-vectordata/src/test/resources/rust-v1/fixture-manifest.yaml`
- Rust-generated catalogs, manifests, facets, offset indexes, and `.mref` files under the same fixture directory
- Expected values, dimensions, counts, SHA-256 hashes, and producer commit in the fixture manifest
- An optional fixture-regeneration procedure that uses the pinned Rust crate

Normal Maven tests must consume checked-in fixtures and must not require Cargo, the `links/` directory, or internet access.

## 4. Target deliverables

### 4.1 Repository deliverables

```text
nb-vectordata/
├── pom.xml
├── README.md
├── PORTING_PLAN.md
├── RUST_COMPATIBILITY.md
├── src/main/java/io/nosqlbench/vectordata/
│   ├── api and root entry points
│   ├── catalog/
│   ├── dataset/
│   ├── io/
│   ├── settings/
│   └── internal/
│       ├── cache/
│       ├── merkle/
│       ├── storage/
│       └── transport/
└── src/test/
    ├── java/
    └── resources/rust-v1/
```

The exact package split may be refined during the API checkpoint, but transport and cache implementation types must remain non-public.

### 4.2 Maven deliverable

- Coordinates: `io.nosqlbench:nb-vectordata:${revision}`
- Packaging: JAR
- Stable automatic module name: `io.nosqlbench.vectordata`
- Sources and Javadocs attached by inherited build policy
- Apache 2.0 licensing and RAT compliance
- No dependency on `nb-virtdata`, `nb-engine`, adapters, or legacy `nbdatatools`
- Proposed bytecode target: Java 17, built and tested under the repository-required JDK 25

Proposed minimal runtime dependencies:

- SnakeYAML Engine for YAML and JSON-compatible mapping input
- OkHttp for pooled HTTP, TLS, redirects, headers, and range requests
- No mandatory logging implementation; use `System.Logger`

Adding another runtime dependency requires a recorded justification in the decision log.

## 5. Proposed public API

### 5.1 Primary entry path

```java
CatalogSources sources = CatalogSources.defaults();
Catalog catalog = Catalog.of(sources);

TestDataView view = catalog.openProfile("my-dataset", "default");
VectorReader<float[]> base = view.baseVectors();

System.out.printf("count=%d dim=%d%n", base.count(), base.dimension());
float[] vector = base.get(42);
```

Direct file access remains available for tests, diagnostics, and applications without catalogs:

```java
VectorReader<float[]> base = VectorReaders.openF32("base_vectors.fvecs");
VvecReader<int[]> results = VvecReaders.openI32("metadata_results.ivvecs");
```

### 5.2 Public type inventory

| Area | Proposed types | Notes |
|---|---|---|
| Entry points | `VectorData`, `CatalogSources`, `Catalog` | Name-based access is primary |
| Catalog model | `CatalogEntry`, `CatalogLayout` | Immutable values |
| Dataset model | `TestDataGroup`, `TestDataView`, `FacetConfig`, `FacetDescriptor` | Preserve custom facets |
| Uniform data | `VectorReader<A>`, `VectorReaders` | `A` is a primitive array type |
| Variable data | `VvecReader<A>`, `VvecReaders` | Per-record dimensions |
| Typed data | `TypedReader`, `TypedReaders`, `ElementType` | Exact unsigned semantics required |
| Cache | `FacetStorage`, `CacheStats`, `PrebufferProgress`, `AccessMode` | Opaque storage handle |
| Configuration | `VectorDataSettings`, `CredentialsProvider` | Immutable per-client overrides |
| Errors | `VectorDataException` and focused subclasses | Never terminate the process |

### 5.3 Reader contracts

Uniform reader:

```java
public interface VectorReader<A> extends AutoCloseable {
    long count();
    int dimension();
    A get(long ordinal) throws VectorDataException;
    void readInto(long ordinal, A target, int targetOffset) throws VectorDataException;
    boolean isComplete();
    void prebuffer(ProgressListener listener) throws VectorDataException;
}
```

Variable reader:

```java
public interface VvecReader<A> extends AutoCloseable {
    long count();
    int dimensionAt(long ordinal) throws VectorDataException;
    A get(long ordinal) throws VectorDataException;
    void readInto(long ordinal, A target, int targetOffset) throws VectorDataException;
    boolean isComplete();
    void prebuffer(ProgressListener listener) throws VectorDataException;
}
```

Final method names and close semantics are frozen at Checkpoint 1.2. Primitive arrays are used to avoid boxing in vector workloads. Counts and ordinals are `long`; implementations must not silently truncate to `int`.

### 5.4 Standard profile accessors

`TestDataView` must provide dedicated accessors for:

- `baseVectors()` -> `VectorReader<float[]>`
- `queryVectors()` -> `VectorReader<float[]>`
- `neighborIndices()` -> `VectorReader<int[]>`
- `neighborDistances()` -> `VectorReader<float[]>`
- `prefilteredNeighborIndices()` -> `VectorReader<int[]>`
- `prefilteredNeighborDistances()` -> `VectorReader<float[]>`
- `postfilteredNeighborIndices()` -> `VectorReader<int[]>`
- `postfilteredNeighborDistances()` -> `VectorReader<float[]>`
- `metadataResults()` -> `VvecReader<int[]>`
- `facetManifest()` -> immutable map of all standard and custom facets
- `facetSource(name)` and `facetElementType(name)`
- `openTypedFacet(name, targetType)`
- `openFacetStorage(name)`
- `prebufferAll(listener)`

Missing optional facets return `Optional` or throw a focused `MissingFacetException`; the choice must be consistent and decided at Checkpoint 1.2.

### 5.5 Element representation

| Wire type | Native Java bulk representation | Scalar exact representation |
|---|---|---|
| `u8` | `byte[]` | `short` or wider |
| `i8` | `byte[]` | `byte` |
| `u16` | `short[]` raw bits | `int` or wider |
| `i16` | `short[]` | `short` |
| `f16` | `short[]` raw IEEE-754 binary16 | `float` |
| `u32` | `int[]` raw bits | `long` or wider |
| `i32` | `int[]` | `int` |
| `f32` | `float[]` | `float` |
| `u64` | `long[]` raw bits | `BigInteger` |
| `i64` | `long[]` | `long` |
| `f64` | `double[]` | `double` |

`TypedReader` must reject narrowing at open time and check same-width cross-sign conversions per value. Native bulk readers retain raw bit patterns; typed scalar conversion preserves mathematical values.

## 6. Internal architecture and invariants

### 6.1 Configuration and source resolution

Responsibilities:

- Parse named or list-form `catalogs.yaml` entries accepted by the pinned Rust version.
- Load required catalogs strictly and optional catalogs leniently.
- Normalize catalog file versus directory locations.
- Resolve canonical catalog entry paths against their catalog location.
- Load `dataset.yaml`, then fall back to `knn_entries.yaml` where specified.
- Resolve every facet independently so a profile may mix local and remote sources.
- Pass through absolute URI schemes instead of incorrectly joining them to a base URL.
- Normalize `file://` inputs to local filesystem paths.
- Normalize public `s3://bucket/key` locations to regional HTTPS endpoints.
- Preserve all unknown profile keys as custom facet descriptors.

### 6.2 Dataset/profile model

Required semantics:

- Canonical attributes and arbitrary extra attributes are retained.
- Profiles are named and returned in deterministic size-aware order.
- `default` profile inheritance is applied to shared facets.
- Sized profiles window inherited base-vector and per-base metadata facets to `[0..base_count)`.
- Query facets are inherited without base-count clipping.
- Per-profile ground-truth facets are not inherited.
- `partition: true` profiles are self-contained and never inherit base data.
- Simple string and detailed `{source, window}` facet forms are accepted.
- Inline source window syntax is parsed explicitly.
- Multiple disjoint windows are rejected until the public reader contract supports them; silently using only the first is forbidden.

Required aliases:

- `metadata_results` is canonical.
- `metadata_indices` and `predicate_results` map to `metadata_results`.
- `prefiltered_neighbor_*` is canonical for prefilter ground truth.
- Legacy `filtered_neighbor_*` maps to prefilter ground truth.
- `postfiltered_neighbor_*` is canonical for postfilter ground truth.

### 6.3 Shape layer

All on-disk numeric fields are little-endian except the Merkle footer described below.

Scalar layout:

```text
[element 0][element 1]...[element N-1]
```

Uniform-vector layout:

```text
record = [dimension: signed i32 LE][dimension elements]
```

Variable-vector layout:

```text
record = [dimension: signed i32 LE][dimension elements]
```

Variable records require an offset index:

```text
IDXFOR__<data-filename>.i32
IDXFOR__<data-filename>.i64
```

Shape validation must reject:

- Negative or nonsensical dimensions.
- Files shorter than a required header.
- Scalar files whose length is not divisible by element width.
- Uniform files whose size is not divisible by the computed stride.
- Inconsistent per-record dimensions when encountered.
- Offset indexes with invalid width, decreasing offsets, out-of-bounds offsets, or a count inconsistent with the data scan.
- Ordinals outside `[0, count)`.
- Element-type/extension mismatches.

Remote vvec behavior:

1. Try the published sibling index.
2. If absent, scan the remote data through the cache-backed storage.
3. Persist the rebuilt index beside the local cached data.
4. Reuse it on subsequent opens.

### 6.4 Storage layer

Define a package-private byte storage abstraction. Public readers depend on the abstraction, never on a transport implementation.

Required implementations:

- Local mapped storage.
- Merkle-verified cached remote storage.
- Non-Merkle chunk-cached remote storage.
- Full-transfer remote storage for non-range servers.

Required invariants:

- A successful in-bounds read returns exactly the requested bytes.
- Chunk validity is recorded only after bytes are verified and durably written.
- Failed reads never mark cache state valid.
- Completed remote storage is promoted to mapped local reads.
- Readers opened before completion observe promotion without being recreated.
- Remote prebuffer success means the full declared view is locally readable without another network call.
- A full-file prebuffer additionally guarantees mapped access to the whole backing file.
- Window-only prebuffer does not claim that unrelated backing-file ranges are complete.

### 6.5 Shared-source coordination

Within one JVM:

- Canonicalize local paths and normalized remote URLs into source identities.
- Maintain weakly referenced shared storage instances.
- Deduplicate concurrent opens with a per-source in-flight future.
- Deduplicate concurrent chunk downloads.
- Share HTTP connection pools.
- Make storage/readers safe for concurrent reads.

Across JVMs/processes:

- Use atomic sidecar replacement.
- Use file locks or another proven coordination protocol for mutable cache state.
- Revalidate on-disk state after acquiring ownership.
- Never trust a validity bitmap without verifying its geometry and matching origin.

### 6.6 Merkle wire compatibility

`.mref` compatibility requirements:

```text
[node_count * 32-byte SHA-256 hashes][41-byte v1 or 45-byte v2 footer]
```

Footer numeric fields are big-endian. The final byte is the footer-length marker. V2 includes a big-endian bitset-size field.

`.mrkl` cache state requirements:

```text
[hashes][validity words][v2 footer]
```

Validity words are little-endian 64-bit words compatible with Java `BitSet` semantics. Internal Merkle nodes hash the concatenation of left and right child hashes. Unused leaves use SHA-256 of the empty byte sequence.

Only the logic required to parse references, verify chunks, and persist resumable access state belongs in this module.

### 6.7 Cache layout and settings

Catalog-opened datasets use a dataset-keyed natural layout:

```text
<cache-root>/<dataset-name>/
├── origin.json
├── profiles/...
├── <facet-file>
├── <facet-file>.mrkl
└── IDXFOR__<facet-file>.i32|i64
```

Cache resolution order to freeze and test:

1. Explicit immutable client setting.
2. `$VECTORDATA_HOME/settings.yaml`.
3. `$VECTORDATA_HOME/cache` when the isolated home is set but no cache is declared.
4. `~/.config/vectordata/settings.yaml`.
5. The pinned Rust-compatible conditional home-cache bootstrap behavior, if retained by the Phase 0 decision.
6. Otherwise, a focused configuration exception with actionable remediation.

`origin.json` prevents two catalogs from silently using the same dataset cache directory for different origins. Out-of-home facet basename collisions inside one profile must also be rejected.

### 6.8 HTTP and authentication

Required HTTP behavior:

- Shared connection pool.
- Bounded connect, request, and read timeouts.
- Redirect handling with a finite limit.
- HEAD probing for content length and `Accept-Ranges`.
- Correct inclusive HTTP Range header construction.
- Exact `Content-Range`/body length validation.
- Retry only transient failures with exponential backoff and jitter.
- Full-transfer fallback for servers without Range support.
- S3 wrong-region correction when the response supplies `x-amz-bucket-region`.
- Clear errors for 401/403, missing content length, invalid ranges, truncated responses, and retry exhaustion.

Read-token resolution order:

1. Explicit per-client credentials provider.
2. `VECTORDATA_TOKEN`.
3. Stored credential selected by the longest segment-aligned URL-prefix match.

Credentials must never appear in logs, exception messages, cache-origin files, test output, or generated URLs.

## 7. Test and verification strategy

### 7.1 Test tiers

| Tier | Naming/tagging | Network | Purpose |
|---|---|---|---|
| Unit | `*Test`, `@Tag("unit")` | None | Pure parsing, format, geometry, conversion, and state logic |
| Integration | `*IntegrationTest` | Embedded loopback server only | End-to-end storage and catalog behavior |
| Rust interoperability | Unit/integration fixture tests | Embedded loopback server | Read artifacts emitted by pinned Rust code |
| Live canary | Gated profile | External | Scheduled/release validation against a hosted Rust-published dataset |
| Performance | Optional JMH or focused harness, not a release gate initially | None/loopback | Detect major regressions after correctness is established |

### 7.2 Source/shape matrix

Every required cell must have at least one deterministic test:

| Source mode | Scalar | Uniform xvec | Variable vvec |
|---|---:|---:|---:|
| Local file | Required | Required | Required |
| `file://` URI | Required | Required | Required |
| HTTP with `.mref` | Required | Required | Required |
| HTTP Range without `.mref` | Required | Required | Required |
| HTTP without Range | Required | Required | Required |
| Mixed sources in one profile | Required | Required | Required |

### 7.3 Element-type matrix

For every element type:

- Open the canonical extension.
- Open every supported alias.
- Read first, middle, and last values/records.
- Verify little-endian decoding.
- Verify native bulk representation.
- Verify widening.
- Verify narrowing rejection.
- Verify cross-sign success and overflow.
- Verify empty-file behavior where legal.
- Verify out-of-range ordinals.

### 7.4 Catalog/profile tests

- Required and optional catalogs.
- List-form and named catalog configuration.
- Local catalog directory and explicit catalog file.
- HTTP catalog directory and explicit catalog file.
- Canonical layout-embedded entries.
- Case-insensitive exact match.
- Duplicate exact-name rejection.
- Glob and regular-expression matching.
- Relative catalog path resolution.
- Dataset directory and direct `dataset.yaml` loading.
- `knn_entries.yaml` fallback and multiple dataset/profile keys.
- Attributes and unknown attribute retention.
- Custom facet retention.
- Default inheritance.
- Sized profile clipping.
- Partition isolation.
- Detailed and inline windows.
- Alias resolution.
- Mixed local and absolute remote facets.
- Cache relpath and origin collision detection.

### 7.5 Cache and transport fault tests

- Lazy fetch downloads only covering chunks.
- Repeated read performs no second download.
- Interrupted download resumes from persisted state.
- Reopen after JVM-level storage release uses the existing cache.
- `.mref` v1 and v2 parsing.
- Hash mismatch rejects bytes and leaves the chunk invalid.
- Invalid `.mref` footer and geometry are rejected.
- Corrupt `.mrkl` state is rejected or safely rebuilt.
- No `.mref` uses TLS-trusted chunk state.
- No Range support triggers exactly one full transfer.
- Incorrect Range body length is rejected.
- Transient failures retry within policy.
- Permanent 4xx responses are not retried as transient errors.
- Redirect loops fail clearly.
- Bearer token reaches catalog, `.mref`, index, and data requests.
- Token text is absent from all diagnostics.
- Prebuffer progress is monotonic and finishes at the declared total.
- Insufficient cache capacity fails before bulk download where determinable.

### 7.6 Concurrency tests

- Concurrent opens of one source perform one initialization.
- Concurrent reads of one missing chunk perform one fetch.
- Concurrent reads of different chunks make progress in parallel.
- Completion state is not visible before durable writes.
- Multiple readers observe promotion.
- Multiple profiles sharing one source do not corrupt cache state.
- Multiple JVM/process simulation coordinates state safely.
- Closing one reader does not invalidate other shared readers.

### 7.7 Rust fixture requirements

The checked-in Rust-produced fixture must contain:

- A catalog with at least two profiles.
- A canonical `dataset.yaml`.
- A compatible `knn_entries.yaml` example.
- Standard B, Q, G, D, M, P, R, F, and E facet declarations where practical.
- At least one custom facet.
- At least one sized profile with a window.
- Scalar integer and floating-point files.
- Uniform f32 and i32 vector files.
- Variable i32 records.
- Both i32 and i64 offset-index encodings; the i64 case may be a synthetic small fixture using i64 offsets.
- V1 and V2 `.mref` examples.
- A non-power-of-two Merkle leaf count.
- Expected values and file digests.
- Producer crate version and Git commit.

### 7.8 Live canary

The live test is enabled only when these are provided:

```text
NB_VECTORDATA_TEST_CATALOG
NB_VECTORDATA_TEST_DATASET
NB_VECTORDATA_TEST_PROFILE
```

The canary must use a deliberately small, stable Rust-published dataset. It should verify catalog resolution, profile open, representative facet reads, `.mref` verification, prebuffer, cache reopen, and value equality. It is a scheduled/release gate, not a normal pull-request gate.

### 7.9 Standard verification commands

During implementation:

```bash
mvn -q -pl nb-vectordata -am test
mvn -q -pl nb-vectordata -am verify
```

During consumer migration:

```bash
mvn -q -pl nb-virtdata/virtdata-lib-vectors -am verify
```

Before completion:

```bash
mvn -q verify
rg -n "datatools-vectordata|io\.nosqlbench\.nbdatatools" \
  --glob '!links/**' --glob '!**/target/**'
```

The exact commands and results must be recorded in the checkpoint evidence.

## 8. Phased implementation plan and checkpoints

### Progress overview

| Phase | Weight | Status | Gate | Evidence location |
|---|---:|---|---|---|
| 0. Compatibility contract freeze | 10% | In progress | C0 | Progress log |
| 1. Module scaffold and public API | 10% | Implemented; review pending | C1 | Progress log |
| 2. Catalog and dataset model | 15% | Implemented baseline; hardening pending | C2 | Progress log |
| 3. Local typed readers | 15% | Implemented baseline; matrix pending | C3 | Progress log |
| 4. Remote storage and cache | 20% | Implemented baseline; hardening pending | C4 | Progress log |
| 5. View integration and prebuffer | 10% | Implemented baseline; strict-window work pending | C5 | Progress log |
| 6. Rust interoperability and hardening | 10% | In progress | C6 | Progress log |
| 7. NoSQLBench consumer migration | 5% | Implemented; mapper tests pending | C7 | Progress log |
| 8. Release readiness | 5% | Not started | C8 | Progress log |

Percent complete is the sum of weights for gates that have passed. Partially completed phases may be described in the progress log but do not contribute to the gate-based percentage.

### Phase 0 — Compatibility contract freeze

Objective: turn the pinned Rust behavior into an explicit, testable Java-port contract before public implementation choices become expensive to change.

Tasks:

- [ ] Create `RUST_COMPATIBILITY.md` with source commit, supported formats, API mapping, and deviations.
- [ ] Resolve every discrepancy in Section 3.2.
- [ ] Freeze the extension/shape/element table.
- [ ] Freeze cache resolution order.
- [ ] Freeze canonical and legacy facet aliases.
- [ ] Freeze prebuffer semantics for full files and windows.
- [ ] Decide custom-facet behavior.
- [ ] Decide typed f16/f32/f64 behavior.
- [ ] Decide the minimum Java bytecode version.
- [ ] Decide public missing-facet and checked-exception conventions.
- [ ] Generate the initial Rust fixture with the pinned crate.
- [ ] Check in the fixture manifest and expected values.
- [ ] Document how to regenerate the fixture without relying on `links/`.

Checkpoint C0 — Contract frozen:

- [ ] Every known discrepancy has a recorded decision.
- [ ] Fixture producer version and Git SHA are recorded.
- [ ] Fixture hashes reproduce.
- [ ] A reviewer can determine expected behavior without reading legacy Java source.

Evidence required:

- Link or commit containing `RUST_COMPATIBILITY.md`.
- Fixture manifest checksum.
- Decision-log entries for all Section 3.2 topics.

### Phase 1 — Module scaffold and public API

Objective: establish an independently buildable Maven artifact and freeze the small public surface before internal implementation expands.

Tasks:

- [ ] Add `nb-vectordata` to the root reactor before `nb-virtdata`.
- [ ] Create `nb-vectordata/pom.xml` inheriting `mvn-defaults`.
- [ ] Configure Java release and automatic module name.
- [ ] Add only approved dependencies.
- [ ] Add README quick start and scope statement.
- [ ] Add root exception hierarchy.
- [ ] Add immutable `ElementType`, `AccessMode`, facet, cache, and progress value types.
- [ ] Add reader interfaces and direct-open factories.
- [ ] Add catalog/group/view interfaces or final classes.
- [ ] Keep implementation packages non-exported/non-public.
- [ ] Add API compilation tests demonstrating intended Java usage.
- [ ] Add Javadocs for all public types and methods.

Checkpoint C1 — API/build skeleton accepted:

- [ ] `mvn -q -pl nb-vectordata -am test` passes.
- [ ] Public API review is complete.
- [ ] No public type exposes OkHttp, SnakeYAML, cache internals, or legacy APIs.
- [ ] Dependency tree contains no `nbdatatools` artifact.
- [ ] Sources/Javadocs/RAT build steps succeed.

Evidence required:

- Maven command and result.
- Public package/type inventory.
- Dependency tree summary.
- Decision-log entry freezing API conventions.

### Phase 2 — Catalog and dataset model

Objective: parse and resolve every supported catalog and profile shape without opening facet data.

Tasks:

- [ ] Implement catalog source configuration and tilde/environment resolution.
- [ ] Implement required versus optional sources.
- [ ] Implement local and HTTP catalog loading.
- [ ] Parse canonical layout-embedded catalog entries.
- [ ] Resolve entry paths against catalog origins.
- [ ] Implement exact, glob, and regex matching.
- [ ] Reject ambiguous duplicate exact matches.
- [ ] Parse `dataset.yaml` from a directory or direct file/URL.
- [ ] Implement `knn_entries.yaml` fallback.
- [ ] Preserve attributes, variables, profiles, and custom facets.
- [ ] Implement aliases and deterministic profile ordering.
- [ ] Implement default inheritance and partition isolation.
- [ ] Implement simple, detailed, and inline windows.
- [ ] Resolve mixed local and remote facet sources independently.
- [ ] Add parsing and resolution tests using Rust fixtures.

Checkpoint C2 — Catalog-to-facet resolution complete:

- [ ] Every catalog/profile test in Section 7.4 passes.
- [ ] The Rust fixture can be resolved to correct absolute facet sources without data reads.
- [ ] Unknown/custom facets survive round-trip parsing into the manifest.
- [ ] No parser path terminates the JVM or prints directly as error handling.

Evidence required:

- Test count and command result.
- Resolved-source snapshot for the Rust fixture.
- List of supported manifest forms and aliases.

### Phase 3 — Local typed readers

Objective: provide validated, thread-safe local access to scalar, uniform, and variable data for every supported element type.

Tasks:

- [ ] Implement mapped local byte storage.
- [ ] Implement canonical path and `file://` normalization.
- [ ] Implement scalar geometry and typed reads.
- [ ] Implement uniform xvec geometry and typed reads.
- [ ] Validate record dimensions and strides.
- [ ] Implement variable vvec scans and offset-index use.
- [ ] Build and persist missing local offset indexes.
- [ ] Implement native bulk and `readInto` methods.
- [ ] Implement widening, narrowing rejection, and cross-sign checks.
- [ ] Implement exact unsigned scalar representations.
- [ ] Implement f16 conversion and float/double support.
- [ ] Implement bounds and malformed-file errors.
- [ ] Make close and shared-mapping behavior explicit and tested.

Checkpoint C3 — Local format matrix complete:

- [ ] Every local/file-URI cell in Sections 7.2 and 7.3 passes.
- [ ] Java reads all local Rust fixture facets with expected values.
- [ ] Malformed files fail with focused errors.
- [ ] Counts remain correct beyond signed `int` arithmetic in synthetic geometry tests.

Evidence required:

- Element/shape matrix with pass status.
- Rust fixture value comparison report.
- Test command and result.

### Phase 4 — Remote storage and cache

Objective: make the same shape readers work transparently over all required remote access modes.

Tasks:

- [ ] Implement shared OkHttp clients and bounded timeouts.
- [ ] Implement source normalization, including public S3.
- [ ] Implement content-length and Range probing.
- [ ] Implement exact range fetch validation.
- [ ] Implement retry policy and transient-error classification.
- [ ] Implement cache settings resolution.
- [ ] Implement dataset-keyed natural cache layout.
- [ ] Implement `origin.json` collision detection.
- [ ] Implement `.mref` v1/v2 parsing.
- [ ] Implement Merkle chunk verification.
- [ ] Implement atomic `.mrkl` state persistence.
- [ ] Implement non-Merkle sparse chunk caching.
- [ ] Implement full-transfer fallback.
- [ ] Implement cache restart/resumption.
- [ ] Implement same-JVM source registry and open/fetch deduplication.
- [ ] Implement cross-process cache coordination.
- [ ] Implement bearer credential resolution without secret leakage.
- [ ] Implement mmap promotion and lazy observation by existing readers.
- [ ] Implement remote offset-index fetch/rebuild/persistence.

Checkpoint C4 — Remote transport matrix complete:

- [ ] Every remote source/shape cell in Section 7.2 passes.
- [ ] `.mref` hash mismatches are rejected and not recorded valid.
- [ ] Interrupted downloads resume without refetching valid chunks.
- [ ] No-Range servers perform one full transfer and then local reads.
- [ ] Concurrency tests show one open and one fetch per source/chunk.
- [ ] Authentication applies to catalog, `.mref`, index, and data requests.
- [ ] No secret appears in captured logs or exceptions.

Evidence required:

- HTTP request-count assertions.
- Cache-state before/after snapshots.
- Concurrency/fault-test result summary.
- Maven verification result.

### Phase 5 — View integration and strict prebuffer

Objective: complete the prescribed catalog-to-reader API and make profile-wide residency behavior observable and reliable.

Tasks:

- [ ] Wire standard `TestDataView` accessors to shape readers.
- [ ] Implement generic custom-facet access.
- [ ] Implement facet type and source interrogation.
- [ ] Implement opaque `FacetStorage` handles.
- [ ] Implement per-facet cache stats.
- [ ] Implement full-facet prebuffer with progress.
- [ ] Implement window-aware prebuffer.
- [ ] Implement profile-wide prebuffer and error propagation.
- [ ] Implement group-wide prebuffer and large-download advisory.
- [ ] Perform capacity checks before bulk downloads where possible.
- [ ] Ensure a successful prebuffer never leaves required view bytes remote.
- [ ] Ensure readers opened before prebuffer observe completion/promotion.
- [ ] Document mixed-source profile behavior.

Checkpoint C5 — Prescribed programmatic path complete:

- [ ] The README quick-start example runs against local and embedded HTTP Rust fixtures.
- [ ] Standard, custom, typed, and variable facets are accessible from a catalog-opened view.
- [ ] Profile-wide failures propagate with facet context.
- [ ] Progress is monotonic and terminates correctly.
- [ ] Windowed profiles download only their declared byte ranges where format geometry permits.

Evidence required:

- End-to-end sample output.
- Progress callback trace for representative profiles.
- Request-byte comparison for full versus windowed prebuffer.

### Phase 6 — Rust interoperability and hardening

Objective: prove the Java implementation against artifacts created by Rust and make future drift visible.

Tasks:

- [ ] Complete the checked-in Rust fixture matrix.
- [ ] Add Java tests consuming every fixture artifact.
- [ ] Add optional fixture regeneration using the pinned Rust crate.
- [ ] Compare regenerated files and manifests byte-for-byte where formats are deterministic.
- [ ] Add fault injection and adversarial format tests.
- [ ] Complete concurrency and process-coordination tests.
- [ ] Add live-canary test profile.
- [ ] Establish scheduled/release execution for the canary.
- [ ] Add compatibility report generation or a maintained matrix.
- [ ] Review cache disk usage, allocation behavior, and cleanup semantics.
- [ ] Benchmark representative local, lazy remote, and prebuffered paths.
- [ ] Resolve material regressions before migration.

Checkpoint C6 — Interoperability certified:

- [ ] All deterministic Rust fixture tests pass.
- [ ] Fixture regeneration is documented and reproducible.
- [ ] Live canary passes against a Rust-hosted dataset.
- [ ] No known correctness issue is waived without a documented compatibility decision.
- [ ] Representative prebuffered performance is suitable for NoSQLBench cycle access.

Evidence required:

- Compatibility matrix marked pass/fail with zero unexplained failures.
- Live canary URL redacted as appropriate, dataset/profile, timestamp, and result.
- Benchmark summary and environment.

### Phase 7 — NoSQLBench consumer migration

Objective: remove the external legacy artifact from current NoSQLBench vector access paths.

Known current consumers:

- `nb-virtdata/virtdata-lib-vectors/pom.xml`
- `io.nosqlbench.virtdata.lib.vectors.vectordata.CoreVectors`
- `BaseVectors`, `QueryVectors`, `NeighborIndices`, `NeighborDistances`
- `io.nosqlbench.exprs.lib.vectors.VectorDataExprs`

Tasks:

- [ ] Replace `datatools-vectordata:0.1.25` with reactor dependency `nb-vectordata:${revision}`.
- [ ] Rewrite `CoreVectors` against the new catalog/view/reader API.
- [ ] Remove `ProgressIndicatingFuture` and other `nbdatatools` imports.
- [ ] Preserve public VirtData mapper constructors and behavior.
- [ ] Make prebuffer failure synchronous and visible during mapper construction.
- [ ] Migrate vector expression functions.
- [ ] Replace the disabled hosted-dataset test with deterministic fixture-backed coverage.
- [ ] Confirm transitive consumers compile.
- [ ] Inspect `virtdata-lib-hdf5` predicate imports separately; do not pull predicate codecs into `nb-vectordata` merely to satisfy unrelated legacy usage.
- [ ] Remove obsolete repository declarations used only by the legacy artifact.

Checkpoint C7 — Legacy artifact detached:

- [ ] `mvn -q -pl nb-virtdata/virtdata-lib-vectors -am verify` passes.
- [ ] Repository search finds no production dependency on `datatools-vectordata`.
- [ ] Repository search finds no unintended `nbdatatools` import in the migrated path.
- [ ] VirtData mapper tests cover lazy and prebuffered access.
- [ ] Existing mapper and expression names remain usable.

Evidence required:

- Search output.
- Maven result.
- Before/after dependency tree.
- Migration notes for downstream callers.

### Phase 8 — Release readiness

Objective: make the module safe to publish and maintain as a standalone access implementation.

Tasks:

- [ ] Complete README usage, configuration, cache, security, and troubleshooting sections.
- [ ] Complete all public Javadocs.
- [ ] Document supported Rust version and compatibility policy.
- [ ] Document thread safety and lifecycle semantics.
- [ ] Document cache cleanup and corruption recovery.
- [ ] Verify license provenance for selectively ported code.
- [ ] Verify RAT, source JAR, Javadoc JAR, and release metadata.
- [ ] Run dependency convergence and vulnerability review.
- [ ] Run full reactor verification.
- [ ] Run live canary as a release gate.
- [ ] Record known limitations and future compatibility work.
- [ ] Update this document to final status.

Checkpoint C8 — Ready for publication:

- [ ] All prior gates remain green.
- [ ] `mvn -q verify` passes for the full reactor.
- [ ] Artifact can be consumed from a clean standalone Maven sample.
- [ ] Sources and Javadocs are attached and valid.
- [ ] No excluded capability leaked into the module.
- [ ] Compatibility and migration documentation are complete.

Evidence required:

- Full reactor result.
- Standalone consumer sample result.
- Release artifact inventory.
- Final compatibility matrix.

## 9. Progress visibility procedure

### 9.1 Updating this plan

At every meaningful implementation checkpoint:

1. Update the document-control date and current phase.
2. Change task checkboxes only when their evidence exists.
3. Update the progress overview status.
4. Add a progress-log entry using the template below.
5. Record commands and summarized results, not merely “tests pass.”
6. Record blockers immediately and identify the exact decision or dependency needed.
7. Mark a phase gate complete only after all gate criteria are satisfied.

### 9.2 Progress log

Add new entries at the top.

#### 2026-08-24 — Rust-compatible automatic cache setup implemented

- `VectorDataSettings` now follows the Rust cache precedence: an explicit
  `cache_dir` wins; `$VECTORDATA_HOME` yields its isolated `cache/` directory
  without creating configuration; otherwise an absolute XDG cache candidate is
  conditionally auto-persisted only when the home filesystem is the largest
  writable mount (or mount discovery is unavailable).
- Automatic bootstrap creates both the cache directory and the Rust-compatible
  `settings.yaml` shape (`cache_dir`, `protect_settings: true`), while refusing
  to overwrite an existing settings file. Explicit builder cache settings do
  not trigger any automatic settings write.
- `VectorDataSettingsTest` verifies explicit precedence, isolated-home behavior,
  bootstrap persistence, directory creation, overwrite protection, and
  explicit-client isolation using only temporary directories.
- Evidence: `mvn -q -pl nb-vectordata verify -Dnb.junit.tags=` passed on
  2026-08-24. The HTTP cache tests used temporary loopback servers; no user
  config files were modified.

#### 2026-08-24 — Rust catalog and manifest dispatch matrix complete

- Completed: explicit YAML/YML shape dispatch (`profiles` document versus
  flat legacy entries); local and HTTP dataset-directory cascade
  (`dataset.yaml` then `knn_entries.yaml`); catalog-directory precedence
  (`catalog.json`, `catalog.yaml`, then legacy entries); raw JSON/YAML
  canonical catalog lists; and multi-profile legacy aggregation.
- Verification: deterministic tests cover direct arbitrary entries YAML,
  canonical precedence, local directory cascade, and embedded HTTP fallback.
  `mvn -q -pl nb-vectordata -Dnb.junit.tags= verify` passed.

#### 2026-08-24 — Live Rust-hosted canary passed

- Completed: default discovery of the vectordata-rs `catalogs.yaml` named-source
  configuration; live canary against the configured public `datasets.yaml`
  catalog and `ada002-100k:default` profile.
- Compatibility decision: when an HTTP data object is accessible but its
  optional `.mref` sidecar returns 401, 403, or 404, use the Rust-compatible
  unverified sparse-range fallback. A sidecar that is successfully served but
  malformed still fails closed.
- Verification: `mvn -q -pl nb-vectordata -Dnb.junit.tags= verify
  -Dvectordata.canary.catalog=https://vector-datasets-20260601.s3.us-east-1.amazonaws.com/public/datasets-clean/datasets.yaml
  -Dvectordata.canary.dataset=ada002-100k -Dvectordata.canary.profile=default
  -Dvectordata.canary.cache=/tmp/nb-vectordata-canary-cache` passed.
- Configuration safety: global files under `~/.config/vectordata` were read
  only; no global setting, credential, catalog, or cache configuration changed.

#### 2026-08-20 — Retry and release-canary hook

- Completed: bounded retries for transient HTTP status and I/O failures, with
  deterministic 503 recovery coverage; configuration-gated integration canary
  for a Rust-hosted catalog; canary invocation documented in the README.
- Verification: `mvn -q -pl nb-vectordata -Dnb.junit.tags= verify` passed.
- External input still required: an approved Rust-hosted catalog URL, dataset,
  and profile are needed to execute the live canary rather than skip it.

#### 2026-08-20 — Numeric and authenticated transport matrix expanded

- Completed: exact unsigned-64 value representation; f16 and f64 typed
  projections; i32 and i64 vvec sidecar reads; and bearer-token propagation to
  remote `.mref`, HEAD, and range requests.
- Verification: `mvn -q -pl nb-vectordata -Dnb.junit.tags= verify` passed.
- Remaining: widening/narrowing policy tests for all integer combinations,
  retries, cross-process cache coordination, and live Rust-hosted canary.

#### 2026-08-20 — Checked-in Rust fixture established

- Completed: a deterministic Rust fixture generator, checked-in xvec catalog
  artifacts, a fixture manifest with source baseline and SHA-256 values, and a
  Maven compatibility test that opens it through the public catalog API.
- Build decision: `nb-vectordata` disables test-resource filtering because the
  repository parent filters resources as text and would corrupt binary vector
  fixtures.
- Verification: `mvn -q -pl nb-vectordata clean verify -Dnb.junit.tags=`
  passed, including fixture, local-format, catalog, and loopback HTTP tests.
- Remaining: extend this fixture with pinned-crate-emitted `.mref` and vvec
  assets; add the stress/fault/canary coverage required for C6/C8.

#### 2026-08-20 — Compatibility hardening continued

- Completed: Rust-compatible padded multi-chunk `.mref` parsing; rejection of
  malformed published sidecars; no-Range full-transfer fallback; deterministic
  `knn_entries.yaml` profile synthesis; and fixture-backed `BaseVectors`
  migration coverage.
- Verification: `mvn -q -pl nb-vectordata -Dnb.junit.tags= verify` passed
  after the HTTP/Merkle additions. `mvn -q -pl nb-vectordata
  -Dnb.junit.tags= test` passed after legacy catalog coverage. `mvn -q -pl
  nb-virtdata/virtdata-lib-vectors -am test` passed with sandbox permission
  for existing system-interface tests.
- Remaining: the repository still needs a checked-in fixture emitted by the
  pinned `vectordata-rs` crate, not merely format-compatible generated test
  data; live-canary configuration and the remaining stress/fault matrix are
  also outstanding.

#### 2026-08-20 — Baseline implementation and consumer migration

- Status: Core implementation in progress; no phase gate is claimed complete.
- Completed: Java 17 standalone module; local scalar/xvec/vvec readers; typed
  projection; local/HTTP catalog and manifest resolution; profile inheritance,
  aliases, custom facets, and contiguous windows; sparse HTTP cache with
  `.mref` leaf verification and durable validity state; VirtData mapper and
  expression migration.
- Verification: `mvn -q -pl nb-vectordata -Dnb.junit.tags= verify` passed,
  including four unit tests and one loopback HTTP/Merkle integration test.
  `mvn -q -pl nb-virtdata/virtdata-lib-vectors -am -DskipTests compile` passed.
- Remaining: checked-in Rust-generated fixture matrix and regeneration,
  adversarial/retry/concurrency/cache-collision tests, strict window-byte
  prebuffering, live Rust-hosted canary, mapper fixture tests, and full-reactor
  release verification.
- Next checkpoint: C0/C6 fixture provenance and complete interoperability matrix.

#### 2026-08-20 — Plan established

- Status: Planning complete; implementation not started.
- Completed: Repository conventions, Rust access surface, legacy Java module, and current NoSQLBench consumer paths were inspected.
- Scope decision: Programmatic access only; the broader legacy capability set is excluded.
- Baselines: Rust commit `124931007878`; legacy Java commit `3f633142aa5c`.
- Next checkpoint: C0, compatibility contract freeze.
- Verification: Plan reviewed against the linked Rust source and current reactor structure; no implementation tests were run because no production code was changed.

### 9.3 Progress entry template

```markdown
#### YYYY-MM-DD — <checkpoint or work summary>

- Status: <phase and status>
- Completed:
  - <objective item>
- In progress:
  - <objective item>
- Blocked:
  - <blocker, or “None”>
- Decisions:
  - <decision and rationale>
- Verification:
  - `<exact command>` — <result and test count>
- Evidence:
  - <paths, reports, hashes, or commit>
- Next:
  - <next bounded task or checkpoint>
```

### 9.4 Checkpoint evidence template

```markdown
#### Checkpoint Cn — <name>

- Result: PASS | FAIL | BLOCKED
- Date:
- Commit:
- Gate criteria:
  - [x] ...
- Commands:
  - `...`
- Results:
  - Tests: N passed, N failed, N skipped
  - Integration tests: ...
- Compatibility changes:
  - None | <details>
- Known limitations:
  - None | <details>
- Reviewer/approval:
  - <name or reference>
```

## 10. Decision log

Record architectural decisions here before or as they are implemented.

| ID | Status | Decision | Rationale |
|---|---|---|---|
| D001 | Accepted | Create one root-level `nb-vectordata` JAR | The requested deliverable is a focused reusable access module, not another `nb-virtdata` sublibrary |
| D002 | Accepted | Scope is programmatic access only | Explicit project direction; prevents wholesale migration of the legacy module |
| D003 | Accepted | Pin initial behavior to Rust 1.7.1 commit `124931007878` | Makes conformance repeatable despite moving reference links |
| D004 | Accepted | Keep transport/storage implementations internal | Ensures every shape reader inherits the correct cache-first dispatch |
| D005 | Proposed | Compile public artifact to Java 17 bytecode | Broad consumer compatibility without multi-release/incubator complexity; freeze at C0 |
| D006 | Proposed | Preserve custom facets generically | Matches the advertised discover-then-load contract and avoids data loss during parsing; freeze at C0 |
| D007 | Proposed | Implement documented typed floating-point behavior | Correct access semantics are preferable to reproducing an apparent Rust implementation gap; freeze at C0 |
| D008 | Proposed | Windowed prebuffer guarantees view residency, not full backing-file residency | Makes sized profiles efficient and gives the success contract a precise meaning; freeze at C0 |
| D009 | Proposed | Use primitive-array reader values with `long` ordinals | Avoids boxing and removes legacy `int` count limitations; freeze API at C1 |
| D010 | Proposed | Use OkHttp plus SnakeYAML Engine as the only direct runtime dependencies | Provides robust HTTP and manifest parsing while keeping the artifact modular; validate at C1 |

## 11. Risk register

| Risk | Impact | Mitigation | Gate |
|---|---|---|---|
| Rust source and docs continue to drift | Cross-language incompatibility | Pin commit, golden fixtures, compatibility matrix, scheduled canary | C0/C6 |
| Legacy code pulls unrelated modules into the port | Bloated/non-modular artifact | Enforce reuse rule and dependency-tree gate | C1 |
| Concurrent cache writers expose partially written chunks | Silent data corruption | Valid-after-durable-write invariant, source/chunk dedup, file coordination tests | C4 |
| Cache identity collisions mix datasets | Silent wrong data | Dataset origin record and relpath collision rejection | C4 |
| Java unsigned types lose values | Incorrect typed reads | Exact scalar representation table and overflow tests | C3 |
| Very large datasets overflow Java `int` arithmetic | Incorrect counts/offsets | `long` ordinals/byte geometry and synthetic large-geometry tests | C3 |
| Vvec files lack remote offset indexes | Expensive initial scan | Fetch sibling first, cache-backed scan fallback, persist rebuilt index | C4 |
| Remote server ignores Range | Repeated full-body downloads | Detect Range support and promote a single full transfer | C4 |
| Authentication is applied inconsistently | Protected datasets fail unpredictably | One request builder/auth path for catalog, `.mref`, indexes, and data | C4 |
| Secrets leak through diagnostics | Security incident | Redaction policy and captured-log tests | C4 |
| Window semantics over-download massive base files | Excessive time and disk use | Compute record-to-byte range for uniform formats; measure request bytes | C5 |
| Release is coupled to external live service | Flaky builds | Deterministic embedded tests for PRs; live canary only scheduled/release | C6/C8 |
| Java 17 target conflicts with repository conventions | Build friction | Build on JDK 25, override only `release`, confirm plugin/Javadoc behavior at C1 | C1 |

## 12. Definition of done

The port is complete only when all of the following are true:

- [ ] Checkpoints C0 through C8 pass with evidence.
- [ ] `nb-vectordata` builds, tests, and packages independently in the reactor.
- [ ] Local, verified HTTP, unverified ranged HTTP, and non-range HTTP return identical values for equivalent content.
- [ ] Every required element type and record shape passes deterministic tests.
- [ ] Rust-generated catalogs, manifests, facets, indexes, and `.mref` files are consumed correctly.
- [ ] Cache interruption, restart, corruption, concurrency, origin collision, and promotion behavior is tested.
- [ ] Current NoSQLBench vector access uses `nb-vectordata`.
- [ ] No production dependency on `datatools-vectordata` remains.
- [ ] The public API exposes no internal transport or cache implementation.
- [ ] The live Rust-hosted canary passes.
- [ ] Full reactor verification passes.
- [ ] A clean standalone Maven consumer compiles and reads the compatibility fixture.
- [ ] README, Javadocs, compatibility policy, migration notes, and known limitations are complete.
