/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl.varcap;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.VariableCapture;
import io.nosqlbench.virtdata.core.templates.CapturePoint;

import java.util.List;

public class VarCapCycleOp<T> implements CycleOp<T> {
    private final CycleOp<T> realOp;
    private final List<CapturePoint> capturePointList;

    public VarCapCycleOp(CycleOp<T> op, List<CapturePoint> varcaps) {
        this.realOp = op;
        this.capturePointList = varcaps;
    }


    @Override
    public T apply(long value) {
        T result = realOp.apply(value);
        VariableCapture<T> capturer = ((VariableCapture<T>) this);
        List<?> captured = capturer.capture(result, capturePointList);
        capturer.applyCaptures(capturePointList,captured);
        return result;
    }
}
