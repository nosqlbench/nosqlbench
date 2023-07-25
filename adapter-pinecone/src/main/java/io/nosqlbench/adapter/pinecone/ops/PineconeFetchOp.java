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
import io.pinecone.proto.FetchRequest;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class PineconeFetchOp extends PineconeOp {

    private static final Logger logger = LogManager.getLogger(PineconeFetchOp.class);

    private final FetchRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client fetch method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link FetchRequest} built for this operation
     */
    public PineconeFetchOp(PineconeConnection connection, FetchRequest request) {
        super(connection);
        this.request = request;
    }

    @Override
    public Object apply(long value) {
        FetchResponse response = connection.getBlockingStub().fetch(request);
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, Vector> vectors: response.getVectorsMap().entrySet()) {
                logger.debug(vectors.getKey() + ": " + vectors.getValue().toString());
            }
        }
        return response;
    }
}
