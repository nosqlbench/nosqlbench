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

    private final LongFunction<String> targetF;

    public UpdateOpDispenser(OpenSearchAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op);
        this.targetF = targetF;
    }

    @Override
    public LongFunction<UpdateOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op) {
        LongFunction<UpdateRequest.Builder> bfunc = l -> new UpdateRequest.Builder().index(targetF.apply(l));
        // TODO: add details here
        return l -> new UpdateOp(clientF.apply(l),bfunc.apply(l).build(),Object.class);
    }

}
