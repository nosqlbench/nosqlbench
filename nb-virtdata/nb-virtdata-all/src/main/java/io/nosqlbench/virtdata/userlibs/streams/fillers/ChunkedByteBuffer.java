/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.userlibs.streams.fillers;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * <H2>Synopsis</H2>
 *
 * <p>This iterator breaks some rules! The intent of this iterator is to
 * make it programmatically easy to consume raw data in ByteBuffer mode
 * without throwing away generated data. The efficiency loss of throwing
 * away data is variable, but two reason are used as premise for this
 * approach: 1) generation is not free and 2) the buf size mismatch
 * between producer and consumer could be very high, amplifying the generation
 * cost of data.</p>
 *
 * <p>Yet, the iterable pattern is very easy to integrate with, and so long
 * as the user understands what this iterable does, it should make things
 * easier by far than *not* having this helper class.</p>
 *
 * <p>All this iterator does is wrap another iterator and cache the current
 * ByteBuffer, re-issuing it until it is consumed fully.</p>
 *
 * <H2>Usage Patterns</H2>
 *
 * This is intended to be called as an interactive data source, where another
 * iteration controls flow.
 *
 * <H2>Warnings</H2>
 *
 * This class is not thread safe. Either wrap it in a ThreadLocal with appropriate
 * initialization for concurrent use, or make sure no concurrent access occurs.
 */
public class ChunkedByteBuffer implements Iterable<ByteBuffer> {

    private final Iterable<ByteBuffer> source;

    public ChunkedByteBuffer(Iterable<ByteBuffer> source) {
        this.source = source;
    }

    @NotNull
    @Override
    public Iterator<ByteBuffer> iterator() {
        return new ChunkedByteBufferIterator(source.iterator());
    }

    private static class ChunkedByteBufferIterator implements Iterator<ByteBuffer> {

        private final Iterator<ByteBuffer> sourceIter;
        private ByteBuffer buf;
        int chunks;

        public ChunkedByteBufferIterator(Iterator<ByteBuffer> iterator) {
            this.sourceIter = iterator;
        }

        @Override
        public boolean hasNext() {

            if (buf != null && buf.remaining() <= 0) {
                buf = null;
            }

            if (buf == null) {
                if (sourceIter.hasNext()) {
                    buf = sourceIter.next();
                    chunks++;
                }
            }
            return buf != null;
        }

        @Override
        public ByteBuffer next() {
            return buf;
        }

        public String toString() {
            return "chunk " + chunks + " / position " + (buf==null ? "NULL" : buf.position());
        }

    }
}
