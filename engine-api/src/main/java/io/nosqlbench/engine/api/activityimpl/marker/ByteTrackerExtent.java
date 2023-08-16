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
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.CycleResultSegmentsReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultsIntervalSegment;


import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple bytebuffer marker implementation
 */
public class ByteTrackerExtent implements CycleResultSegmentsReadable {

    private final long min; // The maximum value to be dispatched
    private final AtomicInteger totalMarked; // The total number of marked values
    private final AtomicInteger totalServed; // the total number of served values
    byte[] markerData;
    private final int size; // max-min
    private final AtomicReference<ByteTrackerExtent> nextExtent = new AtomicReference<>();
    private final boolean filled = false;

    /**
     * Create a simple marker extent
     *
     * @param min     the first logical cycle to be returned by this tracker
     * @param nextMin the first logical cycle of the next range
     */
    public ByteTrackerExtent(long min, long nextMin) {
        this.min = min;
        this.size = (int) (nextMin - min);
        markerData = new byte[size];
        totalMarked = new AtomicInteger(0);
        totalServed = new AtomicInteger(0);
//        maxcont = new AtomicLong(min - 1);
//        currentValue = new AtomicLong(min);
    }

    public ByteTrackerExtent(long min, int[] ints) {
        this(min, min + ints.length);
        for (int i = 0; i < ints.length; i++) {
            markResult(min + i, ints[i]);
        }
    }

    /**
     * mark the named cycle in the extent, or in any future extent that we know.
     * The return value determines the known state of the current extent:
     * <ol>
     * <li>negative value: indicates an attempt to mark a value outside the range,
     * either before min or after max of the furthest known extent</li>
     * <li>zero: indicates successful marking, but exactly no remaining space available.
     * This is how a marking thread can detect that it was the one that finished marking
     * an extent.</li>
     * <li>positive value: indicates how many cycles remain available in the extent to
     * be marked.</li>
     * </ol>
     *
     * @param cycle  The cycle to be marked
     * @param result the result code to mark in the cycle
     * @return the number of cycles remaining after marking, or a negative number indicating an error.
     */
    public long markResult(long cycle, int result) {
        if (cycle < min) {
            return cycle - min; // how short were we? ( a negative number )
        }
        if (cycle >= min + size) {
            ByteTrackerExtent next = this.nextExtent.get();
            if (next != null) {
                return next.markResult(cycle, result);
            } else {
                return (min + size) - cycle; // how long were we? ( a negative number )
            }
        }

        int position = (int) (cycle - min);
        byte resultCode = (byte) (result & 127);
        try {
            markerData[position] = resultCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = totalMarked.incrementAndGet();

        return (size - i);
    }

    public CycleResultsIntervalSegment getRemainingSegment() {

//        if (!filled) {
//            filled = isFullyFilled(); // eagerly evaluate fill until it is known to be filled
//            if (!filled) {
//                return null;
//            }
//        }
//
        int current = totalServed.get();
        int next = totalMarked.get();
        if (next - current == 0) {
            return null;
        }
        if (totalServed.compareAndSet(current, next)) {
            return CycleResultsIntervalSegment.forData(current + min, markerData, current, next);
        } else {
            throw new RuntimeException("error while attempting to consume remainder of data in extent from position " + current);
        }
    }

//    @Override
//    public CycleResultsIntervalSegment getCycleResultsSegment(int stride) {
//        if (!filled) {
//            filled = isFullyFilled(); // eagerly evaluate fill until it is known to be filled
//            if (!filled) {
//                return null;
//            }
//        }
//
//        while (totalServed.get() < size) {
//            int current = totalServed.get();
//            int next = current + stride;
//            if (next <= totalMarked.get()) {
//                if (totalServed.compareAndSet(current, next)) {
//                    return CycleResultsIntervalSegment.forData(current + min, markerData, current, stride);
//                }
//            }
//        }
//
//        return null;
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(min).append(",").append(min + size).append("): ")
                .append(", getCount=").append(this.size)
                .append(", marked=").append(this.totalMarked.get())
                .append(", served=").append(this.totalServed.get());
        sb.append(" data=");
        if (markerData.length < 100) {
            for (int i = 0; i < this.markerData.length; i++) {
                sb.append(markerData[i]).append(",");
            }
        } else {
            byte[] front = Arrays.copyOfRange(markerData, 0, 50);
            for (int i = 0; i < front.length; i++) {
                sb.append(front[i]).append(",");
            }
            sb.append(" . . ., ");
            byte[] back = Arrays.copyOfRange(markerData, 0, 50);
            for (int i = 0; i < back.length; i++) {
                sb.append(back[i]).append(",");
            }
        }
        return sb.toString();
    }

    public boolean isFullyFilled() {
        return (totalMarked.get() == size);
    }

//    public boolean isFullyServed() {
//        return (totalServed.get() == size);
//    }
//

    public AtomicReference<ByteTrackerExtent> getNextExtent() {
        return nextExtent;
    }

    // For testing
    byte[] getMarkerData() {
        return markerData;
    }

    /**
     * Find the last known extent, and add another after it, account for
     * contiguous ranges and extent getCount. Note that this does not mean
     * necessarily that the extent will be added immediately after the current one.
     *
     * @return The new extent that was created.
     */
    public ByteTrackerExtent extend() {

        ByteTrackerExtent lastExtent = this;
        while (lastExtent.getNextExtent().get() != null) {
            lastExtent = lastExtent.getNextExtent().get();
        }

        ByteTrackerExtent newLastExtent = new ByteTrackerExtent(
                lastExtent.getMin() + size, lastExtent.getMin() + (size * 2L)
        );

        if (!lastExtent.getNextExtent().compareAndSet(null, newLastExtent)) {
            throw new RuntimeException("There should be no contention for extending the extents. If this occurs, then" +
                    "there is a synchronization bug somewhere in the calling code.");
        }

        return newLastExtent;
    }

    public long getMin() {
        return min;
    }

    public int getSize() {
        return size;
    }

    public int getChainSize() {
        return 1 + ((nextExtent.get() == null) ? 0 : nextExtent.get().getChainSize());
    }

    public String rangeSummary() {
        return "[" + min + "," + (min + size) + ")";
    }


    @Override
    public Iterator<CycleResultsSegment> iterator() {
        return new SegmentIterator(min, this.markerData);
    }

    private class SegmentIterator implements Iterator<CycleResultsSegment> {
        private final long cycle;
        private byte[] markerData;

        public SegmentIterator(long cycle, byte[] markerData) {
            this.cycle = cycle;
            this.markerData = markerData;
        }

        @Override
        public boolean hasNext() {
            return (markerData != null);
        }

        @Override
        public CycleResultsSegment next() {
            CycleResultsIntervalSegment cycleResults = CycleResultsIntervalSegment.forData(cycle, markerData);
            markerData = null;
            return cycleResults;
        }
    }
}
