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


import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class OldCoreTracker implements CycleResultSegmentsReadable {

    private final LinkedList<ByteTrackerExtent> extents = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock(false);
    private final Condition extentsAvailable = lock.newCondition();

//    @Override
//    public CycleResultsIntervalSegment getCycleResultsSegment(int stride) {
//
//        while (true) {
//
//            ByteTrackerExtent extent = extents.peekFirst();
//            while (extent == null) {
//                try {
//                    extentsAvailable.await(10, TimeUnit.SECONDS);
//                } catch (InterruptedException ignored) {
//                }
//                extent = extents.peekFirst();
//            }
//
//            CycleResultsIntervalSegment segment = extent.getCycleResultsSegment(stride);
//
//            if (extent.isFullyServed()) {
//                extents.removeFirst();
//            }
//
//            if (segment!=null) {
//                return segment;
//            } else {
//                throw new RuntimeException("Null segment when extent is exhausted.");
//            }
//        }
//    }

    public void onExtent(ByteTrackerExtent extent) {
        extents.addLast(extent);
    }


    @Override
    public Iterator<CycleResultsSegment> iterator() {
        return new SegmentIterable();
    }

    private class SegmentIterable implements Iterator<CycleResultsSegment> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public CycleResultsSegment next() {
            return null;
        }
    }
}
