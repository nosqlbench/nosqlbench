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
import io.nosqlbench.adapter.opensearch.ops.DeleteOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.*;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;

import java.util.Map;
import java.util.function.LongFunction;

public class DeleteOpDispenser extends BaseOpenSearchOpDispenser {

    private final LongFunction<String> targetF;

    public DeleteOpDispenser(OpenSearchAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op);
        this.targetF = targetF;
    }

    @Override
    public LongFunction<DeleteOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op) {
        DeleteRequest.Builder eb = new DeleteRequest.Builder();
        LongFunction<DeleteRequest.Builder> bfunc = l -> new DeleteRequest.Builder().index(targetF.apply(l));
        return (long l) -> new DeleteOp(clientF.apply(l), bfunc.apply(l).build());
    }

}
