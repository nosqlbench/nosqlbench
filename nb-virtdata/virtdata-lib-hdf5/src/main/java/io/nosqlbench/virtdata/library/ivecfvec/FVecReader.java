/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.ivecfvec;

import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

/**
 * Reads fvec files with random access, using the input to specify the record number.
 * This is used for testing with generated KNN test data which is uniform in dimensions and neighborhood size.
 * While it is possible to specify different dimensioned vectors per record, this is not supported, since this
 * function honors the pure-function behavior of other NB binding functions. This requires uniform record structure for random access.
 */
@ThreadSafeMapper
@Categories(Category.readers)
public class FVecReader implements LongFunction<float[]>, AutoCloseable {

    private final int dimensions;
    private final int reclen;
    private final long filesize;
    private final Path path;
    private final int reclim;

    /**
     * Read the fvec file, determining the record size from the first record.
     * @param pathname The location of the fvec file
     */
    @Example({"FvecReader('testfile.fvec')","Create a reader for float vectors, detecting the dimensions and dataset size automatically."})
    public FVecReader(String pathname) {
        this(pathname, 0, 0);
    }

    @Example({"FvecReader('testfile.fvec', 46, 12)","Create a reader for float vectors, asserting 46 dimensions and limit total records to 12."})
    public FVecReader(String pathname, int expectedDimensions, int recordLimit) {
        Content<?> src = NBIO.fs().search(pathname).one();
        this.path = src.asPath();

        try {
            FileChannel initChannel = FileChannel.open(this.path, StandardOpenOption.READ);
            this.filesize = initChannel.size();

            // Read dimensions from the first 4 bytes
            ByteBuffer headerBuffer = ByteBuffer.allocate(Integer.BYTES);
            initChannel.position(0);
            initChannel.read(headerBuffer);
            headerBuffer.flip();
            this.dimensions = Integer.reverseBytes(headerBuffer.getInt());
            initChannel.close();

            if(expectedDimensions > 0 && expectedDimensions != dimensions) {
                throw new RuntimeException("Invalid dimensions specified for '" + pathname +
                    "', found " + dimensions + ", but expected " + expectedDimensions);
            }

            int datalen = (dimensions * Float.BYTES);
            this.reclen = Integer.BYTES + datalen;

            long totalRecords = filesize / reclen;
            if (totalRecords > Integer.MAX_VALUE) {
                throw new RuntimeException("File contains more than Integer.MAX_VALUE records");
            }

            if (recordLimit > totalRecords) {
                throw new RuntimeException("Specified record range of " + recordLimit +
                    ", but file only contained " + totalRecords + " total");
            }

            this.reclim = recordLimit == 0 ? (int)totalRecords : recordLimit;

            if ((filesize % reclen) != 0) {
                throw new RuntimeException("The filesize (" + filesize +
                    ") for '" + pathname + "' must be a multiple of the reclen (" + reclen + ")");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Use a thread-local to store the file channel without closing it between calls
    private static final ThreadLocal<Map<Path, FileChannel>> THREAD_CHANNELS =
        ThreadLocal.withInitial(() -> new HashMap<>());

    private FileChannel getOrCreateChannel() throws IOException {
        // Get the thread-local map of channels
        Map<Path, FileChannel> channelMap = THREAD_CHANNELS.get();

        // Try to get an existing channel for this path
        FileChannel channel = channelMap.get(path);

        // Create a new channel if needed
        if (channel == null || !channel.isOpen()) {
            channel = FileChannel.open(path, StandardOpenOption.READ);
            channelMap.put(path, channel);
        }

        return channel;
    }

    @Override
    public float[] apply(long value) {
        int recordIdx = (int) (value % reclim);
        long recpos = (long)recordIdx * reclen;

        try {
            FileChannel channel = getOrCreateChannel();
            ByteBuffer headerBuffer = ByteBuffer.allocate(Integer.BYTES);
            ByteBuffer vectorBuffer = ByteBuffer.allocate(dimensions * Float.BYTES)
                .order(ByteOrder.BIG_ENDIAN);

            // Read record dimensions
            headerBuffer.clear();
            channel.position(recpos);
            channel.read(headerBuffer);
            headerBuffer.flip();
            int recdim = Integer.reverseBytes(headerBuffer.getInt());

            if(recdim != dimensions) {
                throw new RuntimeException("dimensions are not uniform for fvec file '" +
                    this.path + "', found dim " + recdim + " at record " + value);
            }

            // Read vector data
            vectorBuffer.clear();
            channel.read(vectorBuffer);
            vectorBuffer.flip();

            float[] data = new float[dimensions];
            for (int i = 0; i < dimensions; i++) {
                int intBits = Integer.reverseBytes(vectorBuffer.getInt());
                data[i] = Float.intBitsToFloat(intBits);
            }
            return data;

        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        // Close and remove channel for this path
        Map<Path, FileChannel> channelMap = THREAD_CHANNELS.get();
        FileChannel channel = channelMap.remove(path);
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

}
