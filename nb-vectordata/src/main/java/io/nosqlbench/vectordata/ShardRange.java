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

/// A half-open byte range `[start, end)` within a named shard of a facet.
///
/// The shard index is not decoration: across a series the same byte
/// offset exists in every file, so a range without one names no bytes.
/// A single-file facet is shard `0`, which is exactly the old meaning
/// under a new name — see [#whole].
public record ShardRange(int shard, long start, long end) {

    /// A range in the only shard of a single-file facet.
    public static ShardRange whole(long start, long end) { return new ShardRange(0, start, end); }

    /// Bytes spanned by this range.
    public long length() { return Math.max(0, end - start); }

    /// Whether the range names no bytes.
    public boolean isEmpty() { return end <= start; }

    @Override public String toString() { return "shard " + shard + " [" + start + ".." + end + ")"; }
}
