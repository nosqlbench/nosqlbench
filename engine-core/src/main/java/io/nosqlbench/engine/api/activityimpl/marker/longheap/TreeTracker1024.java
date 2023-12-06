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

package io.nosqlbench.engine.api.activityimpl.marker.longheap;

/**
 * Wrap 32 of the 32-position LongTreeTrackers together, allowing
 * for a range of 0-1023 positions.
 */
public class TreeTracker1024 {
    private long base = 0L;

    private final LongTreeTracker ranges = new LongTreeTracker();
    private final LongTreeTracker[] buckets = new LongTreeTracker[32];

    public TreeTracker1024(long base) {
        this.base = base;
        for (int p = 0; p < buckets.length; p++) {
            buckets[p] = new LongTreeTracker();
        }
    }

    public TreeTracker1024() {
        this(0L);
    }

    public void setPositionChecked(long position) {
        if ((position - base) < 0) {
            throw new RuntimeException("position must be greater than or equal to base of " + base);
        }
        if ((position - base) > 1023) {
            throw new RuntimeException("postion must be less than or equal to 1023 + base of " + base);
        }
        setPosition(position);
    }

    public void setPosition(long position) {
        long ranged = position - base;
        int bucket = (int) ranged >> 5;
        long bucketPosition = ranged & 0b11111L;

        if ((buckets[bucket].setCompleted(bucketPosition) & 2L)>0) {
            this.ranges.setCompleted(bucket);
        }
    }

    public boolean isCompleted(long position) {
        long ranged = position - base;
        int bucket = (int) ranged >> 5;
        long innerPos = ranged & 0b11111L;
        return buckets[bucket].isCompleted(innerPos);
    }

    public long getLowestCompleted() {
        long lowestCompleted=base;
        int lowestBucket = (int) ranges.getLowestCompleted();
        lowestCompleted += (lowestBucket+1) * 32L;
        lowestCompleted += buckets[lowestBucket+1].getLowestCompleted();
        return lowestCompleted;
    }

    public boolean isCompleted() {
        return this.ranges.isCompleted();
    }
}
