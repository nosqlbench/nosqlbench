/*
 * Copyright (c) 2023 nosqlbench
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
import io.pinecone.proto.UpdateRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpdateResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeUpdateOp extends PineconeOp {

    private static final Logger logger = LogManager.getLogger(PineconeUpdateOp.class);

    private final UpdateRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client update method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link UpdateRequest} built for this operation
     */
    public PineconeUpdateOp(PineconeConnection connection, UpdateRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public void run() {
        UpdateResponse response = connection.getBlockingStub().update(request);
        logger.debug("UpdateResponse successful: " + response.toString());
    }
}
