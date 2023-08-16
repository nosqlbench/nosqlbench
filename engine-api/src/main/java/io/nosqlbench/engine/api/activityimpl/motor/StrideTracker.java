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

package io.nosqlbench.engine.api.activityimpl.motor;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.*;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.Buffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.op_output.StrideOutputConsumer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.output.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StrideTracker<D> extends Buffer<CompletedOp<D>> implements OpEvents<D>, CycleResultsSegment {
    private final static Logger logger = LogManager.getLogger(StrideTracker.class);

    private final Timer strideServiceTimer;
    private final Timer strideResponseTimer;

    private final OpImpl<Void> strideOp;
    private final Output output;
    private final StrideOutputConsumer<D> outputReader;

    public StrideTracker(
            Timer strideServiceTimer,
            Timer strideResponseTimer,
            long strideWaitTime,
            long initialCycle,
            int size,
            Output output,
            StrideOutputConsumer<D> outputReader) {
        super(size);
        this.strideServiceTimer = strideServiceTimer;
        this.strideResponseTimer = strideResponseTimer;

        this.strideOp =new OpImpl<>();
        strideOp.setCycle(initialCycle);
        strideOp.setWaitTime(strideWaitTime);

        this.output = output;
        this.outputReader = outputReader;
    }

    /**
     * Each strideOp opTracker must be started before any ops that it tracks
     *
     */
    public void start() {
        this.strideOp.start();
    }

    @Override
    public void onOpStarted(StartedOp<D> op) {
    }

    @Override
    public void onOpSuccess(SucceededOp<D> op) {
        super.put(op);
    }

    @Override
    public void onOpFailure(FailedOp<D> op) {
        super.put(op);
    }

    @Override
    public void onOpSkipped(SkippedOp<D> op) {
        super.put(op);
    }


    /**
     * When a stride is complete, do house keeping. This effectively means when N==stride ops have been
     * submitted to this buffer, which is tracked by {@link Buffer#put(Comparable)}.
     */
    public void onFull() {
        strideOp.succeed(0);
        logger.trace(() -> "completed strideOp with first result cycle (" + strideOp.getCycle() + ")");
        strideServiceTimer.update(strideOp.getResponseTimeNanos(), TimeUnit.NANOSECONDS);
        if (strideResponseTimer!=null) {
            strideResponseTimer.update(strideOp.getResponseTimeNanos(),TimeUnit.NANOSECONDS);
        }

        if (output != null) {
            output.onCycleResultSegment(this);
        }
        if (outputReader!=null) {
            List<CompletedOp<D>> flippedData = getFlippedData();
            outputReader.onStrideOutput(flippedData);
        }
    }

    @Override
    protected int compare(CompletedOp<D> one, CompletedOp<D> other) {
        return one.compareTo(other);
    }


    @Override
    public long getCount() {
        return data.size();
    }

    @Override
    public long getMinCycle() {
        return data.get(0).getCycle();
    }

    @Override
    public Iterator<CycleResult> iterator() {
        return new CycleResultIterator<>(data.iterator());
    }

    private final static class CycleResultIterator<D> implements Iterator<CycleResult> {
        private final Iterator<CompletedOp<D>> copiter;

        public CycleResultIterator(Iterator<CompletedOp<D>> copiter) {
            this.copiter = copiter;
        }

        @Override
        public boolean hasNext() {
            return copiter.hasNext();
        }

        @Override
        public CycleResult next() {
            CompletedOp next = copiter.next();
            return next;
        }
    }
}
