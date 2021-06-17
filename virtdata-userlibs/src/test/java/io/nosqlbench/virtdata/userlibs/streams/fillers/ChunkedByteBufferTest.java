package io.nosqlbench.virtdata.userlibs.streams.fillers;

import io.nosqlbench.virtdata.userlibs.streams.ByteBufferStreams;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class ChunkedByteBufferTest {

    @Test
    public void testChunkedByteBufferx10() {
        Iterable<ByteBuffer> byteBuffers = ByteBufferStreams.partialByteBuffers(0, 100, 100);
        byte[] buf37 = new byte[37];
        int count = 0;
        for (ByteBuffer byteBuffer : byteBuffers) {
            byteBuffer.get(buf37,0,Math.min(buf37.length,byteBuffer.remaining()));
            count++;
        }
        assertThat(count).isEqualTo(300); // Each 100 byte buffer takes 3 rounds to consume


    }

}