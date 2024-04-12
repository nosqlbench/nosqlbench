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
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import io.nosqlbench.adapters.api.templating.ParsedOp;

public class MilvusSearchOp extends MilvusBaseOp<SearchParam> {

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Milvus/Zilliz client delete method
     *
     * @param client    The associated {@link MilvusServiceClient} used to communicate with the database
     * @param request   The {@link SearchParam} built for this operation
     */
    public MilvusSearchOp(MilvusServiceClient client, SearchParam request) {
        super(client, request);
    }

    @Override
    public R<SearchResults> applyOp(long value) {
        R<SearchResults> response = client.search(request);
        return response;
    }
}
