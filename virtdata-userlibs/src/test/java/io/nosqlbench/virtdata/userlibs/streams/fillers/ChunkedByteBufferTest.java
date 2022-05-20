package io.nosqlbench.virtdata.userlibs.streams.fillers;

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
