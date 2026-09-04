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
package io.nosqlbench.vectordata.records;

import io.nosqlbench.vectordata.internal.ByteStorage;

import java.util.ArrayList;
import java.util.List;

/// A facet of ordinal-addressed records, before a codec is chosen.
///
/// Holds the containers behind the facet — one for a single file, one
/// per shard for a series — and answers a facet ordinal by finding the
/// container that owns it and asking for its local ordinal. Each shard
/// of a slab series is an ordinary slab based at zero, so the local
/// ordinal is what a container is asked for and the global base never
/// reaches it. Counts come from the containers themselves, not the
/// shard declaration: a slab knows how many records it holds, and
/// asking it keeps this from being a second place the answer lives.
///
/// Applying a codec yields a typed reader — [#decode] is the curry:
/// the facet supplies ordinals and bytes, the codec supplies the type.
public final class RecordFacet {
    private final String name;
    private final List<SlabContainer> containers;
    private volatile long[] starts;

    private RecordFacet(String name, List<SlabContainer> containers) { this.name = name; this.containers = List.copyOf(containers); }

    /// A facet over its files' storage — one for a single file, one per
    /// shard in ordinal order for a series — under a namespace, or the
    /// default when `namespace` is `null`.
    public static RecordFacet over(String facet, String namespace, List<ByteStorage> shards) {
        if (shards.isEmpty()) throw RecordException.notAContainer("facet '" + facet + "'");
        List<SlabContainer> containers = new ArrayList<>(shards.size());
        for (int shard = 0; shard < shards.size(); shard++)
            containers.add(new SlabContainer(shards.get(shard), shards.size() == 1 ? "facet '" + facet + "'" : "facet '" + facet + "' shard " + shard, namespace));
        return new RecordFacet(facet, containers);
    }

    /// The facet's name, for diagnostics.
    public String name() { return name; }

    /// Records across every container.
    public long count() { long[] offsets = offsets(); return offsets[offsets.length - 1]; }

    private long[] offsets() {
        long[] current = starts;
        if (current != null) return current;
        long[] built = new long[containers.size() + 1];
        for (int i = 0; i < containers.size(); i++) built[i + 1] = built[i] + containers.get(i).count();
        starts = built;
        return built;
    }

    /// One record's bytes — the escape hatch beneath every codec.
    /// Reading one record from a remote facet costs one page, not the
    /// file.
    public byte[] recordBytes(long ordinal) {
        long[] offsets = offsets();
        for (int i = 0; i < containers.size(); i++)
            if (ordinal >= 0 && ordinal < offsets[i + 1]) return containers.get(i).record(ordinal - offsets[i]);
        throw RecordException.outOfBounds(ordinal);
    }

    /// A sibling namespace of this facet as a facet of its own: the
    /// schema, the layout copy, and the survey report are records in
    /// named namespaces of the same containers, so reading them is the
    /// same operation against a different name.
    public RecordFacet namespace(String namespace) {
        List<SlabContainer> siblings = new ArrayList<>(containers.size());
        for (SlabContainer c : containers) siblings.add(c.namespace(namespace));
        return new RecordFacet(name + ":" + namespace, siblings);
    }

    /// Applies a codec, producing a typed reader.
    public <T> Records<T> decode(RecordCodec<T> codec) { return new Records<>(this, codec); }
}
