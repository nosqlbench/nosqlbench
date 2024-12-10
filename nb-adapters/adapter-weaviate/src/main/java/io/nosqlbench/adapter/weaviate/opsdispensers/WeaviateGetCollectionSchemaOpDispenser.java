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
package io.nosqlbench.adapter.weaviate.opsdispensers;

import java.util.function.LongFunction;

import io.nosqlbench.adapter.weaviate.WeaviateDriverAdapter;
import io.nosqlbench.adapter.weaviate.WeaviateSpace;
import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapter.weaviate.ops.WeaviateGetCollectionSchemaOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;

public class WeaviateGetCollectionSchemaOpDispenser extends WeaviateBaseOpDispenser<String, Result<?>> {

    public WeaviateGetCollectionSchemaOpDispenser(WeaviateDriverAdapter adapter, ParsedOp op, LongFunction<WeaviateSpace> spaceF, LongFunction<String> targetF) {
        super(adapter, op, spaceF, targetF);
    }

    @Override
    public LongFunction<String> getParamFunc(LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> targetF.apply(l);
    }

    @Override
    public LongFunction<WeaviateBaseOp<String,Result<?>>> createOpFunc(LongFunction<String> paramF, LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> new WeaviateGetCollectionSchemaOp(clientF.apply(l), paramF.apply(l));
    }

}
