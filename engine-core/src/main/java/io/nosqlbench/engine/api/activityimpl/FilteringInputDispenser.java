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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.inputs.cyclelog.CanFilterResultValue;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;

import java.util.function.Predicate;

public class FilteringInputDispenser implements InputDispenser {
    private final InputDispenser dispenser;
    private final Predicate<ResultReadable> filterPredicate;

    public FilteringInputDispenser(InputDispenser dispenser, Predicate<ResultReadable> resultValuePredicate) {
        this.dispenser = dispenser;
        this.filterPredicate = resultValuePredicate;
    }

    @Override
    public Input getInput(long slot) {
        Input input = dispenser.getInput(slot);

        if (input instanceof CanFilterResultValue) {
            ((CanFilterResultValue) input).setFilter(filterPredicate);
        } else {
            throw new RuntimeException("Unable to set result filterPredicate on input '" + input + ", filterPredicate '" + filterPredicate + "'");
        }
        return input;
    }
}
