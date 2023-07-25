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

package io.nosqlbench.engine.api.activityimpl.marker;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsSegment;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsIntervalSegment;
import io.nosqlbench.engine.api.activityapi.output.Output;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is the default cycle output implementation for NB when
 * the input cycles are known to be contiguous.
 * <p>
 * This cycle marker wraps another tracking structure in order to
 * allow for flexible buffering methods. The extents are buffer segments
 * which can be managed atomically. They are chained here in two chains:
 * The marking chain and the tracking chain. When the atomic marking head
 * is non-null, then marking is possible, but marking calls block otherwise.
 * The same is true for the tracking head element.
 * <p>
 * The nowMarking and nowTracking conditions are meant to be locked and awaited
 * by marking and tracking calls respectively. Conversely, they are expected
 * to be signaled by tracking and marking calls.
 * <p>
 * This implementation needs to be adapted to onAfterOpStop early exit of either
 * marker or tracker threads with no deadlock.
 */
public class ContiguousOutputChunker implements Output {

    private final static Logger logger = LogManager.getLogger(ContiguousOutputChunker.class);
    private final int extentSize;
    private final int maxExtents;
    private final List<Output> readers = new ArrayList<>();
    private final AtomicLong min;
    private final AtomicLong nextMin;
    private final AtomicReference<ByteTrackerExtent> markingExtents = new AtomicReference<>();
    private final ReentrantLock lock = new ReentrantLock(false);
    private final Condition nowMarking = lock.newCondition();
    private final Semaphore mutex = new Semaphore(1, false);

    public ContiguousOutputChunker(long min, long nextRangeMin, int extentSize, int maxExtents) {
        this.min = new AtomicLong(min);
        this.nextMin = new AtomicLong(nextRangeMin);
        this.extentSize = extentSize;
        this.maxExtents = maxExtents;
        initExtents();
    }

    public ContiguousOutputChunker(Activity activity) {

        if (!(activity.getInputDispenserDelegate().getInput(0).isContiguous())) {
            throw new RuntimeException("This type of output may not be used with non-contiguous inputs yet.");
            // If you are looking at this code, it's because we count updates to extents to provide
            // efficient marker extent handling. The ability to use segmented inputs with markers will
            // come in a future append.
        }
        this.min = new AtomicLong(activity.getActivityDef().getStartCycle());
        this.nextMin = new AtomicLong(activity.getActivityDef().getEndCycle());
        long stride = activity.getParams().getOptionalLong("stride").orElse(1L);
        long cycleCount = nextMin.get() - min.get();
        if ((cycleCount % stride) != 0) {
            throw new RuntimeException("stride must evenly divide into cycles.");
            // TODO: Consider setting cycles to " ...
        }
        this.extentSize = calculateExtentSize(cycleCount, stride);
        this.maxExtents = 3;
        initExtents();
    }

    private synchronized void initExtents() {
        ByteTrackerExtent extent = new ByteTrackerExtent(min.get(), (min.get() + extentSize));
        this.markingExtents.set(extent);
        for (int i = 0; i < maxExtents; i++) {
            extent = extent.extend();
            logger.debug("added tracker extent " + extent.rangeSummary());
        }
        logger.info(() -> "using max " + maxExtents + " extents with getCount: " + extentSize);
    }


    @Override
    public synchronized void onCycleResultSegment(CycleResultsSegment segment) {
        logger.trace(() -> "on-cycle-result-segment: (" + segment + ")");
        for (CycleResult cr : segment) {
            onCycleResult(cr.getCycle(), cr.getResult());
        }
    }

    @Override
    public synchronized boolean onCycleResult(long completedCycle, int result) {
        logger.trace(() -> "on-cycle-result: (" + completedCycle + "," + result + ")");

        try {
            while (true) {
                ByteTrackerExtent extent = this.markingExtents.get();
                long unmarked = extent.markResult(completedCycle, result);

                if (unmarked > 0) {
                    return true;
                } else if (unmarked == 0) {
                    try {
                        mutex.acquire();
                        ByteTrackerExtent head = this.markingExtents.get();
                        while (head.isFullyFilled()) {
                            head.extend();
                            if (!this.markingExtents.compareAndSet(head, head.getNextExtent().get())) {
                                throw new RuntimeException("Unable to swap head extent.");
                            }
                            onFullyFilled(head);
                            head = this.markingExtents.get();
                        }
                        mutex.release();
                    } catch (InterruptedException ignored) {
                    } catch (Throwable t) {
                        throw t;
                    }
                    return true;
                } else {
                    System.out.println("whoops");
                }
            }
        } catch (Throwable t) {
            throw t; // for debugging
        }
    }

    @Override
    public synchronized void close() throws Exception {
        try {

            mutex.acquire();
            ByteTrackerExtent e = this.markingExtents.get();
            while (e != null) {
                onFullyFilled(e);
                e = e.getNextExtent().get();
            }
            mutex.release();

            for (Output reader : this.readers) {
                logger.debug("closing downstream reader: " + reader);
                reader.close();
            }
        } catch (Throwable t) {
            logger.error("Error while attempting to close " +this + ": " + t, t);
            throw t;
        }


    }

    private void onFullyFilled(ByteTrackerExtent extent) {
        logger.trace(() -> "MARKER>: fully filled: " + extent);
        for (Output reader : readers) {
            CycleResultsIntervalSegment remainingSegment = extent.getRemainingSegment();
            if (remainingSegment != null) {
                reader.onCycleResultSegment(remainingSegment);
            }
        }
    }

    private void onFullyServed(ByteTrackerExtent firstReadable) {
        logger.debug("TRACKER: fully tracked: " + firstReadable);
    }

    public synchronized void addExtentReader(Output reader) {
        this.readers.add(reader);
    }

    public synchronized void removeExtentReader(Output reader) {
        this.readers.remove(reader);
    }

    private int calculateExtentSize(long cycleCount, long stride) {
        if (cycleCount <= 2000000) {
            return (int) cycleCount;
        }
        for (int cs = 2000000; cs > 500000; cs--) {
            if ((cycleCount % cs) == 0 && (cs % stride) == 0) {
                return cs;
            }
        }
        throw new RuntimeException("no even divisor of cycleCount and Stride between 500K and 2M, with cycles=" + cycleCount + ",  and stride=" + stride);
    }


    @Override
    public String toString() {
        return ContiguousOutputChunker.class.getSimpleName() + "{" +
                "extentSize=" + extentSize +
                ", maxExtents=" + maxExtents +
                ", readers=" + readers +
                ", min=" + min +
                ", nextMin=" + nextMin +
                ", markingExtents/Chain=" + markingExtents.get().getChainSize() +
                '}';
    }

}
