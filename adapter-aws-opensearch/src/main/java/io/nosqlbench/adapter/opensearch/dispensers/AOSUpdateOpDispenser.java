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
import io.nosqlbench.adapter.opensearch.ops.AOSUpdateOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.UpdateRequest;

import java.util.function.LongFunction;

public class AOSUpdateOpDispenser extends AOSBaseOpDispenser {

    public AOSUpdateOpDispenser(AOSAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
    }

    @Override
    public LongFunction<AOSUpdateOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        LongFunction<UpdateRequest.Builder> bfunc = l -> new UpdateRequest.Builder().index(targetF.apply(l));
        // TODO: add details here
        return l -> new AOSUpdateOp(clientF.apply(l),bfunc.apply(l).build(),Object.class);
    }

}
