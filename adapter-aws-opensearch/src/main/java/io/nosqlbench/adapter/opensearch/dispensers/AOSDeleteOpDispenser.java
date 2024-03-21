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
import io.nosqlbench.adapter.opensearch.ops.AOSDeleteOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteRequest;

import java.util.function.LongFunction;

public class AOSDeleteOpDispenser extends AOSBaseOpDispenser {

    public AOSDeleteOpDispenser(AOSAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
    }

    @Override
    public LongFunction<AOSDeleteOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        DeleteRequest.Builder eb = new DeleteRequest.Builder();
        LongFunction<DeleteRequest.Builder> bfunc = l -> new DeleteRequest.Builder().index(targetF.apply(l));
        return (long l) -> new AOSDeleteOp(clientF.apply(l), bfunc.apply(l).build());
    }

}
