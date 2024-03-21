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

import io.nosqlbench.adapter.opensearch.AOSAdapter;
import io.nosqlbench.adapter.opensearch.ops.AOSDeleteIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;

import java.util.function.LongFunction;

public class AOSDeleteIndexOpDispenser extends AOSBaseOpDispenser {

    public AOSDeleteIndexOpDispenser(AOSAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
    }

    @Override
    public LongFunction<AOSDeleteIndexOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        DeleteIndexRequest.Builder eb = new DeleteIndexRequest.Builder();
        LongFunction<DeleteIndexRequest.Builder> f =
            l -> new DeleteIndexRequest.Builder().index(targetF.apply(l));
        return l -> new AOSDeleteIndexOp(clientF.apply(l),f.apply(1).build());
    }
}
