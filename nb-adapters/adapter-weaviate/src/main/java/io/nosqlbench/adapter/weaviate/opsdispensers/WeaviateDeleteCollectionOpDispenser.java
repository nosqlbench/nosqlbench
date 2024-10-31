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
import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapter.weaviate.ops.WeaviateDeleteCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.weaviate.client.WeaviateClient;

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
public class WeaviateDeleteCollectionOpDispenser extends WeaviateBaseOpDispenser<WeaviateDeleteCollectionOp> {

    public WeaviateDeleteCollectionOpDispenser(
        WeaviateDriverAdapter adapter, ParsedOp op,
        LongFunction<String> targetF) {
        super(adapter, op, targetF);
    }

    @Override
    public LongFunction<WeaviateDeleteCollectionOp> getParamFunc(
        LongFunction<WeaviateClient> clientF, ParsedOp op,
        LongFunction<String> targetF
    ) {
//		LongFunction<String> ebF = l -> targetF.apply(l);
//
//		final LongFunction<String> lastF = ebF;
//		return l -> lastF.apply(l);
        return l -> new WeaviateDeleteCollectionOp(clientF.apply(l), targetF.apply(l));
    }

    @Override
    public LongFunction<WeaviateBaseOp<WeaviateDeleteCollectionOp>> createOpFunc(LongFunction<WeaviateDeleteCollectionOp> paramF, LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> new WeaviateDeleteCollectionOp(clientF.apply(l), paramF.apply(l));
    }

}
