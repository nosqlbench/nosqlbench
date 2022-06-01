/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.diag;

import io.nosqlbench.adapter.diag.optasks.DiagOpTask;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class DiagOp implements CycleOp<Integer> {

    private final static Logger logger = LogManager.getLogger(DiagOp.class);
    private final List<DiagOpTask> mutators;

    public DiagOp(List<DiagOpTask> mutators) {
        this.mutators = mutators;
    }

    @Override
    public Integer apply(long value) {
        Map<String, Object> state = Map.of("cycle", value, "code", 0);
        for (DiagOpTask mutator : mutators) {
            state = mutator.apply(value,state);
        }
        return (int) state.getOrDefault("code", 0);
    }

}
