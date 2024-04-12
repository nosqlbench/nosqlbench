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

package io.nosqlbench.datamappers.functions.rainbow;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class TokenMapFileSharedBuffers {
    public final static TokenMapFileSharedBuffers INSTANCE = new TokenMapFileSharedBuffers();
    private final static Map<String,ByteBuffer> BUFFERS = new HashMap<>();
    private TokenMapFileSharedBuffers() {}

    /**
     * Find and load the {@link ByteBuffer} which can be read at the specified
     * location. This will only be loaded into memory once. All callers will
     * get access to the same logical source data. Whether or not the caller
     * gets its own buffer tracking state (see {@link java.nio.Buffer}).
     * If each caller will use the Buffer API for incremental reads, where
     * callers could possibly read the same records, then separate instanced
     * buffers are advised.
     *
     * <p>However, if you are planning to use position-oriented access to the
     * buffer only, then it is not necessary to ask for instanced buffers. In
     * some usage patterns, it may be desirable to provide a single logical
     * view of buffer reader position across multiple threads. In this case,
     * setting instanced to false is necessary.</p>
     *
     * @param filename The location of the source data for the buffer.
     * @param instanced If true, each caller gets a wrapped buffer object with its own
     *                  tracking state
     * @param ascending
     * @return An instance of a ByteBuffer
     */
    public synchronized static ByteBuffer getByteBuffer(String filename, boolean instanced, boolean ascending) {
        ByteBuffer foundBuffer = BUFFERS.computeIfAbsent(filename, f->load(f,ascending));
        return instanced ? foundBuffer.asReadOnlyBuffer() : foundBuffer;
    }

    private static ByteBuffer load(String filename, boolean ascending) {
        try {
            RandomAccessFile image = new RandomAccessFile(filename, "rw");
            ByteBuffer mbb = image.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, image.length());
            if (!ascending) {
                int RECORD_LEN = Long.BYTES * 2;
                ByteBuffer descendingByteBuffer = ByteBuffer.allocate(mbb.capacity());
                for (int i = mbb.capacity()-RECORD_LEN; i >= 0 ; i-=RECORD_LEN) {
                    long v1 = mbb.getLong(i);
                    long v2 = mbb.getLong(i+Long.BYTES);
                    descendingByteBuffer.putLong(v1);
                    descendingByteBuffer.putLong(v2);
                }
                mbb = descendingByteBuffer;
            }
            return mbb;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
