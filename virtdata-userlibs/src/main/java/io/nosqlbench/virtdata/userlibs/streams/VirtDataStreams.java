package io.nosqlbench.virtdata.userlibs.streams;

import java.nio.ByteBuffer;

public class VirtDataStreams {
    public static Iterable<ByteBuffer> byteBuffers(long startCycle, long endCycle, int bufsize) {
        return ByteBufferStreams.byteBuffers(startCycle, endCycle, bufsize);
    }
}
