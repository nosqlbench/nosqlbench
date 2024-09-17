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
 *
 */

package io.nosqlbench.adapter.gcpspanner.opdispensers;

import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerBaseOp;
import io.nosqlbench.adapter.gcpspanner.GCPSpannerSpace;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public abstract class GCPSpannerBaseOpDispenser extends BaseOpDispenser<GCPSpannerBaseOp<?>, GCPSpannerSpace> {
    protected final LongFunction<String> targetFunction;
    protected final LongFunction<GCPSpannerSpace> spaceFunction;

    protected GCPSpannerBaseOpDispenser(DriverAdapter<? extends GCPSpannerBaseOp<?>, GCPSpannerSpace> adapter, ParsedOp op,
                                        LongFunction<String> targetFunction) {
        super(adapter, op);
        this.targetFunction = targetFunction;
        this.spaceFunction = adapter.getSpaceFunc(op);
    }

}
