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

import com.google.common.util.concurrent.ListenableFuture;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MilvusSearchAsyncOp extends MilvusBaseOp<SearchParam> {

    private final long timeout;
    private final TimeUnit timeUnit;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Milvus/Zilliz client delete method
     *
     * @param client    The associated {@link MilvusServiceClient} used to communicate with the database
     * @param request   The {@link SearchParam} built for this operation
     */
    public MilvusSearchAsyncOp(MilvusServiceClient client, SearchParam request, long timeout, TimeUnit timeUnit) {
        super(client, request);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public R<SearchResults> applyOp(long value) {
        ListenableFuture<R<SearchResults>> call = client.searchAsync(request);
        try {
            return call.get(timeout,timeUnit);
        } catch (Exception e) {
            if (e instanceof RuntimeException rte) {
                throw rte;
            }
            else throw new RuntimeException(e);
        }
    }
}
