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

package io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleResultsRLEBufferTarget;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleSpanResults;
import io.nosqlbench.engine.api.util.SimpleConfig;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import io.nosqlbench.engine.api.activityapi.cyclelog.inputs.cyclelog.CanFilterResultValue;
import io.nosqlbench.engine.api.activityapi.output.Output;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.function.Predicate;

/**
 * A {@link Output} that writes cycles and results to an RLE-based file format.
 *
 * This output creates a file on disk and appends one or more (long,long,byte)
 * tuples to it as buffering extents are filled. This tuple format represents
 * the closed-open interval of cycles and the result associated with them.
 * The file is expected to contain only cycle ranges in order.
 *
 * <p>It <em>is</em> valid for RLE segments to be broken apart into contiguous
 * ranges. Any implementation should treat this as normal.
 */
public class CycleLogOutput implements Output, CanFilterResultValue {

    // For use in allocating file data, etc
    private final static Logger logger = LogManager.getLogger(CycleLogOutput.class);
    private MappedByteBuffer mbb;
    private RandomAccessFile file;
    //    private FileBufferConfig config;
    private CycleResultsRLEBufferTarget targetBuffer;
    private final int extentSizeInSpans;
    private final File outputFile;
    private Predicate<ResultReadable> filter;

    public CycleLogOutput(Activity activity) {

        SimpleConfig conf = new SimpleConfig(activity, "output");
        this.extentSizeInSpans = conf.getInteger("extentSize").orElse(1000);
        this.outputFile = new File(conf.getString("file").orElse(activity.getAlias()) + ".cyclelog");


        targetBuffer = new CycleResultsRLEBufferTarget(extentSizeInSpans);
        removeIfPresent(outputFile);
    }

    public CycleLogOutput(File outputFile, int extentSizeInSpans) {
        this.extentSizeInSpans = extentSizeInSpans;
        this.outputFile = outputFile;
        targetBuffer = new CycleResultsRLEBufferTarget(extentSizeInSpans);
        removeIfPresent(outputFile);
    }

    private void removeIfPresent(File filename) {
        try {
            if (Files.deleteIfExists(filename.toPath())) {
                logger.warn("removed extant file '" + filename + "'");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public boolean onCycleResult(long completedCycle, int result) {
        CycleSpanResults cycleResults = new CycleSpanResults(completedCycle, completedCycle + 1, result);
//        if (filter==null || filter.test(cycleResults)) {
        onCycleResultSegment(cycleResults);
//        }
        return true;
    }

    @Override
    public void onCycleResultSegment(CycleResultsSegment segment) {
        for (CycleResult cycleResult : segment) {
            if (filter == null || filter.test(cycleResult)) {
                boolean buffered = targetBuffer.onCycleResult(cycleResult);
                if (!buffered) {
                    flush();
                    targetBuffer = new CycleResultsRLEBufferTarget(extentSizeInSpans);
                    boolean bufferedAfterFlush = targetBuffer.onCycleResult(cycleResult);
                    if (!bufferedAfterFlush) {
                        throw new RuntimeException("Failed to record result in new target buffer");
                    }
                }
            }
        }
    }

    private void flush() {
        ByteBuffer nextFileExtent = targetBuffer.toByteBuffer();
        logger.debug("RLE result extent is " + nextFileExtent.remaining() + " bytes ("
                + (nextFileExtent.remaining() / CycleResultsRLEBufferTarget.BYTES)
                + ") tuples");
        int targetCapacity = (mbb == null ? 0 : mbb.capacity()) + nextFileExtent.remaining();
        logger.trace(() -> "ensuring capacity for " + targetCapacity);
        this.ensureCapacity(targetCapacity);
        mbb.put(nextFileExtent);
        mbb.force();
        logger.trace("extent appended");
        logger.trace(() -> "mbb position now at " + mbb.position());

    }

    @Override
    public synchronized void close() throws Exception {
        try {
            flush();
            if (file != null) {
                file.getFD().sync();
                file.close();
                file = null;
            }
        } catch (Throwable t) {
            logger.error("Error while closing CycleLogOutput: " + t, t);
            throw t;
        }

    }

    private synchronized void ensureCapacity(long newCapacity) {
        try {
            logger.info(() -> "resizing marking file from " + (mbb == null ? 0 : mbb.capacity()) + " to " + newCapacity);
            if (file == null) {
                file = new RandomAccessFile(outputFile, "rw");
                file.seek(0);
                mbb = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, newCapacity);
            } else {
                int pos = mbb.position();
                file.setLength(newCapacity);
                file.seek(pos);
                mbb = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, newCapacity);
                mbb.position(pos);
            }
            logger.trace(() -> "mbb position now at " + mbb.position());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "CycleLogOutput{" +
                "mbb=" + mbb +
                ", file=" + file +
                ", mbb=" + (mbb == null ? "null" : "(pos=" + mbb.position() + ", limit=" + mbb.limit() + ", capacity=" + mbb.capacity() + ")") +
                '}';
    }

    @Override
    public void setFilter(Predicate<ResultReadable> filter) {
        this.filter = filter;
    }

}
