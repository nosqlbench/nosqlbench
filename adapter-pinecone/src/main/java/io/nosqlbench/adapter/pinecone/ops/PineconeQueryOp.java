/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.pinecone.ops;

import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.QueryRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.QueryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeQueryOp extends PineconeOp {

    private static final Logger LOGGER = LogManager.getLogger(PineconeQueryOp.class);

    private final QueryRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client query method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link QueryRequest} built for this operation
     */
    public PineconeQueryOp(PineconeConnection connection, QueryRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        try {
            QueryResponse response = connection.getBlockingStub().query(request);
            LOGGER.info("got query result ids: "
                + response.getResultsList().get(0).getMatchesList());
        } catch (Exception e) {
            LOGGER.error("Exception %s caught trying to do Query", e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }
}
