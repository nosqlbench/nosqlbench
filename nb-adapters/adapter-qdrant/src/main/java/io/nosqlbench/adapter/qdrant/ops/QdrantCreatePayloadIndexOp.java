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

import io.nosqlbench.adapter.qdrant.pojos.CreatePayloadIndexRequest;
import io.qdrant.client.QdrantClient;

public class QdrantCreatePayloadIndexOp extends QdrantBaseOp<CreatePayloadIndexRequest> {
    public QdrantCreatePayloadIndexOp(QdrantClient client, CreatePayloadIndexRequest request) {
        super(client, request);
    }

    @Override
    public Object applyOp(long value) {
        //client.createPayloadIndexAsync(PayloadIndexParams.get);
        return null;
    }
}
