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
package io.nosqlbench.vectordata;

import java.util.List;

/// What a prefetch would fetch, before any of it moves. Returned by
/// [TestDataView#prefetchPlan].
///
/// - `requestedRanges` — the byte ranges the window resolved to, before
///   merging: what the caller actually asked for. Each is qualified by
///   the shard it lies in, because across a series the same byte offset
///   exists in every file; a single-file facet is shard `0`.
/// - `byteRanges` — the ranges that will be issued, after merging those
///   whose fetches would overlap: one request each. Ranges in different
///   shards never merge — they are in different files.
/// - `fills` — chunk-level cost of each issued range. Empty when the
///   facet has no chunks (local storage, which is free by definition).
/// - `prerequisiteBytes` — bytes that had to be read before the window
///   could be resolved at all: the offset index, for variable-length
///   formats; zero for uniform-stride formats. Across a series each
///   touched file's index is a separate read, so they sum. Reported
///   whether or not it was paid this time — the index is cached on the
///   view's facet handle, so a plan and the fetch that follows it load
///   it once.
/// - `degradesToFullDownload` — set when a window was asked for and
///   could not be resolved for this format or storage, so honouring the
///   request means fetching the whole facet. A prefetch with *no*
///   window is a request for the whole facet, not a fallback, and never
///   sets this.
/// - `facetBytes` — size of the facet, every shard included, for reading
///   the degrade case against.
public record PrefetchPlan(List<ShardRange> requestedRanges, List<ShardRange> byteRanges, List<RangeFill> fills,
                           long prerequisiteBytes, boolean degradesToFullDownload, long facetBytes) {

    public PrefetchPlan {
        requestedRanges = List.copyOf(requestedRanges);
        byteRanges = List.copyOf(byteRanges);
        fills = List.copyOf(fills);
    }

    /// Bytes that will cross the network.
    public long bytesToFetch() {
        if (degradesToFullDownload) return facetBytes;
        return fills.stream().mapToLong(RangeFill::bytesToFetch).sum();
    }

    /// Chunks that still have to be fetched.
    public int chunksToFetch() { return fills.stream().mapToInt(RangeFill::chunksToFetch).sum(); }

    /// Requests that will be issued. Lower than the interval count when
    /// intervals were merged; higher when a window crossed a shard seam.
    public int requests() { return byteRanges.size(); }

    /// Bytes fetched beyond what was asked for. Two sources, counted
    /// together: chunk granularity, and gaps bridged by merging nearby
    /// intervals. Both are bytes crossing the wire that nobody asked
    /// for.
    public long overfetchBytes() {
        long spanned = fills.stream().mapToLong(fill -> Math.max(0, fill.alignedEnd() - fill.alignedStart())).sum();
        long asked = requestedRanges.stream().mapToLong(ShardRange::length).sum();
        return Math.max(0, spanned - asked);
    }

    /// Whether everything the window covers is already resident, so a
    /// prefetch would do nothing.
    public boolean isResident() { return !degradesToFullDownload && fills.stream().allMatch(RangeFill::isResident); }
}
