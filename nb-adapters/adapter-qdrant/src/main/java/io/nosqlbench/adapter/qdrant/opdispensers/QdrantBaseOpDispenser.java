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
 */

package io.nosqlbench.adapter.qdrant.opdispensers;

import io.nosqlbench.adapter.qdrant.QdrantDriverAdapter;
import io.nosqlbench.adapter.qdrant.QdrantSpace;
import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.qdrant.client.QdrantClient;

import java.util.function.LongFunction;

public abstract class QdrantBaseOpDispenser<T> extends BaseOpDispenser<QdrantBaseOp<T>, QdrantSpace> {

    protected final LongFunction<QdrantSpace> qdrantSpaceFunction;
    protected final LongFunction<QdrantClient> clientFunction;
    private final LongFunction<? extends QdrantBaseOp<T>> opF;
    private final LongFunction<T> paramF;

    protected QdrantBaseOpDispenser(QdrantDriverAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super((DriverAdapter)adapter, op);
        this.qdrantSpaceFunction = adapter.getSpaceFunc(op);
        this.clientFunction = (long l) -> this.qdrantSpaceFunction.apply(l).getClient();
        this.paramF = getParamFunc(this.clientFunction,op,targetF);
        this.opF = createOpFunc(paramF, this.clientFunction, op, targetF);
    }
    protected QdrantDriverAdapter getDriverAdapter() {
        return (QdrantDriverAdapter) adapter;
    }

    public abstract LongFunction<T> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    public abstract LongFunction<QdrantBaseOp<T>> createOpFunc(
        LongFunction<T> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    @Override
    public QdrantBaseOp<T> getOp(long value) {
        return opF.apply(value);
    }
}
