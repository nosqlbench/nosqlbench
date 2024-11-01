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
import io.qdrant.client.grpc.Points.CountPoints;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class QdrantCountPointsOp extends QdrantBaseOp<CountPoints,Long> {
    public QdrantCountPointsOp(QdrantClient client, CountPoints request) {
        super(client, request);
    }

    @Override
    public Long applyOp(long value) {
        long result;
        try {
            boolean hasFilters = request.getFilter() != null && (request.getFilter().getMustCount() > 0
                || request.getFilter().getMustNotCount() > 0 || request.getFilter().getShouldCount() > 0);
            result = client.countAsync(
                request.getCollectionName(),
                (hasFilters) ? request.getFilter() : null,
                request.getExact(),
                Duration.ofMinutes(5) // opinionated default of 5 minutes for timeout
            ).get();
            logger.info("[QdrantCountPointsOp] Total vector points counted: {}", result);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
