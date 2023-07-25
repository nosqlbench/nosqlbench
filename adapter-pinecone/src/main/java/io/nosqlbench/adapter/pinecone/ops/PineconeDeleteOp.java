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

import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.DeleteRequest;
import io.pinecone.proto.DeleteResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PineconeDeleteOp extends PineconeOp {

    private static final Logger logger = LogManager.getLogger(PineconeDeleteOp.class);

    private final DeleteRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client delete method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link DeleteRequest} built for this operation
     */
    public PineconeDeleteOp(PineconeConnection connection, DeleteRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public Object apply(long value) {
        DeleteResponse response = connection.getBlockingStub().delete(request);
        logger.debug("Pinecone delete request successful: " + response.toString());
        return response;
    }
}
