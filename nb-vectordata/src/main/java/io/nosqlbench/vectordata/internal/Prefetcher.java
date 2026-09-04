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

import io.nosqlbench.vectordata.AccessMode;
import io.nosqlbench.vectordata.CacheStats;
import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.ElementType;
import io.nosqlbench.vectordata.FacetDescriptor;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.PrefetchPlan;
import io.nosqlbench.vectordata.RangeFill;
import io.nosqlbench.vectordata.ShardRange;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.WholeFacetFallback;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/// Resolves record-coordinate prefetch windows to byte ranges and
/// plans their chunk-level cost, mirroring the `vectordata-rs` prefetch
/// surface. Three per-format mappings, each applied to a shard's
/// **file** ordinals:
///
/// - **xvec** (uniform stride): `4 + dim × elemSize`, with `dim` read
///   from the header at byte 0 — the same first-chunk read any reader
///   does on first access.
/// - **vvec** (variable length): the sibling `IDXFOR__` offset index of
///   that file, loaded whole and cached on the facet handle; on local
///   storage a file without a sidecar is walked once instead.
/// - **scalar** (raw packed values): `ordinal × elemSize` exactly,
///   with no header.
///
/// A window over a facet spread across several files decomposes into
/// one sub-window per shard it touches, and each maps by the rule above
/// against its own file — so a window costs the shards it spans, and
/// ranges in different shards never merge.
///
/// `null` from a mapping means "cannot window this" — an unsupported
/// format, a corrupt header, an index that cannot be loaded, or an
/// empty range. That is the answer, not a failure, and
/// [WholeFacetFallback] decides what a caller does with it.
public final class Prefetcher {
    private Prefetcher() { }

    /// A record range within one file resolved to bytes, and what
    /// resolving it cost.
    record MappedRange(long byteStart, long byteEnd, long prerequisiteBytes) { }

    /// A record range resolved to bytes across the shards it spans.
    record MappedRanges(List<ShardRange> ranges, long prerequisiteBytes) { }

    /// Per-facet handle holding the lazily opened storage — one file,
    /// or the [FacetSeries] a multi-file facet resolves to — and the
    /// offset index once loaded. The index cache is scoped to the view
    /// that owns this handle: holding a view is how a caller says it
    /// will ask about its facets repeatedly, and dropping it is how
    /// they say they are done.
    ///
    /// Everything byte-shaped is asked per shard, a single file being
    /// shard `0`, so the planner never branches on layout.
    public static final class FacetHandle {
        private final FacetDescriptor facet;
        private final VectorDataSettings settings;
        private final String identity;
        private volatile ByteStorage data;
        private volatile FacetSeries series;
        private volatile long[] offsets;

        public FacetHandle(FacetDescriptor facet, VectorDataSettings settings, String identity) {
            this.facet = facet; this.settings = settings; this.identity = identity;
        }

        public FacetDescriptor facet() { return facet; }

        /// Whether this facet spans more than one file.
        public boolean isSeries() { return facet.isSeries(); }

        /// The one storage behind a single-file facet.
        public ByteStorage data() {
            if (facet.isSeries())
                throw new VectorDataException("facet '" + facet.name() + "' is a series of " + facet.series().entries().size()
                    + " sources and has no single storage");
            ByteStorage current = data;
            if (current == null) {
                synchronized (this) {
                    if (data == null) data = new StorageFactory(settings).open(facet.source(), identity);
                    current = data;
                }
            }
            return current;
        }

        /// The series behind a multi-file facet, realized on first ask
        /// from its declaration. A bare local entry is measured by
        /// opening its file; a remote one was refused at load.
        public FacetSeries series() {
            if (!facet.isSeries()) throw new VectorDataException("facet '" + facet.name() + "' is a single file, not a series");
            FacetSeries current = series;
            if (current == null) {
                synchronized (this) {
                    if (series == null) {
                        FacetDescriptor.Series declared = facet.series();
                        Shards.Declaration declaration = new Shards.Declaration(declared.entries(), declared.declaredAsArray(),
                            declared.shardStride(), declared.shardCount(), declared.recordCount());
                        Shards shards = Shards.realize(facet.name(), declaration, source -> {
                            ByteStorage storage = new StorageFactory(settings).open(URI.create(source.path()), identity);
                            Long records = recordsIn(source.path(), storage, settings, identity);
                            if (records == null) throw new VectorDataException("cannot derive a record count from '" + source.path() + "'");
                            return records;
                        });
                        series = new FacetSeries(shards, settings, identity);
                    }
                    current = series;
                }
            }
            return current;
        }

        /// Size of the facet: every shard's bytes, not the first shard's.
        public long totalSize() { return isSeries() ? series().totalSize() : data().size(); }

        /// Bytes per shard, in shard order; `0` for a shard whose file
        /// will not open.
        public long[] shardSizes() { return isSeries() ? series().shardSizes() : new long[] {data().size()}; }

        /// The storage a shard's bytes live in.
        public ByteStorage shardStorage(int shard) {
            if (!isSeries()) { if (shard != 0) throw new VectorDataException("shard " + shard + " out of range"); return data(); }
            return series().file(series().fileIndexOfShard(shard));
        }

        /// Chunk residency of a byte range within one shard, or `null`
        /// where the file has no chunks — or will not open, which a
        /// plan reports through its sizes rather than by failing here.
        public RangeFill shardRangeFill(int shard, long byteStart, long byteEnd) {
            try { return shardStorage(shard).rangeFill(byteStart, byteEnd); }
            catch (VectorDataException unopenable) { return null; }
        }

        /// Fetches a byte range within one shard.
        public void prebufferShardRange(int shard, long byteStart, long byteEnd, PrebufferProgress progress) {
            shardStorage(shard).prebufferRange(byteStart, byteEnd, progress);
        }

        /// Fetches every file whole — the degrade path, and the meaning
        /// of a whole-facet request against a source with no chunks.
        public void prebufferWhole(PrebufferProgress progress) {
            if (!isSeries()) { data().prebuffer(progress); return; }
            FacetSeries files = series();
            for (int i = 0; i < files.fileCount(); i++) files.file(i).prebuffer(progress);
        }

        /// Whether every byte this facet can address is resident.
        public boolean isComplete() { return isSeries() ? series().isComplete() : data().isComplete(); }

        /// Cache statistics, folded over every file of a series.
        public CacheStats cacheStats() { return isSeries() ? series().cacheStats() : data().stats(); }

        /// The transfer-chunk size to plan against: the largest across
        /// the files, `0` when nothing is chunked.
        public long chunkSize() { return isSeries() ? series().chunkSize() : data().stats().chunkSize(); }

        /// The offset index for a single-file variable-length facet,
        /// loaded on first use and reused for the life of this handle.
        /// `null` when it cannot be loaded; failures are retried on the
        /// next ask rather than cached.
        private synchronized long[] offsets() {
            if (offsets == null) offsets = publishedOffsets(data(), facet.source(), settings, identity);
            return offsets;
        }

        /// The source locator: the file, plus the slab namespace it
        /// names, if any — the form the per-format mapping reads.
        private String locator() { return facet.source().toString() + (facet.namespace() == null ? "" : ":" + facet.namespace()); }
    }

    // -- Offset indexes --

    /// Planning-only offset loading, mirroring `load_offsets` with
    /// `OffsetSource::Published`: a published sidecar, a persisted
    /// rebuild, or a local mmap walk — never a remote walk, which would
    /// transfer the file in order to price transferring part of it.
    /// Local walks persist their result beside the data, best-effort,
    /// so the walk is paid once rather than per view. Both published
    /// sidecar layouts normalize to record starts: a trailing entry
    /// equal to the payload size is an end-of-data sentinel — no record
    /// can start there — and is dropped, so `prerequisiteBytes` reports
    /// record starts × 8 for either layout. Per **file**: a shard's
    /// sidecar covers its file's records, in file ordinals.
    static long[] publishedOffsets(ByteStorage data, URI source, VectorDataSettings settings, String identity) {
        long payload = data.size();
        String expected = indexExtFor(payload);
        if (data.stats().accessMode() == AccessMode.LOCAL) return loadLocalOffsets(data, source, payload, expected);
        String path = source.getPath() == null ? source.toString() : source.getPath();
        String filename = path.substring(path.lastIndexOf('/') + 1);
        // Probe the width the payload size calls for first — the same
        // rule the local sidecar name uses — so the common case costs
        // one request instead of a guaranteed miss.
        for (String ext : new String[] {expected, "i32".equals(expected) ? "i64" : "i32"}) {
            int width = "i32".equals(ext) ? 4 : 8;
            URI sidecar = URI.create(source.toString().replaceAll("[^/]+$", java.util.regex.Matcher.quoteReplacement("IDXFOR__" + filename + "." + ext)));
            try {
                long[] entries = readEntries(new StorageFactory(settings).open(sidecar, identity), width);
                if (entries == null || entries.length == 0) continue;
                return dropSentinel(entries, payload);
            } catch (VectorDataException missing) { /* try the next sidecar width */ }
        }
        return null;
    }

    /// The local arm of `load_or_build_local_offsets`: read the
    /// size-appropriate sidecar beside the data when it is no older than
    /// the data, else walk the dimension headers and persist the rebuilt
    /// index atomically for the next open.
    private static long[] loadLocalOffsets(ByteStorage data, URI source, long payload, String expected) {
        Path dataPath = Path.of(source);
        int width = "i32".equals(expected) ? 4 : 8;
        Path indexPath = dataPath.resolveSibling("IDXFOR__" + dataPath.getFileName() + "." + expected);
        long[] fromSidecar = loadLocalIndex(indexPath, dataPath, width);
        if (fromSidecar != null) return dropSentinel(fromSidecar, payload);
        long[] walked = walkOffsets(data, source.toString());
        if (walked == null) return null;
        persistIndex(indexPath, walked, width);
        return walked;
    }

    /// Loads a sidecar file beside local data, or `null` when it is
    /// absent, stale (older than the data it describes), or not a whole
    /// number of offsets — all of which send the caller back to the walk
    /// rather than yielding a reader that cannot see the records the
    /// tail describes.
    private static long[] loadLocalIndex(Path indexPath, Path dataPath, int width) {
        try {
            if (!Files.isRegularFile(indexPath)) return null;
            if (Files.getLastModifiedTime(indexPath).compareTo(Files.getLastModifiedTime(dataPath)) < 0) return null;
            byte[] raw = Files.readAllBytes(indexPath);
            if (raw.length == 0 || raw.length % width != 0) return null;
            ByteBuffer bytes = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            long[] entries = new long[raw.length / width];
            for (int at = 0; at < entries.length; at++)
                entries[at] = width == 4 ? Integer.toUnsignedLong(bytes.getInt()) : bytes.getLong();
            return entries;
        } catch (IOException unreadable) { return null; }
    }

    /// Persists a rebuilt index **atomically** beside the data — starts
    /// only, in the width the payload size calls for — via a uniquely
    /// named temporary and a rename, so a concurrent reader sees either
    /// the previous index or the whole new one. Best-effort: a
    /// read-only directory only costs the rebuild.
    private static void persistIndex(Path indexPath, long[] starts, int width) {
        Path temporary = indexPath.resolveSibling("." + indexPath.getFileName() + "."
            + ProcessHandle.current().pid() + "." + PERSIST_SEQUENCE.getAndIncrement() + ".tmp");
        try {
            ByteBuffer bytes = ByteBuffer.allocate(starts.length * width).order(ByteOrder.LITTLE_ENDIAN);
            for (long start : starts) { if (width == 4) bytes.putInt((int) start); else bytes.putLong(start); }
            Files.write(temporary, bytes.array());
            try { Files.move(temporary, indexPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (IOException notAtomic) { Files.move(temporary, indexPath, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ignored) {
            try { Files.deleteIfExists(temporary); } catch (IOException alsoIgnored) { }
        }
    }

    /// Normalizes a parsed sidecar to record starts: a trailing entry
    /// equal to the payload size is unambiguously an end-of-data
    /// sentinel — no record can start there — published by the
    /// `N + 1`-entry layout, and is dropped.
    private static long[] dropSentinel(long[] entries, long payload) {
        return entries.length > 0 && entries[entries.length - 1] == payload
            ? Arrays.copyOf(entries, entries.length - 1) : entries;
    }

    /// Builds record starts by walking the dimension headers of a local
    /// variable-length file that published no sidecar.
    private static long[] walkOffsets(ByteStorage storage, String source) {
        int width;
        try { width = ElementType.forExtension(source).width(); }
        catch (VectorDataException unsupported) { return null; }
        long size = storage.size();
        long offset = 0;
        long[] starts = new long[16];
        int found = 0;
        while (offset + 4 <= size) {
            if (found == starts.length) starts = Arrays.copyOf(starts, found * 2);
            starts[found++] = offset;
            int dim = storage.read(offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if (dim < 0) return null;
            offset += 4 + (long) dim * width;
        }
        return offset == size ? Arrays.copyOf(starts, found) : null;
    }

    private static long[] readEntries(ByteStorage index, int width) {
        long size = index.size();
        if (size == 0 || size % width != 0 || size / width > Integer.MAX_VALUE) return null;
        long[] entries = new long[(int) (size / width)];
        int at = 0;
        int block = 1 << 20;
        for (long offset = 0; offset < size; offset += block) {
            ByteBuffer bytes = index.read(offset, (int) Math.min(block, size - offset)).order(ByteOrder.LITTLE_ENDIAN);
            if (width == 4) while (bytes.remaining() >= 4) entries[at++] = Integer.toUnsignedLong(bytes.getInt());
            else while (bytes.remaining() >= 8) entries[at++] = bytes.getLong();
        }
        return entries;
    }

    /// The sidecar entry width a payload of `fileSize` bytes calls
    /// for: `i32` while every offset still fits one, `i64` beyond.
    /// Shared by the local sidecar name and the remote sidecar probe so
    /// both agree on which file to expect.
    static String indexExtFor(long fileSize) { return fileSize <= Integer.MAX_VALUE ? "i32" : "i64"; }

    private static final java.util.concurrent.atomic.AtomicLong PERSIST_SEQUENCE = new java.util.concurrent.atomic.AtomicLong();

    /// The number of records a file holds, by its format's rule — the
    /// cardinality probe for a bare series entry. A variable-length file
    /// counts through its offsets, rebuilt by a walk when local and
    /// unpublished. `null` when the file cannot be measured.
    static Long recordsIn(String path, ByteStorage storage, VectorDataSettings settings, String identity) {
        String ext = extensionOf(path);
        int width;
        try { width = ElementType.forExtension(path).width(); }
        catch (VectorDataException unsupported) { return null; }
        long total = storage.size();
        if (isVvecExt(ext)) {
            long[] starts = publishedOffsets(storage, URI.create(path), settings, identity);
            return starts == null ? null : (long) starts.length;
        }
        if (isScalarExt(ext)) return total / width;
        if (total < 4) return null;
        int dim = storage.read(0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (dim <= 0 || dim > 1_000_000) return null;
        return total / (4 + (long) dim * width);
    }

    // -- Planning --

    /// Ports `prefetch_plan_on`: resolves the window per interval across
    /// the shards it spans, merges ranges whose fetches would overlap,
    /// and prices the result at chunk granularity. An empty window is a
    /// request for the whole facet — it resolves to every shard's whole
    /// byte range whatever the format, with no ordinal mapping and no
    /// degrade.
    public static PrefetchPlan plan(FacetHandle handle, DSWindow window) {
        long facetBytes = handle.totalSize();
        if (window == null || window.isEmpty()) {
            List<ShardRange> whole = new ArrayList<>();
            List<RangeFill> fills = new ArrayList<>();
            long[] sizes = handle.shardSizes();
            for (int shard = 0; shard < sizes.length; shard++) {
                whole.add(new ShardRange(shard, 0, sizes[shard]));
                RangeFill fill = handle.shardRangeFill(shard, 0, sizes[shard]);
                if (fill != null) fills.add(fill);
            }
            return new PrefetchPlan(whole, whole, fills, 0, false, facetBytes);
        }
        long prerequisite = 0;
        List<ShardRange> requested = new ArrayList<>();
        for (DSWindow.Interval interval : window.intervals()) {
            MappedRanges mapped = recordRangeToBytes(handle, interval.minIncl(), interval.maxExcl());
            // One unmappable interval makes the whole request a full
            // download; a partial plan beside it would understate what
            // is about to happen.
            if (mapped == null) return new PrefetchPlan(List.of(), List.of(), List.of(), prerequisite, true, facetBytes);
            prerequisite = Math.max(prerequisite, mapped.prerequisiteBytes());
            requested.addAll(mapped.ranges());
        }
        // A file that cannot service byte ranges has no partial fetch to
        // offer: honouring a window that touches it means the whole file.
        for (ShardRange range : requested) {
            ByteStorage storage = handle.shardStorage(range.shard());
            if (!storage.rangeCapable() && !storage.isComplete())
                return new PrefetchPlan(List.of(), List.of(), List.of(), prerequisite, true, facetBytes);
        }
        List<ShardRange> issued = coalesce(requested, handle.chunkSize());
        List<RangeFill> fills = new ArrayList<>();
        for (ShardRange range : issued) {
            RangeFill fill = handle.shardRangeFill(range.shard(), range.start(), range.end());
            if (fill != null) fills.add(fill);
        }
        return new PrefetchPlan(requested, issued, fills, prerequisite, false, facetBytes);
    }

    /// The window a facet declares for itself, in facet ordinals: a
    /// suffix on a single source, or the `window:` field a series
    /// carries — the descriptor holds either as `window`. This is the
    /// window a whole-profile prebuffer honours per facet. A malformed
    /// window is an error, not an absent one.
    public static DSWindow facetDeclaredWindow(FacetDescriptor facet) {
        String window = facet.window();
        if (window == null || window.isBlank()) return DSWindow.ALL;
        try { return DSWindow.parse(window); }
        catch (VectorDataException malformed) {
            throw new VectorDataException("facet '" + facet.name() + "': window '" + window + "' is malformed: " + malformed.getMessage());
        }
    }

    /// Refuses a plan that would fetch the whole facet, unless allowed.
    /// The message carries the size, because that is the decision the
    /// caller is being asked to make.
    public static void checkFallback(String facet, PrefetchPlan plan, WholeFacetFallback fallback) {
        if (plan.degradesToFullDownload() && fallback == WholeFacetFallback.REFUSE)
            throw new VectorDataException("facet '" + facet + "': the requested window cannot be resolved for this format, "
                + "so honouring it means fetching the whole facet (" + plan.facetBytes() + " bytes). "
                + "Pass WholeFacetFallback.ALLOW to accept that.");
    }

    /// Merges byte ranges whose fetches would overlap: the same chunk,
    /// or adjacent chunks, which are contiguous on the device. A whole
    /// chunk of gap is not bridged, because bridging it fetches a chunk
    /// nobody asked for. Ranges in different shards never merge — they
    /// are in different files. With `chunkSize` `0` — local storage,
    /// which has no chunks — merging falls back to plain byte overlap
    /// or adjacency.
    static List<ShardRange> coalesce(List<ShardRange> ranges, long chunkSize) {
        List<ShardRange> sorted = new ArrayList<>();
        for (ShardRange range : ranges) if (!range.isEmpty()) sorted.add(range);
        if (sorted.size() < 2) return sorted;
        sorted.sort(Comparator.comparingInt(ShardRange::shard).thenComparingLong(ShardRange::start).thenComparingLong(ShardRange::end));
        List<ShardRange> merged = new ArrayList<>();
        for (ShardRange next : sorted) {
            ShardRange current = merged.isEmpty() ? null : merged.get(merged.size() - 1);
            if (current != null && current.shard() == next.shard() && joins(current.end(), next.start(), chunkSize)) {
                merged.set(merged.size() - 1, new ShardRange(current.shard(), current.start(), Math.max(current.end(), next.end())));
            } else merged.add(next);
        }
        return merged;
    }

    private static boolean joins(long currentEnd, long nextStart, long chunkSize) {
        if (chunkSize > 0) return nextStart / chunkSize <= (currentEnd - 1) / chunkSize + 1;
        return nextStart <= currentEnd;
    }

    /// Maps a record range to byte ranges: one range for a single file,
    /// one per shard the range spans for a series, each mapped by the
    /// format's rule against its own file. Prerequisite bytes are
    /// summed, not maxed: each shard's index is a separate read, and a
    /// window touching three shards pays for three.
    static MappedRanges recordRangeToBytes(FacetHandle handle, long winStart, long winEnd) {
        if (winEnd <= winStart) return null;
        if (!handle.isSeries()) {
            MappedRange mapped = mapInFile(handle.locator(), winStart, winEnd, handle.data(), handle::offsets);
            return mapped == null ? null : new MappedRanges(List.of(ShardRange.whole(mapped.byteStart(), mapped.byteEnd())), mapped.prerequisiteBytes());
        }
        FacetSeries series = handle.series();
        List<ShardRange> ranges = new ArrayList<>();
        long prerequisite = 0;
        for (Shards.SubWindow part : series.shards().decompose(winStart, winEnd)) {
            int file = series.fileIndexOfShard(part.shard());
            MappedRange mapped = mapInFile(series.shards().entries().get(part.shard()).locator(), part.fileLo(), part.fileHi(),
                series.file(file), () -> series.publishedOffsets(file));
            if (mapped == null) return null;
            prerequisite += mapped.prerequisiteBytes();
            ranges.add(new ShardRange(part.shard(), mapped.byteStart(), mapped.byteEnd()));
        }
        return ranges.isEmpty() ? null : new MappedRanges(ranges, prerequisite);
    }

    /// Maps a record range **within one file** to bytes, for formats
    /// where that mapping is computable from the file alone. `offsets`
    /// is consulted only for variable-length formats, and is a supplier
    /// so the caller decides where the index comes from: a facet
    /// handle's cache for a single file, the series' per-file cache for
    /// a shard.
    static MappedRange mapInFile(String path, long lo, long hi, ByteStorage storage, Supplier<long[]> offsets) {
        if (hi <= lo) return null;
        String ext = extensionOf(path);
        int width;
        try { width = ElementType.forExtension(SourceSpec.stripNamespace(path)).width(); }
        catch (VectorDataException unsupported) { return null; }
        long total = storage.size();
        if (isVvecExt(ext)) {
            // Exact, not estimated, and a window past the last record
            // ends at the file, not at a record that does not exist.
            long[] starts = offsets.get();
            if (starts == null || starts.length == 0 || lo >= starts.length) return null;
            long byteStart = starts[(int) lo];
            long byteEnd = hi >= starts.length ? total : starts[(int) hi];
            // The index had to be read whole to answer this at all.
            return byteEnd <= byteStart ? null : new MappedRange(byteStart, byteEnd, starts.length * 8L);
        }
        if (isScalarExt(ext)) {
            long byteStart = Math.min(saturatedMultiply(lo, width), total);
            long byteEnd = Math.min(saturatedMultiply(hi, width), total);
            return byteStart >= byteEnd ? null : new MappedRange(byteStart, byteEnd, 0);
        }
        // The uniform-stride mapping: `dim` read from the header at byte
        // 0 — the same first-chunk read any reader does on first access,
        // so nothing to report as a prerequisite.
        if (total < 4) return null;
        int dim = storage.read(0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (dim <= 0 || dim > 1_000_000) return null; // sanity vs corrupt header
        long bytesPerRecord = 4 + (long) dim * width;
        long byteStart = Math.min(saturatedMultiply(lo, bytesPerRecord), total);
        long byteEnd = Math.min(saturatedMultiply(hi, bytesPerRecord), total);
        return byteStart >= byteEnd ? null : new MappedRange(byteStart, byteEnd, 0);
    }

    private static long saturatedMultiply(long a, long b) {
        try { return Math.multiplyExact(a, b); } catch (ArithmeticException overflow) { return Long.MAX_VALUE; }
    }

    static String extensionOf(String source) {
        String value = SourceSpec.stripNamespace(source).toLowerCase(Locale.ROOT);
        int slash = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        if (slash >= 0) value = value.substring(slash + 1);
        int query = value.indexOf('?');
        if (query >= 0) value = value.substring(0, query);
        int dot = value.lastIndexOf('.');
        return dot < 0 ? "" : value.substring(dot + 1);
    }

    /// Only the `*vvec` extensions carry variable-length records. In
    /// this catalog format, `ivec`/`ivecs` records are length-qualified
    /// but fixed throughout — ground-truth neighbor files — and map at
    /// the uniform header stride like every other xvec.
    static boolean isVvecExt(String ext) { return ext.contains("vvec"); }

    static boolean isScalarExt(String ext) { return ext.matches("u8|i8|u16|i16|u32|i32|u64|i64"); }
}
