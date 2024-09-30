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
 *
 */

package io.nosqlbench.adapter.gcpspanner.ops;

import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Mutation;

import java.util.Collections;

/**
 * This class represents an operation to insert a vector into a Google Cloud Spanner database.
 * It extends the GCPSpannerBaseOp class and provides the implementation for the applyOp method.
 */
public class GCPSpannerInsertOp extends GCPSpannerBaseOp<Long> {
    private final Mutation mutation;
    private final DatabaseClient dbClient;

    /**
     * Constructs a new GCPSpannerInsertVectorOp.
     *
     * @param searchIndexClient the Spanner client used to interact with the database
     * @param requestParam the request parameter
     * @param mutation the Mutation object representing the data to be inserted
     * @param dbClient the DatabaseClient used to execute the mutation
     */
    public GCPSpannerInsertOp(Spanner searchIndexClient, Long requestParam, Mutation mutation, DatabaseClient dbClient) {
        super(searchIndexClient, requestParam);
        this.mutation = mutation;
        this.dbClient = dbClient;
    }

    /**
     * Applies the insert operation using the provided mutation.
     *
     * @param value the value to be used in the operation
     * @return the result of the write operation
     */
    @Override
    public Object applyOp(long value) {
        return dbClient.write(Collections.singletonList(mutation));
    }
}
