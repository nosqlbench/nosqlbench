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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * <p>This class provides <em>cursor-like</em> access to a set of data from
 * a binary file using Java nio buffers. Calling {@link #next()} causes
 * the next record to be loaded, after which the getter methods return
 * the loaded values. You must call next before access each record's fields.</p>
 *
 * <p>The {@link #next(int)} method may be used for absolute offset access.
 * In this mode, no thread safety is imposed, as there is no chance of the
 * internal buffer's position to affect the result.</p>
 *
 * <p>Buffers may be accessed as shared or not. If</p>
 *
 */
public class TokenMapFileAPIService {

//    public static ThreadLocal<Map<String, BinaryCursorForTokenCycle>> tl_cll =
//            ThreadLocal.withInitial(HashMap::new);
//
    private final int recordCount;
    private final ByteBuffer buffer;
    private final int RECORD_LEN = Long.BYTES * 2;

    private final int recordPosition;

    private long token;
    private final int TOKEN_OFFSET = 0;
    private long cycle;
    private final int CYCLE_OFFSET = Long.BYTES;

    private final boolean loopdata;

    /**
     * Create a new binary cursor for data in a binary file which consists of a (long,long) tuple of
     * token values (murmur3 partitioner tokens) and cycle values that correspond to them. The cycles
     * are the ones responsible for producing the associated token values.
     * @param datafile The data file to read from
     * @param loopdata Whether or not to loop around to the beginning of the data. For positional reads this is also
     *                 modulo-based, such that relatively prime sizes and increments will loop not simply repeat
     *                 values at the start of the buffer
     * @param instanced Whether or not to provide an instanced view into the byte buffer, where each thread can have
     *                  its own read tracking state
     * @param ascending Whether to reverse the order othe long,long tuples when the file is read.
     */
    public TokenMapFileAPIService(String datafile, boolean loopdata, boolean instanced, boolean ascending) {
        this.loopdata = loopdata;
        buffer = TokenMapFileSharedBuffers.getByteBuffer(datafile,instanced,ascending).asReadOnlyBuffer();
        this.recordCount = buffer.capacity() / RECORD_LEN;
        this.recordPosition = 0;
    }


    public synchronized void next() {
        try {
            token = buffer.getLong();
            cycle = buffer.getLong();
        } catch (BufferUnderflowException bue) {
            if (loopdata) {
                buffer.position(0);
                next();
            }
            else {
                throw bue;
            }
        }
    }

    /**
     * Do a read of [token,cycle] record without incremental read state.
     * @param position The logical record within the buffer to read
     */
    public void next(int position) {

        if (loopdata) {
            position = (position % recordCount) * RECORD_LEN;
        }
        token = buffer.getLong(position+TOKEN_OFFSET);
        cycle = buffer.getLong(position+CYCLE_OFFSET);
    }

    public long getToken() {
        return token;
    }

    public long getCycle() {
        return cycle;
    }

//    public static BinaryCursorForTokenCycle get(String mapname) {
//        BinaryCursorForTokenCycle cursorLongLong = tl_cll.get().get(mapname);
//        return cursorLongLong;
//    }

}
