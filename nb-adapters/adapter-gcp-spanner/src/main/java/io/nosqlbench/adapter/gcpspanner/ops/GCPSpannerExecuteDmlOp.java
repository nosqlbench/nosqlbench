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

import com.google.cloud.spanner.*;

/**
 * This class represents an operation to execute a DML statement on Google Cloud Spanner.
 * It extends the GCPSpannerBaseOp class and overrides the applyOp method to execute the DML statement.
 */
public class GCPSpannerExecuteDmlOp extends GCPSpannerBaseOp<Long> {
    private final Statement statement;
    private final DatabaseClient dbClient;

    /**
     * Constructs a new GCPSpannerExecuteDmlOp.
     *
     * @param spanner the Spanner instance
     * @param requestParam the request parameter
     * @param statement the DML statement to execute
     * @param dbClient the DatabaseClient to use for executing the statement
     */
    public GCPSpannerExecuteDmlOp(Spanner spanner, Long requestParam, Statement statement,
                                  DatabaseClient dbClient) {
        super(spanner, requestParam);
        this.statement = statement;
        this.dbClient = dbClient;
    }

    /**
     * Executes the DML statement using the provided value.
     *
     * @param value the value to use for the operation
     * @return the result of the DML execution
     */
    @Override
    public Object applyOp(long value) {
        try (ReadContext context = dbClient.singleUse()) {
            return context.executeQuery(statement);
        }
    }
}
