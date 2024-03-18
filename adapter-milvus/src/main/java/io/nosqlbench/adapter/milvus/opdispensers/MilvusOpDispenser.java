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

package io.nosqlbench.adapter.milvus.opdispensers;

import io.milvus.client.MilvusServiceClient;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusSpace;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public abstract class MilvusOpDispenser extends BaseOpDispenser<MilvusBaseOp, MilvusSpace> {

    protected final LongFunction<MilvusSpace> mzSpaceFunction;
    protected final LongFunction<MilvusServiceClient> clientFunction;
    private final LongFunction<? extends MilvusBaseOp> opF;

    protected MilvusOpDispenser(MilvusDriverAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op);
        this.mzSpaceFunction = adapter.getSpaceFunc(op);
        this.clientFunction = (long l) -> this.mzSpaceFunction.apply(l).getClient();
        this.opF = createOpFunc(this.clientFunction, op, targetF);
    }

    public abstract LongFunction<? extends MilvusBaseOp> createOpFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    @Override
    public MilvusBaseOp apply(long value) {
        return opF.apply(value);
    }
}
