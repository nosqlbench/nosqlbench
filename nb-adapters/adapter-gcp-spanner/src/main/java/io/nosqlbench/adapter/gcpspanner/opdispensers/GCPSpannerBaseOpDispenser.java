/*
 * Copyright (c) 2020-2024 nosqlbench
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

import io.nosqlbench.adapter.gcpspanner.GCPSpannerDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerBaseOp;
import io.nosqlbench.adapter.gcpspanner.GCPSpannerSpace;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

/**
 * Abstract base class for GCP Spanner operation dispensers.
 * This class extends the BaseOpDispenser and provides common functionality
 * for creating GCP Spanner operations.
 */
public abstract class GCPSpannerBaseOpDispenser<OP extends GCPSpannerBaseOp,RESULT>
    extends BaseOpDispenser<GCPSpannerBaseOp<?,?>, GCPSpannerSpace> {
    /**
     * A function that provides the target string based on a long input.
     */
    protected final LongFunction<String> targetFunction;

    /**
     * A function that provides the GCP Spanner space based on a long input.
     */
    protected final LongFunction<GCPSpannerSpace> spaceFunction;

    /**
     * Constructs a new GCPSpannerBaseOpDispenser.
     *
     * @param adapter the driver adapter for GCP Spanner operations
     * @param op the parsed operation
     * @param targetFunction a function that provides the target string
     */
    protected GCPSpannerBaseOpDispenser(GCPSpannerDriverAdapter adapter, ParsedOp op,
                                        LongFunction<String> targetFunction) {
        super(adapter, op);
        this.targetFunction = targetFunction;
        this.spaceFunction = adapter.getSpaceFunc(op);
    }

}
