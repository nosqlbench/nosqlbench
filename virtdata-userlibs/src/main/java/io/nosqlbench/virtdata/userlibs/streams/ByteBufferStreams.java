package io.nosqlbench.virtdata.userlibs.streams;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer.HashedToByteBuffer;
import io.nosqlbench.virtdata.userlibs.streams.fillers.ChunkedByteBuffer;
import io.nosqlbench.virtdata.userlibs.streams.fillers.LongFunctionIterable;
import io.nosqlbench.virtdata.userlibs.streams.pojos.ByteBufferObject;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class ByteBufferStreams {


    public static Iterable<ByteBufferObject> byteBufferObjects(long startCycle, long endCycle, int bufsize) {
        HashedToByteBuffer htbb = new HashedToByteBuffer(bufsize);
        LongFunctionIterable<ByteBufferObject> bbi = new LongFunctionIterable<>(startCycle, endCycle, l -> new ByteBufferObject(htbb.apply(l)));
        return bbi;
//        LongFunctionIterable<ByteBuffer> byteBuffers = new LongFunctionIterable<>(0L, new HashedToByteBuffer(bufsize));
    }

    public static Iterable<ByteBuffer> byteBuffers(long startCycle, long endCycle, int bufsize) {
        HashedToByteBuffer htbb = new HashedToByteBuffer(bufsize);
        LongFunctionIterable<ByteBuffer> bbi = new LongFunctionIterable<>(startCycle, endCycle, htbb);
        return bbi;
    }

    public static Iterable<ByteBuffer> partialByteBuffers(int startCycle, int endCycle, int bufSize) {
        Iterable<ByteBuffer> byteBuffers = byteBuffers(startCycle, endCycle, bufSize);
        return new ChunkedByteBuffer(byteBuffers);
    }

    private final static class ByteBufferObjectIterable implements Iterable<ByteBufferObject> {
        @NotNull
        @Override
        public Iterator<ByteBufferObject> iterator() {
            return new ByteBufferObjectIterator();
        }
        private final static class ByteBufferObjectIterator implements Iterator<ByteBufferObject> {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public ByteBufferObject next() {
                return null;
            }
        }
    }
}
