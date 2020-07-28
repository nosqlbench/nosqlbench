package io.nosqlbench.virtdata.userlibs.streams;

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
