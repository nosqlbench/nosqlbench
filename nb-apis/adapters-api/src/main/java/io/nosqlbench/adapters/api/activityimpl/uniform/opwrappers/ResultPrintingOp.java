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

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

public class ResultPrintingOp<T> implements CycleOp<T> {

    private final CycleOp<T> cycleOp;
    public ResultPrintingOp(CycleOp<T> cycleOp) {
        this.cycleOp = cycleOp;
    }

    @Override
    public T apply(long value) {
            T result = cycleOp.apply(value);
            System.out.println("result from cycle " + value + ":\n"+result);
            return result;
    }
}
