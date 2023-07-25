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

package io.nosqlbench.engine.api.activityapi.cyclelog.filters;

import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.TristateFilter;
import io.nosqlbench.engine.api.activityapi.input.Input;

import java.util.function.IntPredicate;

public abstract class InputMapper implements TristateFilter {

    private Input input;
    private IntPredicate predicate;

    public InputMapper setInput(Input input) {
        this.input = input;
        return this;
    }

    public InputMapper setPredicate(IntPredicate predicate) {
        this.predicate = predicate;
        return this;
    }

//    @Override
//    public InputSegment getInputSegment(int segmentLength) {
//        CycleArrayBuffer buf = new CycleArrayBuffer(segmentLength);
//        while (buf.remaining()>0) {
//            int remaining = buf.remaining();
//            InputSegment inputSegment = input.getInputSegment(remaining);
//        }
//        input.getInputSegment(segmentLength);
//        input.getInputSegment(1);
//        input.getInputSegment()
//        return null;
//    }
}
