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
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.LongFunction;

/**
 * Reads ivec files with random access, using the input to specify the record number.
 */
@ThreadSafeMapper
@Categories(Category.readers)
public class FVecReader implements LongFunction<float[]> {

    private final MappedByteBuffer bb;
    private final int dimensions;
    private final int reclen;
    private final long filesize;
    private final Path path;
    private final int reclim;

    public FVecReader(String pathname) {
        this(pathname,0,0);
    }
    public FVecReader(String pathname, int expectedDimensions, int recordLimit) {
        Content<?> src = NBIO.fs().search(pathname).one();
        this.path = src.asPath();
        try {
            FileChannel channel = FileChannel.open(this.path, StandardOpenOption.READ, StandardOpenOption.SPARSE);
            this.filesize = channel.size();
            this.bb = channel.map(FileChannel.MapMode.READ_ONLY, 0, filesize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.dimensions = Integer.reverseBytes(bb.getInt(0));
        if(expectedDimensions>0 && expectedDimensions!=dimensions) {
            throw new RuntimeException("Invalid dimensions specified for '" +pathname + "', found " + dimensions + ", but expected " + expectedDimensions);
        }
        int datalen = (dimensions * Float.BYTES);
        this.reclen = Integer.BYTES + datalen;
        int totalRecords = (int) (filesize/reclen);
        if (recordLimit > totalRecords) {
            throw new RuntimeException("Specified record range of " + recordLimit + ", but file only contained " + totalRecords + " total");
        }
        this.reclim = recordLimit==0? totalRecords : recordLimit;
        if ((filesize % reclen)!=0) {
            throw new RuntimeException("The filesize (" + filesize + ") for '" + pathname + "' must be a multiple of the reclen (" + reclen + ")");
        }
    }

    @Override
    public float[] apply(long value) {
        int recordIdx = (int) (value % reclim);
        int recpos = recordIdx*reclen;
        int recdim = Integer.reverseBytes(bb.getInt(recpos));
        if(recdim!=dimensions) {
            throw new RuntimeException("dimensions are not uniform for fvec file '" + this.path.toString() + "', found dim " + recdim + " at record " + value);
        }
        var vbuf = new byte[dimensions*Float.BYTES];
        bb.get(recpos + Integer.BYTES, vbuf);

        FloatBuffer fbuf=ByteBuffer.wrap(vbuf).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        var vectors = new float[dimensions];
        fbuf.get(vectors);
        return vectors;
    }
}
