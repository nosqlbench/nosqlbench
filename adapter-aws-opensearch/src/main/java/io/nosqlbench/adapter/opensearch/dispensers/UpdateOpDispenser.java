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

package io.nosqlbench.adapter.opensearch.dispensers;

import io.nosqlbench.adapter.opensearch.OpenSearchAdapter;
import io.nosqlbench.adapter.opensearch.ops.CreateIndexOp;
import io.nosqlbench.adapter.opensearch.ops.UpdateOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.*;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;

import java.util.Map;
import java.util.function.LongFunction;

public class UpdateOpDispenser extends BaseOpenSearchOpDispenser {

    public UpdateOpDispenser(OpenSearchAdapter adapter, ParsedOp op) {
        super(adapter, op);
    }

    /**
     * {@see
     * <a href="https://docs.aws.amazon.com/opensearch-service/latest/developerguide/serverless-vector-search.html">doc
     * </a>}
     * <pre>{@code
     *           {
     *             "mappings": {
     *               "properties": {
     *                 "value": {
     *                   "type": "dense_vector",
     *                   "dims": TEMPLATE(dimensions, 25),
     *                   "index": true,
     *                   "similarity": "TEMPLATE(similarity_function, cosine)"
     *                 },
     *                 "key": {
     *                   "type": "text"
     *                 }
     *               }
     *             }
     *           }}</pre>
     *
     * @return
     */
    @Override
    public LongFunction<UpdateOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op) {
        return null;
//        LongFunction<UpdateRequest.Builder> bfunc = l -> new UpdateRequest.Builder();
//        op.getAsRequiredFunction("type")
//        return l -> new UpdateOp(clientF.apply(l),bfunc.apply(l).build());
//        bfunc = op.enhanceFunc(bfunc, "mappings", Map.class, this::resolveTypeMapping);
//
//        LongFunction<CreateIndexRequest.Builder> finalBfunc = bfunc;
//        return (long l) -> new CreateIndexOp(clientF.apply(l), finalBfunc.apply(l).build());
    }

}
