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
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public abstract class MilvusBaseOpDispenser<T> extends BaseOpDispenser<MilvusBaseOp<T>, MilvusSpace> {

    protected final LongFunction<MilvusSpace> mzSpaceFunction;
    protected final LongFunction<MilvusServiceClient> clientFunction;
    private final LongFunction<? extends MilvusBaseOp<T>> opF;
    private final LongFunction<T> paramF;

    protected MilvusBaseOpDispenser(MilvusDriverAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super((DriverAdapter)adapter, op);
        this.mzSpaceFunction = adapter.getSpaceFunc(op);
        this.clientFunction = (long l) -> this.mzSpaceFunction.apply(l).getClient();
        this.paramF = getParamFunc(this.clientFunction,op,targetF);
        this.opF = createOpFunc(paramF, this.clientFunction, op, targetF);
    }
    protected MilvusDriverAdapter getDriverAdapter() {
        return (MilvusDriverAdapter) adapter;
    }

    public abstract LongFunction<T> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    public abstract LongFunction<MilvusBaseOp<T>> createOpFunc(
        LongFunction<T> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    @Override
    public MilvusBaseOp<T> getOp(long value) {
        return opF.apply(value);
    }
}
