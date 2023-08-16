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

package io.nosqlbench.engine.api.activityapi.cyclelog.outputs;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultArray;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegmentReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.inputs.cyclelog.CanFilterResultValue;
import io.nosqlbench.engine.api.activityapi.output.Output;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * This will implement a result buffer that puts cycles in order when possible,
 * according to a sliding window.
 */
public class ReorderingConcurrentResultBuffer implements Output, CanFilterResultValue {

    private final static Logger logger = LogManager.getLogger(ReorderingConcurrentResultBuffer.class);

    private final LinkedList<CycleResultsSegment> segments = new LinkedList<>();
    private final Output downstream;
    private final int threshold;
    private long currentCount;
    private long segmentCount;
    private Predicate<ResultReadable> resultFilter;

    public ReorderingConcurrentResultBuffer(Output downstream) {
        this(downstream,1000);
    }

    public ReorderingConcurrentResultBuffer(Output downstream, int threshold) {
        this.downstream = downstream;
        this.threshold = threshold;
    }

    @Override
    public synchronized boolean onCycleResult(long completedCycle, int result) {
        this.onCycleResultSegment(CycleResultsSegmentReadable.forCycleResult(completedCycle, result));
        return true;
    }

    @Override
    public synchronized void onCycleResultSegment(CycleResultsSegment segment) {
        if (resultFilter!=null) {
            segment = segment.filter(resultFilter);
        }
        if (!(segment instanceof CanSortCycles)) {
            segment = new CycleResultArray(segment);
        }
        ((CanSortCycles)segment).sort();
        segments.add(segment);
        segmentCount++;
        currentCount+=segment.getCount();
        if (currentCount>=threshold) {
            logger.trace(() -> "Reordering threshold met: " + currentCount +"/" + threshold + ", sorting and pushing. (" + segments.size() + " segments)");
            Collections.sort(segments);
            while(currentCount>=threshold) {
                CycleResultsSegment head = segments.removeFirst();
                downstream.onCycleResultSegment(head);
                segmentCount--;
                currentCount-=head.getCount();
            }
        }
    }

    @Override
    public synchronized void close() throws Exception {
        logger.trace(() -> "closing and flushing " + segments.size() + " segments");
        Collections.sort(segments);
        for (CycleResultsSegment segment : segments) {
            downstream.onCycleResultSegment(segment);
            segmentCount--;
            currentCount-=segment.getCount();
        }
        downstream.close();

    }

    @Override
    public void setFilter(Predicate<ResultReadable> filter) {
        this.resultFilter = filter;

    }
}
