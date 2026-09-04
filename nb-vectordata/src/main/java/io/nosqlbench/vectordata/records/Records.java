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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/// A record facet with a codec applied: values by ordinal.
public final class Records<T> implements Iterable<T> {
    private final RecordFacet facet;
    private final RecordCodec<T> codec;

    Records(RecordFacet facet, RecordCodec<T> codec) { this.facet = facet; this.codec = codec; }

    /// The number of records.
    public long count() { return facet.count(); }

    /// The record at `ordinal`, decoded.
    public T get(long ordinal) { return codec.decode(facet.recordBytes(ordinal)); }

    /// Every record in order, decoded lazily.
    public Stream<T> stream() { return LongStream.range(0, count()).mapToObj(this::get); }

    @Override public Iterator<T> iterator() {
        long count = count();
        return new Iterator<>() {
            private long next;
            @Override public boolean hasNext() { return next < count; }
            @Override public T next() { if (next >= count) throw new NoSuchElementException(); return get(next++); }
        };
    }
}
