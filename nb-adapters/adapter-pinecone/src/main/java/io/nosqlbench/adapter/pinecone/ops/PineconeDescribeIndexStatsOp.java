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
import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.NamespaceSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class PineconeDescribeIndexStatsOp extends PineconeOp {

    private static final Logger logger = LogManager.getLogger(PineconeDescribeIndexStatsOp.class);

    private final DescribeIndexStatsRequest request;

    /**
     * Create a new {@link ParsedOp} encapsulating a call to the Pinecone client describeIndexStats method
     *
     * @param connection    The associated {@link PineconeConnection} used to communicate with the database
     * @param request       The {@link DescribeIndexStatsRequest} built for this operation
     */
    public PineconeDescribeIndexStatsOp(PineconeConnection connection, DescribeIndexStatsRequest request) {
        super(connection);
        this.request = request;
    }

     @Override
    public Object apply(long value) {
        DescribeIndexStatsResponse response = connection.getBlockingStub().describeIndexStats(request);
        if (logger.isDebugEnabled()) {
            logger.debug("Vector counts:");
            for (Map.Entry<String, NamespaceSummary> namespace : response.getNamespacesMap().entrySet()) {
                logger.debug(namespace.getKey() + ": " + namespace.getValue().getVectorCount());
            }
        }
        return response;
    }
}
