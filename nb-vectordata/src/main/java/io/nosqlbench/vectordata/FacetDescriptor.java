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

import java.net.URI;
import java.util.List;
import java.util.Map;

/// Resolved manifest facet, including custom application-defined
/// fields.
///
/// `source` is the facet's one file, resolved against its manifest, or
/// `null` for a multi-file [Series]: a caller written before sharding
/// must fail visibly rather than read a series as its first file — or,
/// for the uniform form, as the `NNNN` *pattern*, which names no file at
/// all. `window` is the **facet** window, in facet ordinals: a suffix on
/// a single source, or the `window:` field a series carries, having no
/// single source to put one on. `namespace` selects a namespace within
/// a slab source (`m.slab:content`), `null` for the default.
public record FacetDescriptor(String name, URI source, String window, Map<String, Object> attributes, Series series, String namespace) {

    public FacetDescriptor(String name, URI source, String window, Map<String, Object> attributes) {
        this(name, source, window, attributes, null, null);
    }

    public FacetDescriptor(String name, URI source, String window, Map<String, Object> attributes, Series series) {
        this(name, source, window, attributes, series, null);
    }

    /// A facet declared as a series of files. `entries` are the source
    /// strings in ordinal order, resolved against the manifest: one
    /// `NNNN` pattern for the uniform form, one per shard for the
    /// explicit form — each carrying its own namespace, window, or
    /// `=count` suffix exactly as written, because an entry window is in
    /// that *file's* ordinals and must stay with the entry it bounds.
    public record Series(List<String> entries, boolean declaredAsArray, Long shardStride, Integer shardCount, Long recordCount) {
        public Series { entries = List.copyOf(entries); }
    }

    /// Whether this facet spans more than one file.
    public boolean isSeries() { return series != null; }
}
