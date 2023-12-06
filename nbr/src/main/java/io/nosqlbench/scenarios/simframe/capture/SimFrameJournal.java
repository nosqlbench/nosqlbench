/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.scenarios.simframe.capture;

import io.nosqlbench.scenarios.simframe.planning.SimFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Aggregate usage patterns around capturing and using simulation frame data.
 */
public class SimFrameJournal<P> extends ArrayList<SimFrame<P>> implements JournalView<P> {

    public SimFrameJournal() {
    }
    protected SimFrameJournal(SimFrameJournal<P> simFrames) {
        this.addAll(simFrames);
    }

    public void record(P params, FrameSampleSet samples) {
        add(new SimFrame<P>(params, samples));
    }

    @Override
    public List<SimFrame<P>> frames() {
        return Collections.unmodifiableList(this);
    }

    @Override
    public SimFrame<P> last() {
        return super.getLast();
    }

    @Override
    public SimFrame<P> beforeLast() {
        if (size()<2) {
            throw new RuntimeException("can't get beforeLast for only " + size() + " elements");
        }
        return get(size()-2);
    }

    @Override
    public SimFrame<P> bestRun() {
        return this.stream().sorted(Comparator.comparingDouble(SimFrame::value)).toList().getLast();
    }

    @Override
    public SimFrame<P> before(SimFrame<P> frame) {
        int beforeIdx=frame.index()-1;
        if (beforeIdx>=0 && beforeIdx<=size()-1) {
            return frames().get(beforeIdx);
        } else throw new RuntimeException("Invalid index for before: " + beforeIdx + " with " + size() + " frames");
    }

    @Override
    public SimFrame<P> after(SimFrame<P> frame) {
        int afterIdx=frame.index()+1;
        if (afterIdx>=0 && afterIdx<=size()-1) {
            return frames().get(afterIdx);
        } else throw new RuntimeException("Invalid index for after: " + afterIdx + " with " + size() + " frames");
    }

    @Override
    public JournalView<P> reset() {
        var prior = new SimFrameJournal<>(this);
        this.clear();
        return prior;
    }
}
