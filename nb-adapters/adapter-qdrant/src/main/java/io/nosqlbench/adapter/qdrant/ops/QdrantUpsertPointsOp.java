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
import io.qdrant.client.grpc.Points.UpdateResult;
import io.qdrant.client.grpc.Points.UpsertPoints;

import java.util.concurrent.ExecutionException;

public class QdrantUpsertPointsOp extends QdrantBaseOp<UpsertPoints,UpdateResult> {
    public QdrantUpsertPointsOp(QdrantClient client, UpsertPoints request) {
        super(client, request);
    }

    @Override
    public UpdateResult applyOp(long value) {
        UpdateResult response = null;
        String responseStatus;
        long responseOperationId;
        try {
            logger.debug("[QdrantUpsertPointsOp] Cycle {} has Request: {}", value, request.toString());
            response = client.upsertAsync(request).get();
            responseStatus = response.getStatus().toString();
            responseOperationId = response.getOperationId();
            switch(response.getStatus()) {
                case Completed, Acknowledged ->
                    logger.trace("[QdrantUpsertPointsOp] Upsert points finished successfully." +
                    " [Status ({}) for Operation id ({})]", responseStatus, responseOperationId);
                case UnknownUpdateStatus, ClockRejected ->
                    logger.error("[QdrantUpsertPointsOp] Upsert points failed with status '{}'" +
                        " for operation id '{}'", responseStatus, responseOperationId);
                default ->
                    logger.error("[QdrantUpsertPointsOp] Unknown status '{}' for operation id '{}'", responseStatus, responseOperationId);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
