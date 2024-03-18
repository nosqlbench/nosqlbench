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
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;

import java.util.concurrent.TimeUnit;

public class MilvusQueryAsyncOp extends MilvusBaseOp<QueryParam> {
    private final long timeout;
    private final TimeUnit timeUnit;

    public MilvusQueryAsyncOp(MilvusServiceClient client, QueryParam request, long timeout, TimeUnit timeUnit) {
        super(client, request);

        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public Object applyOp(long value) {
        ListenableFuture<R<QueryResults>> call = client.queryAsync(request);
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
