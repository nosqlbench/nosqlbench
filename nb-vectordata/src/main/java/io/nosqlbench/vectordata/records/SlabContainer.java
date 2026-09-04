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

import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.internal.ByteStorage;
import io.nosqlbench.vectordata.internal.SlabIndex;

import java.nio.ByteBuffer;

/// One slab backing part or all of a facet, read through the same
/// storage the vector readers use rather than a mapping of its own.
/// That is what keeps a slab facet incremental like every other
/// format: opening it costs its tail, and reading a record costs that
/// record's page — fetched and verified as a byte range by the chunked
/// source, never the file.
final class SlabContainer {
    private final ByteStorage storage;
    private final String label;
    private final String namespace;
    private volatile SlabIndex index;
    private volatile boolean absent;

    SlabContainer(ByteStorage storage, String label, String namespace) {
        this.storage = storage; this.label = label; this.namespace = namespace;
    }

    ByteStorage storage() { return storage; }
    String label() { return label; }

    /// The same storage under a sibling namespace, with its own index.
    SlabContainer namespace(String name) { return new SlabContainer(storage, label + ":" + name, name); }

    /// The ordinal index, built from the tail on first use. An absent
    /// namespace is a normal state and reads as empty.
    private SlabIndex index() {
        SlabIndex current = index;
        if (current != null || absent) return current;
        synchronized (this) {
            if (index == null && !absent) {
                SlabIndex built;
                try { built = SlabIndex.read(storage, namespace, label); }
                catch (VectorDataException e) { throw RecordException.container(e.getMessage()); }
                if (built == null) absent = true; else index = built;
            }
            return index;
        }
    }

    long count() { SlabIndex i = index(); return i == null ? 0 : i.total(); }

    /// The record at this container's own ordinal `local`.
    byte[] record(long local) {
        SlabIndex index = index();
        if (index == null) throw RecordException.outOfBounds(local);
        Integer page = index.pageOf(local);
        if (page == null) throw RecordException.outOfBounds(local);
        long offset = index.pageOffset(page), start = index.pageStartOrdinal(page);
        ByteBuffer bytes = page(offset);
        long within = local - start;
        long count = SlabIndex.recordCount(bytes, label);
        if (within >= count) throw RecordException.outOfBounds(local);
        try { return SlabIndex.record(bytes, (int) within, label); }
        catch (VectorDataException e) { throw RecordException.container(label + ": record " + local + ": " + e.getMessage()); }
    }

    /// The page at `offset`: its own header states its size.
    private ByteBuffer page(long offset) {
        Long size = SlabIndex.pageSizeAt(storage, offset);
        if (size == null) throw RecordException.container(label + ": short page header at " + offset);
        if (size > Integer.MAX_VALUE) throw RecordException.container(label + ": page of " + size + " bytes is too large to buffer");
        try { return storage.read(offset, size.intValue()); }
        catch (VectorDataException e) { throw RecordException.container(label + ": read " + size + " bytes at " + offset + ": " + e.getMessage()); }
    }
}
