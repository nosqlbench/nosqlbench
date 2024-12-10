/*
 * Copyright (c) 2022-2024 nosqlbench
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

package io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers;

import io.nosqlbench.adapters.api.activityimpl.uniform.Validator;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

import java.util.Map;
import java.util.function.Function;

public class CapturingOp<T> implements CycleOp<Map<String,?>> {

    private final CycleOp<T> op;
    private final Function<T, Map<String, ?>> extractorF;

    public CapturingOp(CycleOp<T> op, Function<T, Map<String,?>> extractorF) {
        this.op = op;
        this.extractorF = extractorF;
    }

    @Override
    public Map<String,?> apply(long value) {
        T result = op.apply(value);
        Map<String, ?> map = extractorF.apply(result);
        return map;
    }
}
