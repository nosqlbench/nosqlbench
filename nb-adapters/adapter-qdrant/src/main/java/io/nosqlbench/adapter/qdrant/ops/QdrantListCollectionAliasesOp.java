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
import io.qdrant.client.grpc.Collections.ListCollectionAliasesRequest;

import java.time.Duration;
import java.util.List;

public class QdrantListCollectionAliasesOp extends QdrantBaseOp<ListCollectionAliasesRequest,List<String>> {
    public QdrantListCollectionAliasesOp(QdrantClient client, ListCollectionAliasesRequest request) {
        super(client, request);
    }

    @Override
    public List<String> applyOp(long value) {
        List<String> response;
        try {
            response = client.listCollectionAliasesAsync(request.getCollectionName(), Duration.ofSeconds(600)).get();
            logger.info("Collection aliases are {}", response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
