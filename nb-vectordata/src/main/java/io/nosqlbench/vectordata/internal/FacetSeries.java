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
import io.nosqlbench.vectordata.RangeFill;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// The files behind a multi-file facet, and the ordinal model that maps
/// records onto them.
///
/// Ordinal concerns count per **shard** and storage concerns count per
/// **file**: two shards sliced from one file share one storage, one
/// sidecar, one offset index, and one fetch. Anything byte-shaped here
/// iterates files; anything ordinal-shaped iterates shards.
///
/// Files open lazily — reading ordinal 0 must not open shard 400 — and
/// the number held open at once is capped by a budget derived from the
/// process descriptor limit. Eviction releases this series' reference;
/// the storage registry holds files weakly, so a file nobody else holds
/// closes on its own.
public final class FacetSeries {
    private static final System.Logger LOGGER = System.getLogger(FacetSeries.class.getName());
    private static final long FD_FRACTION = 4;
    private static final int FD_FLOOR = 8;

    private final Shards shards;
    private final List<String> filePaths;
    private final List<URI> fileUris;
    private final int[] fileOfShard;
    private final VectorDataSettings settings;
    private final String identity;
    private final ByteStorage[] opened;
    private final long[][] offsets;
    /// Open files, most-recently-used last, bounded by `cap`.
    private final Deque<Integer> lru = new ArrayDeque<>();
    private final int cap;
    private volatile long totalBytes = -1;

    public FacetSeries(Shards shards, VectorDataSettings settings, String identity) {
        this(shards, settings, identity, openFileCap());
    }

    /// As the public constructor, with the descriptor budget supplied —
    /// what lets eviction be exercised at a size a test can build.
    FacetSeries(Shards shards, VectorDataSettings settings, String identity, int budget) {
        this.shards = shards; this.settings = settings; this.identity = identity;
        Map<String, Integer> index = new LinkedHashMap<>();
        List<String> paths = new ArrayList<>();
        fileOfShard = new int[shards.entries().size()];
        for (int shard = 0; shard < fileOfShard.length; shard++) {
            String source = shards.entries().get(shard).source();
            Integer at = index.get(source);
            if (at == null) { at = paths.size(); index.put(source, at); paths.add(source); }
            fileOfShard[shard] = at;
        }
        filePaths = List.copyOf(paths);
        fileUris = paths.stream().map(URI::create).toList();
        opened = new ByteStorage[paths.size()];
        offsets = new long[paths.size()][];
        if (paths.size() > budget)
            LOGGER.log(System.Logger.Level.WARNING, "facet spans " + paths.size() + " files but only " + budget
                + " may stay open at once; readers crossing that many files will reopen them. Raise VECTORDATA_SHARD_FD_CAP or ulimit -n.");
        // A series never needs more descriptors than it has files.
        cap = Math.min(budget, Math.max(1, paths.size()));
    }

    /// How many files a series may hold open at once: a quarter of the
    /// process descriptor soft limit, floor 8, or `VECTORDATA_SHARD_FD_CAP`
    /// when set. Derived rather than fixed because a constant is wrong
    /// in both directions — it strands descriptors on a generous host
    /// and thrashes on a constrained one — and is invisible to the
    /// operator who raised `ulimit -n` precisely to make this work.
    public static int openFileCap() { return resolveOpenFileCap(System.getenv("VECTORDATA_SHARD_FD_CAP"), fdSoftLimit()); }

    static int resolveOpenFileCap(String env, Long softLimit) {
        if (env != null) {
            try { int forced = Integer.parseInt(env.trim()); if (forced > 0) return forced; }
            catch (NumberFormatException ignored) { }
        }
        long soft = softLimit == null ? 1024 : softLimit;
        return (int) Math.max(FD_FLOOR, soft / FD_FRACTION);
    }

    private static Long fdSoftLimit() {
        try {
            if (java.lang.management.ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.UnixOperatingSystemMXBean unix)
                return unix.getMaxFileDescriptorCount();
        } catch (Throwable unavailable) { /* not a Unix runtime, or the bean is inaccessible */ }
        return null;
    }

    public Shards shards() { return shards; }
    public int fileCount() { return filePaths.size(); }
    public String filePath(int file) { return filePaths.get(file); }
    public URI fileUri(int file) { return fileUris.get(file); }
    public VectorDataSettings settings() { return settings; }
    public String identity() { return identity; }

    /// Which file a shard is drawn from.
    public int fileIndexOfShard(int shard) {
        if (shard < 0 || shard >= fileOfShard.length) throw new VectorDataException("shard " + shard + " out of range");
        return fileOfShard[shard];
    }

    /// The storage of one file, opened on first ask. A file that will
    /// not open is named: a declared shard that is absent is a broken
    /// facet, never a shorter one.
    public ByteStorage file(int file) {
        synchronized (lru) {
            if (opened[file] != null) { touch(file); return opened[file]; }
        }
        ByteStorage storage;
        try { storage = new StorageFactory(settings).open(fileUris.get(file), identity); }
        catch (VectorDataException failure) {
            throw new VectorDataException("open shard file '" + filePaths.get(file) + "': " + failure.getMessage(), failure);
        }
        synchronized (lru) {
            if (opened[file] == null) opened[file] = storage;
            touch(file);
            while (lru.size() > cap) {
                Integer victim = lru.pollFirst();
                if (victim == null) break;
                if (victim == file) { lru.addLast(victim); break; }
                opened[victim] = null;
            }
            return opened[file];
        }
    }

    private void touch(int file) { lru.remove(file); lru.addLast(file); }

    /// Files currently held open by this series.
    public int openFileCount() {
        synchronized (lru) { int count = 0; for (ByteStorage storage : opened) if (storage != null) count++; return count; }
    }

    /// The record-offset index of one **file**, loaded on first use and
    /// reused; two shards sliced from one file load it once between
    /// them. `null` when it cannot be loaded without fetching.
    public long[] publishedOffsets(int file) {
        synchronized (offsets) {
            if (offsets[file] == null) offsets[file] = Prefetcher.publishedOffsets(file(file), fileUris.get(file), settings, identity);
            return offsets[file];
        }
    }

    /// The byte range a shard can address within its file — its whole
    /// file for a whole-file shard, its window's bytes for a sliced one
    /// — or `null` when the format cannot map it.
    public long[] shardByteExtent(int shard) {
        Shards.Entry entry = shards.entries().get(shard);
        int file = fileIndexOfShard(shard);
        Prefetcher.MappedRange mapped = Prefetcher.mapInFile(entry.locator(), entry.fileBase(), entry.fileBase() + entry.len(),
            file(file), () -> publishedOffsets(file));
        return mapped == null ? null : new long[] {mapped.byteStart(), mapped.byteEnd()};
    }

    /// Every file's bytes summed, opening each on the first ask. Fails
    /// naming the file that will not open.
    public long tryTotalSize() {
        long cached = totalBytes;
        if (cached >= 0) return cached;
        long sum = 0;
        for (int file = 0; file < filePaths.size(); file++) sum += file(file).size();
        totalBytes = sum;
        return sum;
    }

    /// As [#tryTotalSize], answering `0` for a series that cannot be
    /// sized — the only answer this signature allows, which is why
    /// every caller that can report a failure uses the other.
    public long totalSize() {
        try { return tryTotalSize(); }
        catch (VectorDataException failure) {
            LOGGER.log(System.Logger.Level.ERROR, "facet size unavailable: " + failure.getMessage());
            return 0;
        }
    }

    /// Bytes per shard, in shard order; `0` for a shard whose file will
    /// not open.
    public long[] shardSizes() {
        long[] sizes = new long[fileOfShard.length];
        for (int shard = 0; shard < sizes.length; shard++) {
            try { sizes[shard] = file(fileOfShard[shard]).size(); }
            catch (VectorDataException unopenable) { sizes[shard] = 0; }
        }
        return sizes;
    }

    /// Whether **every byte this facet can address** is resident — not
    /// every byte of every file it draws from. A facet slicing a tenth
    /// of a large file would otherwise report incomplete forever unless
    /// the other nine tenths were downloaded: bytes it can never
    /// address, fetched to satisfy a predicate about them.
    public boolean isComplete() {
        try {
            for (int shard = 0; shard < fileOfShard.length; shard++) {
                ByteStorage storage = file(fileOfShard[shard]);
                long[] extent = shardByteExtent(shard);
                if (extent == null) { if (!storage.isComplete()) return false; continue; }
                RangeFill fill = storage.rangeFill(extent[0], extent[1]);
                if (fill != null && !fill.isResident()) return false;
            }
            return true;
        } catch (VectorDataException unopenable) { return false; }
    }

    /// Whether every file is local.
    public boolean isLocal() {
        for (int file = 0; file < filePaths.size(); file++) if (file(file).stats().accessMode() != AccessMode.LOCAL) return false;
        return true;
    }

    /// Statistics folded over every file: bytes and chunk counts sum,
    /// the chunk size is the largest in play, completeness is the
    /// conjunction, and the access mode is the [AccessMode#weakest] —
    /// the promise true of every read this facet will serve.
    public CacheStats cacheStats() {
        List<AccessMode> modes = new ArrayList<>();
        long total = 0, cached = 0, chunk = 0, hits = 0, misses = 0;
        boolean complete = true;
        for (int file = 0; file < filePaths.size(); file++) {
            CacheStats stats = file(file).stats();
            modes.add(stats.accessMode());
            total += stats.totalBytes(); cached += stats.cachedBytes(); chunk = Math.max(chunk, stats.chunkSize());
            hits += stats.hits(); misses += stats.misses(); complete &= stats.complete();
        }
        return new CacheStats(AccessMode.weakest(modes), total, cached, chunk, hits, misses, complete);
    }

    /// The largest transfer-chunk size across the files; `0` when none
    /// is chunked.
    public long chunkSize() {
        long chunk = 0;
        for (int file = 0; file < filePaths.size(); file++) chunk = Math.max(chunk, file(file).stats().chunkSize());
        return chunk;
    }
}
