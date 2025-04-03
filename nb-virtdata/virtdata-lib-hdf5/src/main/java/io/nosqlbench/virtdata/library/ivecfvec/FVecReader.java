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
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.LongFunction;

/**
 * Reads ivec files with random access, using the input to specify the record number.
 */
@ThreadSafeMapper
@Categories(Category.readers)
public class FVecReader implements LongFunction<float[]>, AutoCloseable {

    private final FileChannel channel;
    private final int dimensions;
    private final int reclen;
    private final long filesize;
    private final Path path;
    private final int reclim;
    private final ByteBuffer headerBuffer = ByteBuffer.allocate(Integer.BYTES);
    private final ByteBuffer vectorBuffer;

    public FVecReader(String pathname) {
        this(pathname, 0, 0);
    }

    public FVecReader(String pathname, int expectedDimensions, int recordLimit) {
        Content<?> src = NBIO.fs().search(pathname).one();
        this.path = src.asPath();
        try {
            this.channel = FileChannel.open(this.path, StandardOpenOption.READ);
            this.filesize = channel.size();

            // Read dimensions from the first 4 bytes
            channel.position(0);
            channel.read(headerBuffer);
            headerBuffer.flip();
            this.dimensions = Integer.reverseBytes(headerBuffer.getInt());

            if(expectedDimensions > 0 && expectedDimensions != dimensions) {
                throw new RuntimeException("Invalid dimensions specified for '" + pathname +
                    "', found " + dimensions + ", but expected " + expectedDimensions);
            }

            int datalen = (dimensions * Float.BYTES);
            this.reclen = Integer.BYTES + datalen;

            // Pre-allocate buffer for vector data
            this.vectorBuffer = ByteBuffer.allocate(datalen);

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

    @Override
    public float[] apply(long value) {
        int recordIdx = (int) (value % reclim);
        long recpos = (long)recordIdx * reclen;

        try {
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

            float[] vectors = new float[dimensions];
            vectorBuffer.order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(vectors);
            return vectors;

        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }
}
