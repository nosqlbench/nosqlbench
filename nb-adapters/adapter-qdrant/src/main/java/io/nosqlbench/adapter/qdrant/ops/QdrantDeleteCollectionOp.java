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

package io.nosqlbench.adapter.qdrant.ops;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CollectionOperationResponse;
import io.qdrant.client.grpc.Collections.DeleteCollection;

public class QdrantDeleteCollectionOp extends QdrantBaseOp<DeleteCollection> {
    public QdrantDeleteCollectionOp(QdrantClient client, DeleteCollection request) {
        super(client, request);
    }

    @Override
    public Object applyOp(long value) {
        ListenableFuture<CollectionOperationResponse> response = client.deleteCollectionAsync(request.getCollectionName());

        return response;
    }
}
