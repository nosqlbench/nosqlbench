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

/// What fetching a byte range would actually cost, at chunk
/// granularity. The unit of fetch is a chunk, not a byte, so a range's
/// real cost is rarely the range's length: a 4 KiB window against 8 MiB
/// chunks is 8 MiB, and a window whose chunks are already resident is
/// free. Both facts have to be visible *before* the fetch, or a caller
/// cannot tell an incremental warm-up from a full download.
///
/// `firstChunk` and `lastChunk` are inclusive; `alignedStart` and
/// `alignedEnd` are the byte range the fetch actually spans once
/// widened to chunk boundaries — always a superset of what was asked
/// for.
public record RangeFill(int firstChunk, int lastChunk, long chunkSize, int chunks, int chunksResident,
                        long alignedStart, long alignedEnd) {

    /// Chunks that still have to be fetched.
    public int chunksToFetch() { return Math.max(0, chunks - chunksResident); }

    /// Bytes that will cross the network, at chunk granularity.
    public long bytesToFetch() { return chunksToFetch() * chunkSize; }

    /// Bytes fetched beyond the requested range because chunks are the
    /// granularity. This is the number that tells a caller its
    /// scattered single-record prefetches are really a full download.
    public long overfetchBytes(long requestedStart, long requestedEnd) {
        long requested = Math.max(0, requestedEnd - requestedStart);
        long spanned = Math.max(0, alignedEnd - alignedStart);
        return Math.max(0, spanned - requested);
    }

    /// Whether every chunk covering the range is already resident.
    public boolean isResident() { return chunksResident >= chunks; }
}
