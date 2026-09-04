/*
 * Copyright (c) 2026 The NoSQLBench Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.VectorDataException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/// Ordinal mapping for multi-file facets, mirroring the `vectordata-rs`
/// shard model.
///
/// A facet may be one file or a series of them. Either way its records
/// form one dense, gapless ordinal space, and this class turns a global
/// ordinal in that space into the file that holds it and the ordinal
/// within that file. Three coordinate levels, never conflated:
///
/// ```text
/// global ordinal  o
///    ↓  locate                     — which shard, how far into it
/// local ordinal   l   within shard s
///    ↓  + entries[s].fileBase      — the entry window's lower bound
/// file ordinal    f   within the file s is drawn from
///    ↓  the format's record→byte rule — see Prefetcher
/// byte offset
/// ```
///
/// **Shards and files are counted separately.** A shard is a contiguous
/// run of ordinals; a file is where bytes live. Two shards may be drawn
/// from one file at different windows, in which case they are two
/// shards and one file. Ordinal mapping is also separate from source
/// resolution: the map answers "which shard, and how far into it"; the
/// entries answer "which file, and how far into that".
public final class Shards {
    /// The literal token marking the shard-index field in a uniform
    /// source. Exactly four `N`s: the width is fixed, so there is no
    /// `NNN` or `NNNNN` form to accept.
    public static final String SHARD_FIELD = "NNNN";

    /// How a global ordinal becomes a shard index and a local ordinal.
    /// The two arms are resolved once, when the declaration is
    /// realized, and are thereafter a single dispatch — never a general
    /// path with a fast case tested for on every lookup.
    sealed interface OrdinalMap permits Uniform, Explicit {
        long total();
        int shardCount();
        long shardBase(int shard);
        long shardLen(int shard);
        /// `{shard, local}`, or `null` past the end — never a clamp,
        /// because a silently clamped ordinal reads the wrong record.
        long[] locate(long ordinal);
    }

    /// Every shard but the last holds exactly `stride` ordinals, so the
    /// lookup is division and remainder: O(1), no allocation, no search.
    record Uniform(long stride, int count, long total) implements OrdinalMap {
        @Override public int shardCount() { return count; }
        @Override public long shardBase(int shard) { return (long) shard * stride; }
        @Override public long shardLen(int shard) { return Math.min(shardBase(shard) + stride, total) - shardBase(shard); }
        @Override public long[] locate(long ordinal) {
            return ordinal < 0 || ordinal >= total ? null : new long[] {ordinal / stride, ordinal % stride};
        }
    }

    /// Shard lengths are uneven, so the lookup is a binary search over
    /// prefix sums. `starts` has `count + 1` entries: it opens at `0`,
    /// closes at `total`, and is strictly increasing.
    record Explicit(long[] starts, long total) implements OrdinalMap {
        @Override public int shardCount() { return starts.length - 1; }
        @Override public long shardBase(int shard) { return starts[shard]; }
        @Override public long shardLen(int shard) { return starts[shard + 1] - starts[shard]; }
        @Override public long[] locate(long ordinal) {
            if (ordinal < 0 || ordinal >= total) return null;
            // The last index whose start is <= ordinal.
            int found = Arrays.binarySearch(starts, ordinal);
            int shard = found >= 0 ? found : -found - 2;
            return new long[] {shard, ordinal - starts[shard]};
        }
    }

    /// Builds the map from per-shard lengths, collapsing to [Uniform]
    /// whenever the lengths permit it. Uniformity is a property of the
    /// lengths, not of how the series was spelled: a list of
    /// evenly-sized files — what an importer routinely produces — gets
    /// the O(1) map. `null` if any length is zero: a zero-length shard
    /// contributes no ordinals and would put two shards at one
    /// prefix-sum boundary.
    static OrdinalMap fromLengths(long[] lens) {
        if (lens.length == 0) return null;
        long total = 0;
        for (long len : lens) { if (len <= 0) return null; total += len; }
        long stride = lens[0];
        boolean uniform = lens[lens.length - 1] <= stride;
        for (int i = 0; i < lens.length - 1 && uniform; i++) uniform = lens[i] == stride;
        if (uniform) return new Uniform(stride, lens.length, total);
        long[] starts = new long[lens.length + 1];
        for (int i = 0; i < lens.length; i++) starts[i + 1] = starts[i] + lens[i];
        return new Explicit(starts, total);
    }

    /// One shard's binding to the file it is drawn from: the resolved
    /// source path and its slab namespace, if any — the window has
    /// already been folded into `fileBase` and `len` — the first
    /// **file** ordinal this shard reads (zero for a whole-file shard,
    /// the entry window's lower bound for a sliced one), and the
    /// ordinals it holds.
    public record Entry(String source, String namespace, long fileBase, long len) {
        public Entry(String source, long fileBase, long len) { this(source, null, fileBase, len); }
        /// The path with its namespace, the form a slab is addressed by.
        public String locator() { return namespace == null ? source : source + ":" + namespace; }
    }

    /// Where a global ordinal lives: the shard, its offset within that
    /// shard, and its ordinal within the file the shard is drawn from.
    public record Located(int shard, long local, long fileOrdinal) { }

    /// One shard's slice of a decomposed window: half-open bounds in the
    /// shard's local ordinals, and the same bounds in the file's
    /// ordinals, which is what a format's record→byte rule wants.
    public record SubWindow(int shard, long localLo, long localHi, long fileLo, long fileHi) { }

    private final OrdinalMap map;
    private final List<Entry> entries;

    private Shards(OrdinalMap map, List<Entry> entries) { this.map = map; this.entries = List.copyOf(entries); }

    /// Builds from per-shard entries, deriving the map from their
    /// lengths; `null` when the entries are empty or any is zero-length.
    static Shards of(List<Entry> entries) {
        long[] lens = new long[entries.size()];
        for (int i = 0; i < lens.length; i++) lens[i] = entries.get(i).len();
        OrdinalMap map = fromLengths(lens);
        return map == null ? null : new Shards(map, entries);
    }

    /// The shard entries, indexed by shard number.
    public List<Entry> entries() { return entries; }
    /// Total records across the series.
    public long count() { return map.total(); }
    public int shardCount() { return map.shardCount(); }
    /// First global ordinal of a shard.
    public long shardBase(int shard) { return map.shardBase(shard); }
    /// Ordinals held by a shard.
    public long shardLen(int shard) { return map.shardLen(shard); }
    /// Whether the uniform O(1) map is in force.
    public boolean isUniform() { return map instanceof Uniform; }
    /// Whether this facet is a single whole file — the canonical shape
    /// for everything written before sharding existed.
    public boolean isSingleFile() { return entries.size() == 1 && entries.get(0).fileBase() == 0; }

    /// Resolves a global ordinal through all three coordinate levels, or
    /// `null` past the end of the series.
    public Located locate(long ordinal) {
        long[] at = map.locate(ordinal);
        if (at == null) return null;
        int shard = (int) at[0];
        return new Located(shard, at[1], entries.get(shard).fileBase() + at[1]);
    }

    /// Decomposes the window `[lo, hi)` into per-shard sub-windows.
    /// Empty for an empty or out-of-range window; never emits an empty
    /// sub-window, so a window ending exactly on a shard boundary does
    /// not produce a trailing no-op.
    public List<SubWindow> decompose(long lo, long hi) {
        long end = Math.min(hi, map.total());
        if (lo < 0 || lo >= end) return List.of();
        int first = (int) map.locate(lo)[0];
        int last = (int) map.locate(end - 1)[0];
        List<SubWindow> parts = new ArrayList<>();
        for (int shard = first; shard <= last; shard++) {
            long base = map.shardBase(shard), len = map.shardLen(shard);
            long localLo = Math.max(lo, base) - base, localHi = Math.min(end, base + len) - base;
            long fileBase = entries.get(shard).fileBase();
            parts.add(new SubWindow(shard, localLo, localHi, fileBase + localLo, fileBase + localHi));
        }
        return parts;
    }

    // -- Declarations --

    /// A facet declaration in the form the shard model consumes: source
    /// strings in ordinal order — one for a single file or a uniform
    /// series, several for an explicit one — whether they came from an
    /// array, and the uniform layout fields.
    public record Declaration(List<String> sources, boolean isArray, Long shardStride, Integer shardCount, Long recordCount) {
        public Declaration { sources = List.copyOf(sources); }
    }

    /// Answers how many records a file holds. Consulted only for an
    /// entry that declares neither a window nor a count — the
    /// local-convenience spelling; every other entry is self-describing
    /// and never reaches this. Throws with a reason when it cannot.
    @FunctionalInterface
    public interface Cardinality { long of(SourceSpec source); }

    /// Whether a source string declares a uniform series.
    public static boolean hasShardField(String source) { return source.contains(SHARD_FIELD); }

    /// Substitutes the shard index into a uniform source's `NNNN` field.
    /// The four digits appear here and nowhere else, so a writer and a
    /// reader cannot drift apart on the width.
    public static String shardFilename(String pattern, int index) {
        return pattern.replaceFirst(SHARD_FIELD, String.format(Locale.ROOT, "%04d", index));
    }

    /// Realizes a declaration into the ordinal model. This is where
    /// declaration shape stops mattering: whatever form the facet was
    /// written in — single file, uniform series, explicit series, pinned
    /// or bare — the result is one [Shards], and no stage above this
    /// branches on the spelling again. A non-canonical single-shard
    /// declaration is accepted and realized as the single-file facet it
    /// describes; reporting it is a validator's job, not a loader's.
    public static Shards realize(String facet, Declaration decl, Cardinality cardinality) {
        boolean uniform = !decl.sources().isEmpty() && hasShardField(decl.sources().get(0));
        boolean layout = decl.shardStride() != null || decl.shardCount() != null;
        if (decl.isArray() && layout)
            throw new VectorDataException("facet '" + facet + "': an array source cannot also carry shard_stride or shard_count"
                + " — the array already states the layout");
        if (uniform) return realizeUniform(facet, decl);
        if (layout) throw incomplete(facet, "shard_stride/shard_count without a '" + SHARD_FIELD + "' field in the source");
        return realizeEntries(facet, decl, cardinality);
    }

    /// Marker detail for a length that only the file can answer.
    static final String UNPROBED = "deferred: needs the file";

    /// Checks everything about a declaration that can be checked without
    /// touching a file: mixed forms, half-stated uniform fields, a count
    /// that contradicts its interval, a total that contradicts its
    /// entries. A length that only the file can answer is not a fault
    /// here — it is the local-convenience spelling, resolved at open.
    /// Implemented by running [#realize] with a probe that declines,
    /// rather than by restating its rules: two copies of these checks
    /// would be two chances to disagree.
    public static void validate(String facet, Declaration decl) {
        try { realize(facet, decl, source -> { throw new VectorDataException(UNPROBED); }); }
        catch (VectorDataException fault) { if (!fault.getMessage().endsWith(UNPROBED)) throw fault; }
    }

    private static VectorDataException incomplete(String facet, String detail) {
        return new VectorDataException("facet '" + facet + "': incomplete shard declaration — " + detail);
    }

    private static VectorDataException at(String facet, int index, String detail) {
        return new VectorDataException("facet '" + facet + "' shard " + index + ": " + detail);
    }

    /// The uniform form: filenames derived from `NNNN`, lengths from the
    /// stride and the declared total.
    private static Shards realizeUniform(String facet, Declaration decl) {
        if (decl.isArray()) throw incomplete(facet, "a '" + SHARD_FIELD + "' field belongs to a single source string, not an array");
        String pattern = decl.sources().get(0);
        String ambiguous = ambiguousTokenBeforeShardField(pattern);
        if (ambiguous != null)
            throw incomplete(facet, "the token '" + ambiguous + "' before the '" + SHARD_FIELD + "' field is all digits, so the"
                + " derived filenames have two readings and neither is decidable; give it a non-numeric prefix");
        if (decl.shardStride() == null) throw incomplete(facet, "'" + SHARD_FIELD + "' without shard_stride");
        if (decl.shardCount() == null) throw incomplete(facet, "'" + SHARD_FIELD + "' without shard_count");
        if (decl.recordCount() == null) throw incomplete(facet, "a sharded facet must declare record_count");
        long stride = decl.shardStride(); int count = decl.shardCount(); long total = decl.recordCount();
        if (stride <= 0) throw incomplete(facet, "shard_stride must be greater than zero");
        if (count <= 0) throw incomplete(facet, "shard_count must be greater than zero");
        // Lengths follow from the stride and the declared total: every
        // shard but the last is `stride`, and the last is whatever
        // remains. A remainder outside `1..=stride` means the
        // declaration disagrees with itself.
        long full = (count - 1L) * stride;
        if (total <= full || total > full + stride)
            throw new VectorDataException("facet '" + facet + "': declared record_count " + total + " does not match the "
                + (full + stride) + " records its shards hold");
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            SourceSpec source;
            try { source = SourceSpec.parse(shardFilename(pattern, i)); }
            catch (VectorDataException malformed) { throw at(facet, i, malformed.getMessage()); }
            entries.add(new Entry(source.path(), source.namespace(), 0, i < count - 1 ? stride : total - full));
        }
        Shards shards = of(entries);
        if (shards == null) throw at(facet, 0, "resolves to zero records");
        return shards;
    }

    /// The single-file and explicit forms: one entry per listed source.
    private static Shards realizeEntries(String facet, Declaration decl, Cardinality cardinality) {
        List<Entry> entries = new ArrayList<>(decl.sources().size());
        for (int i = 0; i < decl.sources().size(); i++) {
            SourceSpec parsed;
            try { parsed = SourceSpec.parse(decl.sources().get(i)); }
            catch (VectorDataException malformed) { throw at(facet, i, malformed.getMessage()); }
            DSWindow window = parsed.window();
            if (window.intervals().size() > 1)
                throw at(facet, i, "an entry carries at most one interval — list the file once per interval instead");
            DSWindow.Interval interval = window.isEmpty() ? null : window.intervals().get(0);
            long fileBase, len;
            if (interval != null && interval.maxExcl() != Long.MAX_VALUE) {
                fileBase = interval.minIncl();
                len = Math.max(0, interval.maxExcl() - interval.minIncl());
                // The edifying count checks whatever it annotates: the
                // interval's length when there is one.
                if (parsed.declaredCount() != null && parsed.declaredCount() != len)
                    throw at(facet, i, "declared count " + parsed.declaredCount() + " does not match its length " + len);
            } else if (interval == null && parsed.declaredCount() != null) {
                fileBase = 0; len = parsed.declaredCount();
            } else {
                // Nothing self-describing — a bare name, or an open-ended
                // window: the length can only come from the file.
                // Refused for a remote *series*, where building the map
                // would open every shard before a single record is read,
                // which is the expense a declaration exists to avoid. A
                // single remote file is not that case: its reader must
                // open it to read anything, so its count is the same open
                // rather than an extra one.
                if (decl.isArray() && isRemote(parsed.path()))
                    throw at(facet, i, "remote entry '" + parsed.path() + "' states no window or count, and its length cannot be"
                        + " learned without fetching");
                long records;
                try { records = cardinality.of(parsed); }
                catch (VectorDataException unavailable) { throw at(facet, i, "cannot establish record count — " + unavailable.getMessage()); }
                fileBase = interval == null ? 0 : interval.minIncl();
                len = Math.max(0, records - fileBase);
            }
            if (len == 0) throw at(facet, i, "resolves to zero records");
            entries.add(new Entry(parsed.path(), parsed.namespace(), fileBase, len));
        }
        Shards shards = of(entries);
        if (shards == null) throw at(facet, 0, "resolves to zero records");
        // A declared total is checked, never preferred. Only a series is
        // required to carry one; a plain single-file facet has always
        // been spelled without.
        if (decl.recordCount() != null && decl.recordCount() != shards.count())
            throw new VectorDataException("facet '" + facet + "': declared record_count " + decl.recordCount()
                + " does not match the " + shards.count() + " records its shards hold");
        if (decl.isArray() && decl.recordCount() == null) throw incomplete(facet, "a sharded facet must declare record_count");
        return shards;
    }

    /// The all-digit token, if any, immediately before the `__NNNN`
    /// field of a pattern's stem. Such a name has two readings — the
    /// token could itself be a shard index — so it is refused rather
    /// than guessed at.
    static String ambiguousTokenBeforeShardField(String pattern) {
        int colon = pattern.indexOf(':');
        String path = colon < 0 ? pattern : pattern.substring(0, colon);
        int dot = path.lastIndexOf('.');
        String stem = dot < 0 ? path : path.substring(0, dot);
        if (!stem.endsWith(SHARD_FIELD)) return null;
        String before = stem.substring(0, stem.length() - SHARD_FIELD.length());
        if (!before.endsWith("__")) return null;
        before = before.substring(0, before.length() - 2);
        int separator = before.lastIndexOf("__");
        String token = separator < 0 ? before : before.substring(separator + 2);
        return !token.isEmpty() && token.chars().allMatch(Character::isDigit) ? token : null;
    }

    static boolean isRemote(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
