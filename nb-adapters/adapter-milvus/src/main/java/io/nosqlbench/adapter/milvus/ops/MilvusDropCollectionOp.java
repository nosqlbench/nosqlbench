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

package io.nosqlbench.adapter.milvus.ops;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.DropCollectionParam;
import io.nosqlbench.adapters.api.templating.ParsedOp;

public class MilvusDropCollectionOp extends MilvusBaseOp<DropCollectionParam> {
    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Milvus/Zilliz client delete method
     *
     * @param client    The associated {@link MilvusServiceClient} used to communicate with the database
     * @param request   The {@link DropCollectionParam} built for this operation
     */
    public MilvusDropCollectionOp(MilvusServiceClient client, DropCollectionParam request) {
        super(client, request);
    }

    @Override
    public Object applyOp(long value) {
        return client.dropCollection(request);
    }
}
