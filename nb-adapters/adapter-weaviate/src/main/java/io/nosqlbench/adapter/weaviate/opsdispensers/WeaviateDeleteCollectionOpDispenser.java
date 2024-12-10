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
import io.nosqlbench.adapter.weaviate.ops.WeaviateDeleteCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;

/**
 * Delete a Weaviate collection.
 *
 * @see <a href=
 *     "https://weaviate.io/developers/weaviate/manage-data/collections#delete-a-collection">Delete
 *     Collection docs</a>.
 * @see <a href=
 *     "https://weaviate.io/developers/weaviate/api/rest#tag/schema/delete/schema/{className}">Delete
 *     Collection REST API</a>.
 */
public class WeaviateDeleteCollectionOpDispenser extends WeaviateBaseOpDispenser<String, Result<?>> {

    public WeaviateDeleteCollectionOpDispenser(NBComponent adapter, ParsedOp op, LongFunction<WeaviateSpace> spaceF, LongFunction<String> targetF) {
        super(adapter, op, spaceF, targetF);
    }

    @Override
    public LongFunction<String> getParamFunc(LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return targetF;
    }

    @Override
    public LongFunction<WeaviateBaseOp<String, Result<?>>> createOpFunc(LongFunction<String> paramF, LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> new WeaviateDeleteCollectionOp(clientF.apply(l), paramF.apply(l));
    }

}
