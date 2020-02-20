/*
*   Copyright 2015 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
package io.nosqlbench.activitycore.fortesting;

import io.nosqlbench.activityapi.cyclelog.buffers.results.CycleArray;
import io.nosqlbench.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.activityapi.input.Input;
import io.nosqlbench.activityimpl.input.InputInterval;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This cycle value supplier blocks the caller, only letting it complete
 * for each value that is set from the controlling producer. This is just
 * for testing. The convenience notify methods are to make tests more obvious.
 */
public class BlockingSegmentInput implements Input {

    private final AtomicLong cycle = new AtomicLong(0L);
    private final InputInterval inputInterval = new InputInterval(0,Long.MAX_VALUE);

    private CycleSegment segment;

    @Override
    public synchronized CycleSegment getInputSegment(int segmentLength) {
        try {
            this.wait();
        } catch (InterruptedException ignored){}
        CycleSegment toReturn = this.segment;
        this.segment=null;
        return toReturn;
    }

    public void publishSegment(long... cycleValues) {
        this.segment = new CycleArray.ArraySegment(cycleValues);
        synchronized (this) {
            this.notify();
        }
    }

    public String toString() {
        return BlockingSegmentInput.class.getSimpleName() + ": " + segment;
    }

}
