package io.nosqlbench.virtdata.userlibs.streams.fillers;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_bytebuffer.HashedToByteBuffer;

import java.nio.ByteBuffer;

public class ByteBufferSource extends LongFunctionIterable<ByteBuffer> {

    public ByteBufferSource(long startCycle, long endCycle, int bufsize) {
        super(startCycle, endCycle, new HashedToByteBuffer(bufsize));
    }
}
