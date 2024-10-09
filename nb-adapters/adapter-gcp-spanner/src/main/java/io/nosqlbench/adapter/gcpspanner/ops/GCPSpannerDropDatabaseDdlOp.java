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

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseNotFoundException;
import com.google.cloud.spanner.Spanner;

/**
 * This class represents an operation to Drop the database DDL (Data Definition Language) in Google Cloud Spanner.
 * It extends the {@link GCPSpannerBaseOp} class and provides the implementation for applying the DDL update operation.
 */
public class GCPSpannerDropDatabaseDdlOp extends GCPSpannerBaseOp<Long> {
    private final String databaseId;
    private final DatabaseAdminClient dbAdminClient;
    private final Database db;

    /**
     * Constructs a new {@link GCPSpannerUpdateDatabaseDdlOp}.
     *
     * @param searchIndexClient the {@link Spanner} client
     * @param requestParam the request parameter
     * @param databaseId the database ID to be dropped
     * @param dbAdminClient the {@link DatabaseAdminClient} to execute the DDL update
     * @param db the {@link Database} to be dropped
     */
    public GCPSpannerDropDatabaseDdlOp(Spanner searchIndexClient, Long requestParam, String databaseId,
                                         DatabaseAdminClient dbAdminClient, Database db) {
        super(searchIndexClient, requestParam);
        this.databaseId = databaseId;
        this.dbAdminClient = dbAdminClient;
        this.db = db;
    }

    /**
     * Applies the DDL drop operation.
     *
     * @param value the value to be used in the operation
     * @return the result of the operation
     * @throws RuntimeException if an error occurs during the operation
     */
    @Override
    public Object applyOp(long value) {
        try {
            if (null != db && db.exists()) {
                db.drop();
            }
        } catch (Exception e) {
            logger.warn("Error dropping database using the Database object: {}. Will re-try using the DBAdminClient now...", e.getMessage());
            try {
                if (null != dbAdminClient) {
                    dbAdminClient.dropDatabase(databaseId, null);
                }
            } catch (DatabaseNotFoundException noDB) {
                logger.info("Database does not exist. {}", noDB.getMessage());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }
}
