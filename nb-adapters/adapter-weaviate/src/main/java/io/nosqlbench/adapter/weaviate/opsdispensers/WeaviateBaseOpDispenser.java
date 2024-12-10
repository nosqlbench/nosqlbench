package io.nosqlbench.adapter.weaviate.opsdispensers;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.function.LongFunction;

import io.nosqlbench.adapter.weaviate.WeaviateDriverAdapter;
import io.nosqlbench.adapter.weaviate.WeaviateSpace;
import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.auth.exception.AuthException;

public abstract class WeaviateBaseOpDispenser<REQUEST, RESPONSE> extends BaseOpDispenser<WeaviateBaseOp<REQUEST, RESPONSE>, WeaviateSpace> {

    protected final LongFunction<WeaviateClient> clientFunction;
    protected final LongFunction<? extends WeaviateBaseOp<REQUEST,RESPONSE>> opF;
    private final LongFunction<REQUEST> paramF;

    protected WeaviateBaseOpDispenser(NBComponent adapter, ParsedOp op, LongFunction<WeaviateSpace> spaceF, LongFunction<String> targetF) {
        super(adapter, op, spaceF);
        this.clientFunction = (long l) -> {
            return spaceF.apply(l).getClient();
        };
        this.paramF = getParamFunc(this.clientFunction, op, targetF);
        this.opF = createOpFunc(paramF, this.clientFunction, op, targetF);
    }

    public abstract LongFunction<REQUEST> getParamFunc(LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF);

    public abstract LongFunction<WeaviateBaseOp<REQUEST, RESPONSE>> createOpFunc(
        LongFunction<REQUEST> paramF,
        LongFunction<WeaviateClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    @Override
    public WeaviateBaseOp<REQUEST, RESPONSE> getOp(long cycle) {
        return opF.apply(cycle);
    }

}
