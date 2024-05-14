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

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CollectionInfo;

import java.time.Duration;

public class QdrantCollectionInfoOp extends QdrantBaseOp<String> {
    public QdrantCollectionInfoOp(QdrantClient client, String request) {
        super(client, request);
    }

    @Override
    public Object applyOp(long value) {
        CollectionInfo response;
        try {
            response = client.getCollectionInfoAsync(request, Duration.ofSeconds(600)).get();
            logger.debug("Collection info: {}", response.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
