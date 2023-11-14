/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import io.nosqlbench.engine.api.activityapi.cyclelog.inputs.cyclelog.CanFilterResultValue;
import io.nosqlbench.engine.api.activityapi.output.Output;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

/**
 * Implements a convenient target buffer for Marker data that can be used
 * to create nio ByteBuffers easily.
 *
 * This is not thread-safe. It is not meant to be used by concurrent callers.
 *
 * It is recommended to use the {@link AutoCloseable} method to ensure that
 * partial runs are flushed automatically. Access the buffer for read via either
 * the {@link #toByteBuffer()} or the {@link #toSegmentsReadable()} methods will
 * automatically {@link #flush()} and invalidate the writable buffer, so further writes
 * will be deemed invalid and will cause an exception to be thrown.
 */
public class CycleResultsRLEBufferTarget implements Output,CanFilterResultValue {
    private final static Logger logger = LogManager.getLogger(CycleResultsRLEBufferTarget.class);

    public final static int BYTES = Long.BYTES + Long.BYTES + Byte.BYTES;

    private ByteBuffer buf;
    private long lastCycle = Long.MIN_VALUE;
    private long lastResult = Integer.MIN_VALUE;
    private long runlength = 0L;
    private boolean flushed = false;
    private final long count=0L;
    private final long min=Long.MAX_VALUE;
    private Predicate<ResultReadable> filter;

    /**
     * Create a buffer with the provided ByteBuffer.
     *
     * @param buf the source data
     */
    public CycleResultsRLEBufferTarget(ByteBuffer buf) {
        this.buf = buf;
    }

//    /**
//     * Create a buffer with an automatic capacity of around 1MiB.
//     */
//    public CycleResultsRLEBufferTarget() {
//        this(1024 * 1024);
//    }
//
    /**
     * Create a target RLE buffer for the specified getCount in memory,
     * rounded to the nearest record getCount.
     *
     * @param elementCount The number of elements to buffer.
     */
    public CycleResultsRLEBufferTarget(int elementCount) {
        this(ByteBuffer.allocate(elementCount * BYTES));
    }

    /**
     * Convert the contents of this RLE buffer to a readable and
     * invalide it for writing.
     * @return a CycleResultRLEBuffer
     */
    public CycleResultsRLEBufferReadable toSegmentsReadable() {
        flush();
        ByteBuffer readable = buf.duplicate();
        readable.flip();
        return new CycleResultsRLEBufferReadable(readable);
    }

    public ByteBuffer toByteBuffer() {
        flush();
        ByteBuffer bb = buf.duplicate();
        bb.flip();
        return bb;
    }

    /**
     * Record new cycle result data in the buffer, and optionally flush any
     * completed RLE segments to the internal ByteBuffer.
     *
     * @param cycle The cycle number being marked.
     * @param result the result ordinal
     *
     * @throws RuntimeException if the buffer has been converted to a readable form
     * @return false if there was no more room in the buffer for another tuple, true otherwise.
     */
    @Override
    public boolean onCycleResult(long cycle, int result) {
        ResultReadableWrapper resultReadableWrapper = new ResultReadableWrapper(result);
        if (filter!=null && !filter.test(resultReadableWrapper)) {
            return true;
        }
        if (cycle != lastCycle + 1 || lastResult != result) {
            if (lastCycle != Long.MIN_VALUE) {
                checkpoint(lastCycle + 1 - runlength, lastCycle + 1, lastResult);
            }
        }

        lastCycle = cycle;
        lastResult = result;
        runlength++;
        flushed = false;
        return true;
    }

    private void checkpoint(long istart, long iend, long lastResult) {
        if (buf.remaining()==0) {
            buf=resize(buf);
        }
        if (lastResult > Byte.MAX_VALUE) {
            throw new RuntimeException("Unable to encode result values greater than Byte.MAX_VALUE.");
        }
        if (lastCycle>=0) {
            buf.putLong(istart).putLong(iend).put((byte) lastResult);
            runlength = 0;
            return;
        }
        if (lastCycle!=Long.MIN_VALUE) {
            throw new RuntimeException("Unable to encode cycle values less than 0");
        } {
            logger.trace("checkpoint with no active RLE segment data.");
        }
    }

    private static class ResultReadableWrapper implements ResultReadable {
        private final int result;
        ResultReadableWrapper(int result) {
            this.result = result;
        }
        public int getResult() { return result; }
    }

    private ByteBuffer resize(ByteBuffer buf) {
        ByteBuffer doubled=ByteBuffer.allocate(buf.capacity()*2);
        buf.flip();
        doubled.put(buf);
        logger.warn("resized buffer to " + doubled + " to ensure capacity.");
        return doubled;
    }

    public int getRawBufferCapacity() {
        return buf.capacity();
    }

    public int getRecordCapacity() {
        return buf.capacity() / BYTES;
    }

    /**
     * Flushes any partial data that was submitted (an incomplete run of results,
     * for example), to the internal ByteBuffer, and marks flushed status.
     *
     * @return the getCount of the current buffer, in bytes.
     */
    private int flush() {
        if (!flushed) {
            checkpoint(lastCycle + 1 - runlength, lastCycle + 1, lastResult);
            flushed = true;
        }
        return buf.position();
    }

    @Override
    public void close() {
        flush();
    }

    public boolean onCycleResult(CycleResult cycleResult) {
        return this.onCycleResult(cycleResult.getCycle(),cycleResult.getResult());
    }

    @Override
    public void setFilter(Predicate<ResultReadable> filter) {
        this.filter = filter;
    }
}
