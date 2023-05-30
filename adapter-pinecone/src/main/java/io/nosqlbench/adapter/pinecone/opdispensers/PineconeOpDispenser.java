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

package io.nosqlbench.adapter.pinecone.opdispensers;

import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public abstract class PineconeOpDispenser extends BaseOpDispenser<PineconeOp, PineconeSpace> {
    protected final LongFunction<PineconeSpace> pcFunction;
    protected final LongFunction<String> targetFunction;

    protected PineconeOpDispenser(PineconeDriverAdapter adapter,
                                  ParsedOp op,
                                  LongFunction<PineconeSpace> pcFunction,
                                  LongFunction<String> targetFunction) {
        super(adapter, op);
        this.pcFunction = pcFunction;
        this.targetFunction = targetFunction;
    }

}
