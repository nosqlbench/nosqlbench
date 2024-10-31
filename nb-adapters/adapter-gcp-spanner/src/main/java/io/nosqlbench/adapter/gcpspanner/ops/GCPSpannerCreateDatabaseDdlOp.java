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

package io.nosqlbench.adapter.gcpspanner.ops;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.Spanner;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;

import java.util.Collections;

/**
 * This class represents an operation to create the database DDL (Data Definition Language) in Google Cloud Spanner.
 * It extends the {@link GCPSpannerBaseOp} class and provides the implementation for applying the DDL update operation.
 */
public class GCPSpannerCreateDatabaseDdlOp extends GCPSpannerBaseOp<Long,Database> {
    private final String databaseId;
    private final DatabaseAdminClient dbAdminClient;
    private final String instanceId;

    /**
     * Constructs a new {@link GCPSpannerUpdateDatabaseDdlOp}.
     *
     * @param searchIndexClient the {@link Spanner} client
     * @param requestParam the request parameter
     * @param databaseId the SQL statement to create the table
     * @param dbAdminClient the {@link DatabaseAdminClient} to execute the DDL update
     * @param instanceId the instance ID string representing the target spanner instance
     */
    public GCPSpannerCreateDatabaseDdlOp(Spanner searchIndexClient, Long requestParam, String databaseId,
                                         DatabaseAdminClient dbAdminClient, String instanceId) {
        super(searchIndexClient, requestParam);
        this.databaseId = databaseId;
        this.dbAdminClient = dbAdminClient;
        this.instanceId = instanceId;
    }

    /**
     * Applies the DDL create database operation.
     *
     * @param value the value to be used in the operation
     * @return the result of the operation
     * @throws RuntimeException if an error occurs during the operation
     */
    @Override
    public Database applyOp(long value) {
        OperationFuture<Database, CreateDatabaseMetadata> operation = dbAdminClient.createDatabase(
            instanceId, databaseId, Collections.emptyList());
        try {
            return operation.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
