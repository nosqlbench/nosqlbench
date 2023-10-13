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

package io.nosqlbench.scenarios.findmax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimFrameJournal extends ArrayList<SimFrame> implements JournalView {
    public void record(SimFrameParams params, SimFrameCapture.FrameSampleSet samples) {
        add(new SimFrame(params, samples));
    }

    @Override
    public List<SimFrame> frames() {
        return Collections.unmodifiableList(this);
    }

    @Override
    public SimFrame last() {
        return super.getLast();
    }

    @Override
    public SimFrame beforeLast() {
        if (size()<2) {
            throw new RuntimeException("can't get beforeLast for only " + size() + " elements");
        }
        return get(size()-2);
    }
}
