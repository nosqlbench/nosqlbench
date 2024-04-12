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
 */

package io.nosqlbench.adapter.opensearch.ops;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;

import java.io.IOException;

public class AOSIndexOp extends AOSBaseOp {
    private final static Logger logger = LogManager.getLogger(AOSIndexOp.class);
    private final IndexRequest<?> rq;

    public AOSIndexOp(OpenSearchClient client, IndexRequest<?> rq) {
        super(client);
        this.rq = rq;
    }


    public Object applyOp(long value) throws IOException {
        IndexResponse response = client.index(rq);
        logger.debug("IndexResponse:" + response);
        return response;
    }
}
