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

package io.nosqlbench.engine.api.activityapi.cyclelog.inputs.cyclelog;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results_rle.CycleResultsRLEBufferReadable;
import io.nosqlbench.engine.api.util.SimpleConfig;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import io.nosqlbench.engine.api.activityapi.input.Input;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.function.Predicate;

public class CycleLogInput implements Input, AutoCloseable, Iterable<CycleResultsSegment>, CanFilterResultValue {
    private final static Logger logger = LogManager.getLogger(CycleLogInput.class);
    private final Iterator<CycleResultsSegment> cycleResultSegmentIterator;
    private RandomAccessFile raf;
    private MappedByteBuffer mbb;
    private Iterator<CycleResult> segmentIter;
    private Predicate<ResultReadable> filter;

    public CycleLogInput(Activity activity) {
        SimpleConfig conf = new SimpleConfig(activity, "input");
        mbb = initMappedBuffer(conf.getString("file").orElse(activity.getAlias()) + ".cyclelog");
        cycleResultSegmentIterator = iterator();
        segmentIter = cycleResultSegmentIterator.next().iterator();
    }

    public CycleLogInput(String filename) {
        File cycleFile = null;
        try {
            cycleFile = new File(filename);
            if (!cycleFile.exists()) {
                cycleFile = new File(cycleFile + ".cyclelog");
                if (!cycleFile.exists()) {
                    throw new RuntimeException("Cyclelog file does not exist:" + filename);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        mbb = initMappedBuffer(cycleFile.getPath());
        cycleResultSegmentIterator = new CycleResultsRLEBufferReadable(mbb).iterator();
        segmentIter = cycleResultSegmentIterator.next().iterator();
    }

    @Override
    public void setFilter(Predicate<ResultReadable> filter) {
        this.filter = filter;
    }

    @Override
    public synchronized CycleSegment getInputSegment(int segmentLength) {

        CycleSegmentBuffer csb = new CycleSegmentBuffer(segmentLength);

        while (csb.remaining() > 0) {

            while (!segmentIter.hasNext() && cycleResultSegmentIterator.hasNext()) {
                segmentIter = cycleResultSegmentIterator.next().iterator();
            }
            if (segmentIter.hasNext()) {
                CycleResult cycleResult = segmentIter.next();
                if (filter==null || filter.test(cycleResult)) {
                    csb.append(cycleResult.getCycle());
                }
            } else {
                if (csb.remaining() == segmentLength) {
                    return null;
                } else {
                    break;
                }
            }
        }
        return csb.toReadable();
    }

//            // acquire a buffered interval result
//            if (currentBuffer == null) {
//                currentBuffer = CycleResultsRLEBufferReadable.forOneRleSpan(mbb);
//                if (currentBuffer == null) {
//                    // or return null if none are left
//                    return null;
//                } else {
//                    strider = new CycleResultStrider(currentBuffer.getCycleResultIterable().iterator());
//                }
//            }
//            CycleResultsSegment cycleResultsSegment = strider.getCycleResultsSegment(remaining);
//            if (cycleResultsSegment!=null) {
//                for (CycleResult cycleResult : cycleResultsSegment) {
//                    csb.append(cycleResult.getCycle());
//                }
//            }
//            // else try again, because there are apparently more RLESegments to read.
//
//            remaining = csb.remaining();

    private MappedByteBuffer initMappedBuffer(String filename) {
        File filepath = new File(filename);
        if (!filepath.exists()) {
            throw new RuntimeException("file path '" + filename + "' does not exist!");
        }
        try {
            raf = new RandomAccessFile(filepath, "r");
            mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mbb;
    }

    @Override
    public void close() throws Exception {
        if (raf != null) {
            raf.close();
            mbb = null;
        }
    }


    @Override
    public Iterator<CycleResultsSegment> iterator() {
        CycleResultsRLEBufferReadable cycleResultsSegments = new CycleResultsRLEBufferReadable(mbb.duplicate());
        if (cycleResultsSegments instanceof CanFilterResultValue) {
            ((CanFilterResultValue)cycleResultsSegments).setFilter(filter);
        }
        return cycleResultsSegments.iterator();
    }

}
