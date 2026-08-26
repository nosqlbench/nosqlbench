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
import io.nosqlbench.vectordata.ByteRange;
import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.ElementType;
import io.nosqlbench.vectordata.FacetDescriptor;
import io.nosqlbench.vectordata.PrefetchPlan;
import io.nosqlbench.vectordata.RangeFill;
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

/// Resolves record-coordinate prefetch windows to byte ranges and
/// plans their chunk-level cost, mirroring the `vectordata-rs` prefetch
/// surface. Three per-format mappings:
///
/// - **xvec** (uniform stride): `4 + dim × elemSize`, with `dim` read
///   from the header at byte 0 — the same first-chunk read any reader
///   does on first access.
/// - **vvec** (variable length): the sibling `IDXFOR__` offset index,
///   loaded whole and cached on the facet handle; on local storage a
///   facet without a sidecar is walked once instead.
/// - **scalar** (raw packed values): `ordinal × elemSize` exactly,
///   with no header.
///
/// `null` from the mapping means "cannot window this" — an unsupported
/// format, a corrupt header, an index that cannot be loaded, or an
/// empty range. That is the answer, not a failure, and
/// [WholeFacetFallback] decides what a caller does with it.
public final class Prefetcher {
    private Prefetcher() { }

    /// A record range resolved to bytes, and what resolving it cost.
    record MappedRange(long byteStart, long byteEnd, long prerequisiteBytes) { }

    /// Record start offsets, one per record. Both published sidecar
    /// layouts normalize to this: a trailing entry equal to the payload
    /// size is an end-of-data sentinel — no record can start there —
    /// and is dropped at parse, so `prerequisiteBytes` reports record
    /// starts × 8 for either layout.
    private record CachedOffsets(long[] starts) { }

    /// Per-facet handle holding the lazily opened data storage and the
    /// offset index once loaded. The index cache is scoped to the view
    /// that owns this handle: holding a view is how a caller says it
    /// will ask about its facets repeatedly, and dropping it is how
    /// they say they are done.
    public static final class FacetHandle {
        private final FacetDescriptor facet;
        private final VectorDataSettings settings;
        private final String identity;
        private volatile ByteStorage data;
        private volatile CachedOffsets offsets;

        public FacetHandle(FacetDescriptor facet, VectorDataSettings settings, String identity) {
            this.facet = facet; this.settings = settings; this.identity = identity;
        }

        public ByteStorage data() {
            ByteStorage current = data;
            if (current == null) {
                synchronized (this) {
                    if (data == null) data = new StorageFactory(settings).open(facet.source(), identity);
                    current = data;
                }
            }
            return current;
        }

        /// The offset index for a variable-length facet, loaded on
        /// first use and reused for the life of this handle. `null`
        /// when it cannot be loaded; failures are retried on the next
        /// ask rather than cached.
        private synchronized CachedOffsets offsets() {
            if (offsets == null) offsets = loadOffsets();
            return offsets;
        }

        /// Planning-only offset loading, mirroring `load_offsets` with
        /// `OffsetSource::Published`: a published sidecar, a persisted
        /// rebuild, or a local mmap walk — never a remote walk, which
        /// would transfer the facet in order to price transferring part
        /// of it. Local walks persist their result beside the data,
        /// best-effort, so the walk is paid once rather than per view.
        private CachedOffsets loadOffsets() {
            long payload = data().size();
            String expected = indexExtFor(payload);
            if (data().stats().accessMode() == AccessMode.LOCAL) return loadLocalOffsets(payload, expected);
            URI source = facet.source();
            String path = source.getPath() == null ? source.toString() : source.getPath();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            // Probe the width the payload size calls for first — the
            // same rule the local sidecar name uses — so the common
            // case costs one request instead of a guaranteed miss.
            for (String ext : new String[] {expected, "i32".equals(expected) ? "i64" : "i32"}) {
                int width = "i32".equals(ext) ? 4 : 8;
                URI sidecar = URI.create(source.toString().replaceAll("[^/]+$", java.util.regex.Matcher.quoteReplacement("IDXFOR__" + filename + "." + ext)));
                try {
                    long[] entries = readEntries(new StorageFactory(settings).open(sidecar, identity), width);
                    if (entries == null || entries.length == 0) continue;
                    return new CachedOffsets(dropSentinel(entries, payload));
                } catch (VectorDataException missing) { /* try the next sidecar width */ }
            }
            return null;
        }

        /// The local arm of `load_or_build_local_offsets`: read the
        /// size-appropriate sidecar beside the data when it is no older
        /// than the data, else walk the dimension headers and persist
        /// the rebuilt index atomically for the next open.
        private CachedOffsets loadLocalOffsets(long payload, String expected) {
            Path dataPath = Path.of(facet.source());
            int width = "i32".equals(expected) ? 4 : 8;
            Path indexPath = dataPath.resolveSibling("IDXFOR__" + dataPath.getFileName() + "." + expected);
            long[] fromSidecar = loadLocalIndex(indexPath, dataPath, width);
            if (fromSidecar != null) return new CachedOffsets(dropSentinel(fromSidecar, payload));
            long[] walked = walkOffsets();
            if (walked == null) return null;
            persistIndex(indexPath, walked, width);
            return new CachedOffsets(walked);
        }

        /// Loads a sidecar file beside local data, or `null` when it is
        /// absent, stale (older than the data it describes), or not a
        /// whole number of offsets — all of which send the caller back
        /// to the walk rather than yielding a reader that cannot see
        /// the records the tail describes.
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

        /// Persists a rebuilt index **atomically** beside the data —
        /// starts only, in the width the payload size calls for — via a
        /// uniquely named temporary and a rename, so a concurrent
        /// reader sees either the previous index or the whole new one.
        /// Best-effort: a read-only directory only costs the rebuild.
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

        /// Normalizes a parsed sidecar to record starts: a trailing
        /// entry equal to the payload size is unambiguously an
        /// end-of-data sentinel — no record can start there — published
        /// by the `N + 1`-entry layout, and is dropped.
        private static long[] dropSentinel(long[] entries, long payload) {
            return entries.length > 0 && entries[entries.length - 1] == payload
                ? Arrays.copyOf(entries, entries.length - 1) : entries;
        }

        /// Builds record starts by walking the dimension headers of a
        /// local variable-length file that published no sidecar.
        private long[] walkOffsets() {
            int width;
            try { width = ElementType.forExtension(facet.source().toString()).width(); }
            catch (VectorDataException unsupported) { return null; }
            ByteStorage storage = data();
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
    }

    /// The sidecar entry width a payload of `fileSize` bytes calls
    /// for: `i32` while every offset still fits one, `i64` beyond.
    /// Shared by the local sidecar name and the remote sidecar probe so
    /// both agree on which file to expect.
    static String indexExtFor(long fileSize) { return fileSize <= Integer.MAX_VALUE ? "i32" : "i64"; }

    private static final java.util.concurrent.atomic.AtomicLong PERSIST_SEQUENCE = new java.util.concurrent.atomic.AtomicLong();

    /// Ports `prefetch_plan_on`: resolves the window per interval,
    /// merges ranges whose fetches would overlap, and prices the
    /// result at chunk granularity. An empty window is a request for
    /// the whole facet — it resolves to the whole byte range whatever
    /// the format, with no ordinal mapping and no degrade.
    public static PrefetchPlan plan(FacetHandle handle, DSWindow window) {
        ByteStorage storage = handle.data();
        long facetBytes = storage.size();
        if (window == null || window.isEmpty()) {
            List<ByteRange> whole = List.of(new ByteRange(0, facetBytes));
            RangeFill fill = storage.rangeFill(0, facetBytes);
            return new PrefetchPlan(whole, whole, fill == null ? List.of() : List.of(fill), 0, false, facetBytes);
        }
        long prerequisite = 0;
        List<ByteRange> requested = new ArrayList<>();
        for (DSWindow.Interval interval : window.intervals()) {
            MappedRange mapped = recordRangeToBytes(handle, interval.minIncl(), interval.maxExcl());
            // One unmappable interval makes the whole request a full
            // download; a partial plan beside it would understate what
            // is about to happen.
            if (mapped == null) return new PrefetchPlan(List.of(), List.of(), List.of(), prerequisite, true, facetBytes);
            prerequisite = Math.max(prerequisite, mapped.prerequisiteBytes());
            requested.add(new ByteRange(mapped.byteStart(), mapped.byteEnd()));
        }
        // A source that cannot service byte ranges has no partial fetch
        // to offer: honouring a window against it means the whole file.
        if (!storage.rangeCapable() && !storage.isComplete())
            return new PrefetchPlan(List.of(), List.of(), List.of(), prerequisite, true, facetBytes);
        List<ByteRange> issued = coalesce(requested, storage.stats().chunkSize());
        List<RangeFill> fills = new ArrayList<>();
        for (ByteRange range : issued) {
            RangeFill fill = storage.rangeFill(range.start(), range.end());
            if (fill != null) fills.add(fill);
        }
        return new PrefetchPlan(requested, issued, fills, prerequisite, false, facetBytes);
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
    /// nobody asked for. With `chunkSize` `0` — local storage, which
    /// has no chunks — merging falls back to plain byte overlap or
    /// adjacency.
    static List<ByteRange> coalesce(List<ByteRange> ranges, long chunkSize) {
        List<ByteRange> sorted = new ArrayList<>();
        for (ByteRange range : ranges) if (range.end() > range.start()) sorted.add(range);
        if (sorted.size() < 2) return sorted;
        sorted.sort(Comparator.comparingLong(ByteRange::start).thenComparingLong(ByteRange::end));
        List<ByteRange> merged = new ArrayList<>();
        for (ByteRange next : sorted) {
            if (!merged.isEmpty() && joins(merged.get(merged.size() - 1).end(), next.start(), chunkSize)) {
                ByteRange current = merged.get(merged.size() - 1);
                merged.set(merged.size() - 1, new ByteRange(current.start(), Math.max(current.end(), next.end())));
            } else merged.add(next);
        }
        return merged;
    }

    private static boolean joins(long currentEnd, long nextStart, long chunkSize) {
        if (chunkSize > 0) return nextStart / chunkSize <= (currentEnd - 1) / chunkSize + 1;
        return nextStart <= currentEnd;
    }

    /// Maps a record range to a byte range, for formats where that
    /// mapping is computable from the file alone. See the class
    /// documentation for the per-format policy.
    static MappedRange recordRangeToBytes(FacetHandle handle, long winStart, long winEnd) {
        if (winEnd <= winStart) return null;
        String ext = extensionOf(handle.facet.source().toString());
        if (isVvecExt(ext)) return vvecRangeToBytes(handle, winStart, winEnd);
        int width;
        try { width = ElementType.forExtension(handle.facet.source().toString()).width(); }
        catch (VectorDataException unsupported) { return null; }
        ByteStorage storage = handle.data();
        long total = storage.size();
        if (isScalarExt(ext)) {
            long byteStart = Math.min(saturatedMultiply(winStart, width), total);
            long byteEnd = Math.min(saturatedMultiply(winEnd, width), total);
            return byteStart >= byteEnd ? null : new MappedRange(byteStart, byteEnd, 0);
        }
        if (total < 4) return null;
        int dim = storage.read(0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (dim <= 0 || dim > 1_000_000) return null; // sanity vs corrupt header
        long bytesPerRecord = 4 + (long) dim * width;
        long byteStart = Math.min(saturatedMultiply(winStart, bytesPerRecord), total);
        long byteEnd = Math.min(saturatedMultiply(winEnd, bytesPerRecord), total);
        // A uniform-stride mapping costs a 4-byte header read, which
        // every reader pays on first access anyway — nothing to report.
        return byteStart >= byteEnd ? null : new MappedRange(byteStart, byteEnd, 0);
    }

    /// Maps through the offset index: exact, not estimated, and a
    /// window past the last record ends at the file, not at a record
    /// that does not exist.
    private static MappedRange vvecRangeToBytes(FacetHandle handle, long winStart, long winEnd) {
        CachedOffsets cached = handle.offsets();
        if (cached == null || cached.starts().length == 0) return null;
        long[] starts = cached.starts();
        long count = starts.length;
        if (winStart >= count) return null;
        long byteStart = starts[(int) winStart];
        long byteEnd = winEnd >= count ? handle.data().size() : starts[(int) winEnd];
        if (byteEnd <= byteStart) return null;
        // The index had to be read whole to answer this at all. Record
        // starts × 8, so both sidecar layouts report the same figure.
        return new MappedRange(byteStart, byteEnd, starts.length * 8L);
    }

    private static long saturatedMultiply(long a, long b) {
        try { return Math.multiplyExact(a, b); } catch (ArithmeticException overflow) { return Long.MAX_VALUE; }
    }

    private static String extensionOf(String source) {
        String value = source.toLowerCase(Locale.ROOT);
        int slash = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        if (slash >= 0) value = value.substring(slash + 1);
        int query = value.indexOf('?');
        if (query >= 0) value = value.substring(0, query);
        int dot = value.lastIndexOf('.');
        return dot < 0 ? "" : value.substring(dot + 1);
    }

    private static boolean isVvecExt(String ext) { return ext.contains("vvec") || ext.equals("ivec") || ext.equals("ivecs"); }

    private static boolean isScalarExt(String ext) { return ext.matches("u8|i8|u16|i16|u32|i32|u64|i64"); }
}
