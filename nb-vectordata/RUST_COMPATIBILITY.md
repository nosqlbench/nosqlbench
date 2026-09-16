# vectordata-rs compatibility contract

This module implements the programmatic dataset-access surface of
`vectordata-rs` (baseline: crate 2.1.2, source commit `33fedae` — "Upload on the streams --concurrency promises").
When this document and an older Java implementation disagree, the Rust
implementation's current behavior and its format tests are normative.

## Supported data model

- Catalogs: JSON or YAML `catalog.json`/`catalog.yaml` entry lists and legacy
  flat `knn_entries.yaml`/`datasets.yaml` entries. A directory resolves in
  Rust order: `catalog.json`, `catalog.yaml`, then `knn_entries.yaml`.
- Dataset manifests: a `dataset.yaml`-shaped document (identified by its
  top-level `profiles` field) or a legacy flat entries document, regardless of
  an explicit YAML/YML filename. A dataset directory or URL resolves in Rust
  order: `dataset.yaml`, then `knn_entries.yaml` — the fallback applies only
  when `dataset.yaml` is absent; one that exists and is refused stays refused,
  with its own diagnosis.
- Format version: an optional top-level `format_version`. The number is a
  minimum reader requirement: a dataset above `FormatVersion.SUPPORTED`
  (`3`) is refused naming both numbers before anything else is read, and a
  stated version below what the content requires — `1` on a sharded
  dataset, `2` on one that names a parent — is refused as a declaration
  that understates what it holds. An **absent** field means `1` and the
  dataset is held to it: an unversioned manifest is read as pre-shard,
  pre-inheritance, pre-tag, and content that needs more is refused naming
  the version to declare (`declare format_version: 2`). A generous version
  is accepted. Version `2` is what a multi-file facet needs; version `3`
  is what a `profile_tags` schema or a profile naming a parent other than
  `default` needs. The version a dataset states is exposed on
  `TestDataGroup.formatVersion()` and, from a catalog listing's `layout`,
  on `CatalogEntry.formatVersion()`, so a caller can decide before it
  fetches. YAML is read under the 1.2 core schema the reference writes,
  so `~` is null — a naming tag in a schema — rather than the string it
  spells.
- Data: scalar, fixed-dimension xvec records, and variable-dimension vvec
  records with `IDXFOR__<file>.i32` or `.i64` index sidecars. Both sidecar
  layouts are accepted: `N+1` entries ending in an end-of-data sentinel,
  and the Rust walk-built form of `N` record starts with no sentinel.
  Slab containers (`.slab`) — paged records indexed by ordinal through a
  pages page in the file's tail, with optional named namespaces — are
  planned, prefetched, and prebuffered by the pages a window spans, and
  read by ordinal as records (see *Records* below).
- Multi-file facets: a facet may be a *series* of files forming one dense,
  gapless ordinal space, declared in either of the reference forms —
  uniform (`source: base__NNNN.fvec` with `shard_stride`, `shard_count`,
  and `record_count`; exactly four digits, contiguous from `0000`) or
  explicit (`source:` as an array of entries in the source-string grammar,
  with `record_count`). An entry is bare (local only), `=`-counted
  (`a.u8=4M`), windowed (`a.u8[0..1M]`), or both, checked against each
  other; the same file may appear in several entries at different windows.
  Entry windows are in that **file's** ordinals; the facet's `window:` is in
  **facet** ordinals, and a window suffix on a uniform pattern is the facet
  window (giving both is refused). Ordinal lookup is O(1) whenever the
  lengths are uniform however the series was spelled, and a prefix-sum
  search otherwise. Storage, sidecars, and offset indexes are per **file**
  — two shards drawn from one file share one of each. The readers present
  the same surface as a single-file facet; every shard is opened to check
  that it agrees on dimension and element type; a declared `record_count`
  is checked, never preferred; a declared shard that is absent is named,
  never read as a shorter facet; a bare remote entry in a series is refused
  at load, since its length could only be learned by fetching. A series
  holds no more files open than a budget derived from the process
  descriptor limit (a quarter of the soft limit, floor 8, or
  `VECTORDATA_SHARD_FD_CAP`), evicting least-recently used.
- Windows: the full `DSWindow` grammar — `..` bounds, open ends, `(`/`]`
  bound adjustment, count/size suffixes (`K`/`M`/`G`/`T`, `KB`…`TiB`,
  compound `1g24m`), comma-separated intervals, the structural
  window-suffix sugar on source strings (`base.fvec[0..1M)`), and the
  `=<count>` cardinality suffix (`a.u8[0..1M]=1M`), never split when the
  source carries a `?` query string and never taken when the tail is not a
  count. The serialized structured form — a list of `{min_incl, max_excl}`
  maps — and bare counts (`[0..N)`) are accepted on the `window:` key.
  Intervals that select no records are rejected at parse. Readers apply
  the first interval and clamp both bounds to the data. A source may name
  a slab namespace (`m.slab:content`, `m.slab:ns:[0..1K]`), or carry one
  beside it under `namespace:`/`ns:`; the text after the last `:` is a
  namespace only when the path before it has an extension and the text
  after names no directory, so a URL scheme or a drive letter never is.
- Profiles: a non-default profile inherits unstated facets from the profile
  it names with `inherits:`, else from `default`. What inherits depends on
  the axis, and **the axis is derived from `base_count`**: a step is on
  the size axis when the child's count differs from its parent's effective
  one, whatever the parent is called, and at one size otherwise. Across
  the size axis `base_vectors` and `metadata_content` inherit **re-cut**
  to `[0..base_count)` — a window the parent carries is replaced, so a
  `20m` built on `10m` reads the first twenty million of the same file —
  while the per-size outputs do not cross: the neighbor facets and
  `metadata_results`, all derived from `base_count`, so a sized profile
  that omits its own fails with "lacks facet" rather than serving the
  full base's. Across a step at one size every facet is invariant and
  inherits as is, and so does `base_count`; `maxk` crosses every step.
  A `partition: true` profile is an oracle partition with independent
  base vectors and inherits nothing. A parent YAML reads as a number — a
  rung named `100` — is the name it spells.
- Stated parents: from `format_version` 3 every profile other than
  `default` states `inherits:`, and a stated 3 is a claim that every
  parent is real — an absent parent ("names no parent; state `inherits:
  default` or the layer it builds on"), an unknown or self parent, a
  cycle (named once by its sorted members), `default` naming a parent,
  and `partition: true` beside `inherits:` are load refusals naming the
  profiles involved, joined by `; `. Below 3 an unknown or self parent
  falls back to `default` and a cycle leaves its members with what they
  declare; `ProfileParents.advisories` phrases the same conditions as
  what version 3 will refuse. Because a parent other than `default`
  itself requires version 3, those fallbacks are reachable only through
  in-memory legacy groups, as in the reference.
- Profile tags: an optional top-level `profile_tags` schema, naming tags
  in order, each with a default or `~` for a naming tag, loaded as
  written on `TestDataGroup.profileTags()` and from a catalog listing on
  `CatalogEntry.profileTags()`. Its presence requires version 3.
- Selectors: `ProfileSelector` is the reference's one parser — the
  grammar (`,` is AND; `and(…)`, `or(…)`, `not(…)`; a bare name is
  `profile=<name>` with a literal value), the value readings by spelling
  (regex by `^`/`$`, glob by `*`/`?`/`[`, `lo..hi` interval, number under
  the window count rule, boolean, literal; quoted is literal; every
  reading folds case), the structural keys read before attributes
  (`profile`, `base_count`, `maxk`, `partition`, `inherits`), dotted keys
  into map attributes, lists matching on any element, absence matching
  nothing under any operator, a comparison against a non-number being
  false rather than an error, and a `<`/`>` against a pattern being a
  parse error. `ProfileFacts` is what a selector reads: structure **as
  loaded** after inheritance, attributes the profile's own. `resolve`
  yields the set in the group's size-sorted order — `default` first, then
  by `base_count` or the name read as a count, then naturally by name —
  with `null` meaning `default`, `profile=*` meaning all, and no match
  failing with the offer listed; `resolveOne` refuses a set. Every
  failure is a `SelectionException` of kind `SYNTAX` (with position),
  `NO_MATCH`, `AMBIGUOUS`, or `NO_DEFAULT`, with the reference's message.
  `TestDataGroup.select`/`selectOne`/`profileFacts`/`profileNames`,
  `CatalogEntry.select`/`selectOne` (structure read through the declared
  `inherits` chain: `maxk` across any parent, `base_count` only across a
  named non-default one), `Catalog.open(dataset, selector)` (one
  profile, `null` for `default`), `Catalog.openProfiles(dataset,
  selector)` (the set), and `DatasetSpec` — the head found by shape (a
  URL's `://` and port, a drive letter), the selector starting at the
  first colon after it, a malformed selector reported at its position in
  the spec — replace the last-colon split. A bare name still opens the
  one profile it names on every surface, case folded.
- Group prebuffer: `TestDataGroup.prebuffer(names, fallback, progress,
  largeDownload)` prebuffers the named profiles in order — what a set
  selector resolved to — each facet against its declared window through
  the same planner, after tallying the announced total and reporting it
  when it reaches `PREBUFFER_LARGE_WARNING_BYTES` (250 MiB);
  `prebufferAll` is the same over every profile.
- Prefetch: caller-supplied record windows on any facet via
  `prefetchPlan`, `prefetch`, and `prefetchInBackground` on
  `TestDataView`, with `WholeFacetFallback` consent gating, chunk-level
  `RangeFill` residency accounting, chunk-adjacency range coalescing, an
  offset-index cache scoped to the view's facet handle, and an empty
  window meaning the whole facet (a request, never a degrade). A
  `FacetDescriptor` answers `shardCount()` — a uniform series'
  `shard_count`, `null` for a single file and for the explicit form,
  whose files are its entries — so a caller can report what opening a
  facet costs without realizing the series. Plan ranges
  are `ShardRange`s, qualified by the shard they lie in — a single file is
  shard `0` — because across a series the same byte offset exists in every
  file. A window decomposes into one sub-window per shard it spans, each
  mapped by the format's rule against its own file; ranges in different
  shards never merge; prerequisite bytes sum over the files touched; a
  whole-facet request names every shard. A slab window maps to the byte
  extent of the pages holding it — one contiguous range, since pages lie
  in ordinal order — through the index read from the slab's tail: the
  16-byte footer, the terminal pages page or namespaces page, the named
  namespace's pages page when indirected, and the last page's own record
  count. That bounded read, never a walk of the file, is what the plan
  reports as prerequisite bytes. A window starting past the end degrades
  rather than fabricating a range; one running past the end stops at the
  last page.
- Whole-profile prebuffer: `prebuffer(WholeFacetFallback, PrebufferProgress)`
  plans every facet against the window it declares for itself, with the
  same shard-aware planner the selective prefetch uses, refusing a declared
  window the format cannot map under `REFUSE` exactly as a requested one —
  so a sized profile over a large base fetches what it can address and
  nothing more. `prebuffer(PrebufferProgress)` is the `REFUSE` form.
- Records: a slab facet opens through `openFacetRecords` as a
  `RecordFacet` — ordinals and bytes, before a codec is chosen — and a
  codec applied with `decode` yields a typed `Records` reader. The facet
  holds one container per file, each shard of a series an ordinary slab
  based at zero, so a facet ordinal is answered by the container that owns
  it at its local ordinal; counts come from the containers, never from
  the shard declaration. A namespace written into the declaration
  (`m.slab:content`, `namespace:`) selects that document, one absent from
  the container holds no records, and `namespace(name)` opens a sibling
  document of the same containers. Reading one record from a remote
  facet costs one page, not the file. The codecs compose the reference's
  two stages: `Codecs.ANODE` stops after stage 1 — the record as the
  `ANode` its leader byte says it is, `0x01` an `MNode` and `0x02` a
  `PNode`, never inferred from the facet; `Codecs.text(vernacular)` adds
  stage 2 and renders it; `Codecs.TREE` hands back plain Java values, the
  role a serde target plays in the reference. `Codecs.byName` resolves a
  vernacular name through the same table every other by-name surface
  uses, so a codec chosen at runtime and one written in code decode
  identically; `anode` names none, since it produces no text.
- ANode wire formats: an `MNode` is `[0x01][u16 count]` of
  `[u16 name][name][tag][value]` fields over the 29 `TypeTag`s (`text`
  through `typed_map`, little-endian, `text_validated` read as text and
  `decimal`/`varint` as bytes). A `PNode` is a pre-order tree of
  `[conjugate type][…]` nodes in three sub-formats: indexed (`[0][field
  index][op][i16 count][i64…]`), named legacy (a conjugate byte after
  the leader, `i64` comparands), and named typed (`0xFF` after the
  leader, comparands tagged `0` int, `1` float, `2` text, `3` bool, `4`
  bytes, `5` null). Fingerprints replace every value with its type
  default; congruence compares fingerprints. The `Display` grammar
  (`age > 18`, `status IN (1, 2)`, `(a = 1 AND b < 2)`) round-trips
  through `PNodeDisplay.parse`. `PredicateEvaluator` applies a tree to a
  record with the reference's coercions — integer families against `Int`,
  float families against `Float`, the two cross-compared numerically,
  text families against `Text`, `MATCHES` as substring containment over
  text or bytes, a missing field equal to `NULL` and nothing else.
- Vernaculars: the reference's thirteen — `cddl`, `cddl-value`, `sql`,
  `sql-schema`, `sqlite`, `sqlite-schema`, `cql`, `cql-schema`, `json`,
  `jsonl`, `yaml`, `readout`, `display` — each rendering both node kinds
  in its own syntax, with the sqlite pair spelled as the sql pair. JSON,
  YAML, SQL, CQL, CDDL, and readout parse back with the reference's type
  inference (quoted strings, bare integers, decimal-point floats,
  `true`/`false`, each language's null); the schema, value, and display
  renderings refuse to, naming themselves.
- Binding: the reference's `binding` module, for driving load rather
  than rendering text. A `Layout` is learned **once** from a facet's
  first record — names in wire order, each with the `BindType` its tag
  implies — and a `Binder` compiled against it (`all`, or `select` in
  template order, with parameter renames applied at compile time) binds
  every record after by position: the walk in `Scan.fields` skips names
  and reaches each value as a `Field` view over the record's own bytes,
  read through primitive accessors typed by the wire form, or through
  `value()` for the AST view. A field the facet lacks is refused when the
  binder is built, naming what it has; a record whose layout differs
  from the compiled one is refused, not bound short. `BindType` is its
  own exhaustive mapping from the tag, distinct from the schema
  vernaculars': a `Half` binds as `FLOAT16`, `DateTime` as
  `TIMESTAMP_TEXT`, `Null` as no type, and containers with their element
  types left undetermined rather than guessed as text. Forms are read
  from the facet's `forms` namespace — one JSON record per form, unknown
  keys preserved, unreadable records skipped — and a facet without one
  offers exactly one implicit form named `default`, which is every
  dataset written before forms existed; an unknown form is refused
  naming what is offered, the implicit one not listed as a choice. A
  `PredicateBinder` compiles a flat conjunction against a sample
  predicate **and** the layout, typing each `Condition` from the field's
  tag rather than the comparand's variant, and binds a record's
  comparands only after its leader byte says PNode and its fingerprint
  matches the compiled shape; a disjunctive or nested template is
  refused at compile time.
- Facet shape: every extension the spec names belongs to a `FacetFormat`
  with a `FacetShape` — element runs, or opaque records — and `facetShape`
  answers it so a caller handling both branches on the fact rather than
  on a failure. Each reader refuses the other shape at the door, naming
  the reader that opens it (`WrongFacetShapeException` from the element
  readers, `RecordException` of kind `WRONG_SHAPE` from the record path)
  instead of failing on the symptom — a slab parsed as vectors, or an
  xvec parsed for a footer.
- Types: signed and unsigned 8/16/32/64-bit integers plus f16, f32, and f64.
- Sources: local files, `file:` URIs, and HTTP(S) URLs.
- Remote caching: sparse range caching, `.mref` SHA-256 Merkle verification,
  cache promotion to a local mapped reader, and full-transfer fallback when a
  server cannot service byte ranges. A facet spanning several files reports
  the **weakest** access mode among them, its bytes and chunk counts summed,
  and is complete when every byte it can address is resident — not every
  byte of every file it draws from.
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
profiles, failed integrity checks, and shard declarations that disagree with
themselves (mixed forms, half-stated layouts, a count contradicting its
interval, a total contradicting its shards, an all-digit token before the
shard field) fail with a `VectorDataException`; they are never silently
repaired. The shard error messages carry the facet name and the shard index
or file they concern, following the reference taxonomy, so the same broken
dataset is diagnosable in either runtime.

The dispatch behavior above is covered by deterministic local tests and an
embedded HTTP-server integration test, so Rust precedence is stable across
releases instead of depending on a filename convention.

Automatic cache setup is tested exclusively with temporary, explicit settings
roots. The test suite never writes a developer's real `~/.config/vectordata`.

## Known source-level clarifications

The Rust storage code permits a no-`.mref` HTTP fallback even where older
documentation only described full transfer. This Java implementation supports
that fallback. It also preserves custom manifest facets — which inherit as
invariant facets, like every non-neighbor facet — and supports float typed
reads, which are part of the advertised API contract. The older `extends:`
profile key is kept as a spelling of `inherits:`.

Prefetch semantics — scalar windows at the element stride, planning that
never rebuilds a remote offset index (`degradesToFullDownload` plus the
consent gate instead), sentinel-tolerant sidecar parsing with
`prerequisiteBytes` as record starts × 8, size-directed sidecar probing,
and atomic persistence of locally walked indexes — follow the Rust
source directly as of the baseline commit above.

The reference's writer-side surfaces — layering a generated rung into a
size layer and a `-mixed` set, naming generated profiles from their
tags, the textual `dataset.yaml` editors, `veks check` advisories, push
and precache commands — are dataset authoring, not the access API, and
are not ported; the loader-side rules they depend on (stated parents,
the derived axis, tag schemas, selectors) are.

Remaining representation differences:

- **`ivec`/`ivecs` facets are uniform-stride.** The format requires
  length-qualified records that are fixed throughout — ground-truth
  neighbor files — so windows map at the header stride, and only the
  `*vvec` extensions carry variable-length records. (The Rust source's
  `is_vvec_ext` still classifies `ivec` as variable; to be reconciled
  upstream.)
- **The serde codec is a tree codec.** The reference's third codec
  deserializes into any serde target; Java has no serde, so `Codecs.TREE`
  produces the one target that stands for all of them — an
  insertion-ordered map of `String`, `Long`, `Double`, `Boolean`, `null`,
  lists, and nested maps, typed exactly as the JSON vernacular types the
  same record. A caller wanting a class binds it from that map.
- **JSON parses in document order.** The JSON vernacular reads back with
  a minimal strict parser that keeps object keys in the order written, so
  a record round-tripped through JSON keeps its field order; the reference
  relies on its JSON library's map, whose order is that library's choice.
  Numbers follow the reference's inference: no point or exponent and fits
  a long is an integer, anything else numeric is a float.
- **A regular expression outside RE2 is refused at parse.** The
  reference evaluates the Rust `regex` crate's RE2 subset; Java's engine
  accepts more, so a lookaround (`(?=`, `(?!`, `(?<=`, `(?<!`), an atomic
  group, or a backreference is refused as a syntax error rather than
  accepted here and rejected there. Everything inside the subset is
  matched case-insensitively against the whole canonical text with
  `find`, as `is_match` does.
- **An explicit series answers no `shardCount()`.** The reference's
  `shard_count()` is `Some` only for the uniform form; the explicit form's
  file count is `series().entries().size()`.
- **A bound field is a small view object.** The reference hands out a
  borrowed `Field` by value; here each field the walk meets is one
  object holding offsets into the record's bytes, and its primitive
  accessors (`longValue`, `doubleValue`, …) copy nothing. What the
  contract forbids — resolving or copying a field *name* per record — is
  honored: names are read only when a layout is discovered. A Java
  caller wanting the allocation-free loop uses `bindEach` with the
  primitive accessors and never calls `name()` or `value()` per cycle.
- **A form's unknown keys are kept as node values.** The reference keeps
  them as its JSON library's values; here `Form.extra()` holds the
  `MValue`s the JSON vernacular parses them to, typed as that parse
  types them.
- **A CQL conjunction renders without parentheses.** The reference's
  CQL vernacular wraps `AND`-joined relations in parentheses, as its SQL
  vernacular does; a CQL `WHERE` clause has no grouping parentheses and
  Cassandra rejects the wrapped form. Here the CQL rendering of a
  conjunction is the bare `a AND b`, and the SQL rendering, the `!=`
  rewrite, and an `OR` keep the reference's parentheses. To be
  reconciled upstream.
- **The raw predicate scanner is not ported.** The reference's
  `mnode::scan` also compiles predicates to positions and evaluates them
  against raw bytes for its predicate-index pipeline; that is workload
  generation, not the access API. The walk, the schema discovery, and
  the predicate flattening it shares with binding are here; predicate
  evaluation over a record goes through `PredicateEvaluator` on the
  decoded node.
- **A windowed reader's `prebuffer` fetches its window.** The reference's
  windowed reader inherits a no-op `precache`; here the reader a view hands
  back for a windowed facet warms the same bytes the whole-profile prebuffer
  would — its declared window, planned per shard — rather than the file it
  was cut from. A sharded reader's own `prebuffer` fetches every file whole,
  as the reference's does; the window-scoped fetch is the view's.
- **The group prebuffer's progress is per profile.** The reference's
  callback takes `(profile, facet, progress)`; here `progress` is a
  function from the profile name to the `PrebufferProgress` that profile's
  facets report through, since the view-level prebuffer reports byte
  totals without naming the facet.
- **A non-range-capable remote file degrades a window** that touches it,
  rather than planning a partial fetch it cannot perform. Rust reaches the
  same end state by downloading such a file whole at open; Java opens
  lazily, so the plan reports the honest cost and `WholeFacetFallback`
  decides.
- **The offset-index cache is view-scoped**, not per open call: holding
  a `TestDataView` is how a caller says it will ask repeatedly, matching
  the lifetime intent of the Rust facet handle.
- **Unbounded interval ends are `Long.MAX_VALUE`** rather than `u64::MAX`
  and clamp to the facet, which no supported payload can exceed. An
  open-ended window on a series entry is measured against its file like a
  bare entry, and is refused remotely on the same grounds.
- **Evicting a file from a series' open-file budget releases the series'
  reference** rather than closing the file: storage is shared by source
  across readers, and the registry holds it weakly, so a file nobody else
  holds closes on its own.
