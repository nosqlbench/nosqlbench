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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers.op_output;

import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.CompletedOp;

import java.util.Iterator;
import java.util.List;

public class StrideOutputSegmentImpl<D extends CompletedOp> implements StrideOutputSegment<D> {

    List<D> data;

    public StrideOutputSegmentImpl(List<D> strideData) {
        data = strideData;
    }

    @Override
    public long getCount() {
        return data.size();
    }

    @Override
    public long getMinCycle() {
        return data.get(0).getCycle();
    }

    @Override
    public Iterator<D> iterator() {
        return data.iterator();
    }
}
