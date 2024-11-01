/*
 * Copyright (c) nosqlbench
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
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class QdrantSearchPointsOp extends QdrantBaseOp<SearchPoints,List<ScoredPoint>> {
    public QdrantSearchPointsOp(QdrantClient client, SearchPoints request) {
        super(client, request);
    }

    @Override
    public List<ScoredPoint> applyOp(long value) {
        List<ScoredPoint> response = null;
        try {
            logger.debug("[QdrantSearchPointsOp] Cycle {} has request: {}", value, request.toString());
            response = client.searchAsync(request).get();
            if (logger.isDebugEnabled()) {
                response.forEach(scoredPoint -> {
                    logger.debug("[QdrantSearchPointsOp] Scored Point: {}", scoredPoint.toString());
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
