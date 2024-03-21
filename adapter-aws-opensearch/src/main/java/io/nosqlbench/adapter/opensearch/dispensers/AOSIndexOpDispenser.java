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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapter.opensearch.AOSAdapter;
import io.nosqlbench.adapter.opensearch.ops.AOSIndexOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;

import java.util.function.LongFunction;

public class AOSIndexOpDispenser extends AOSBaseOpDispenser {
    private final static Logger logger = LogManager.getLogger(AOSIndexOpDispenser.class);
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String diag;

    public AOSIndexOpDispenser(AOSAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
        this.diag = op.getStaticConfigOr("daig","false");
    }

    @Override
    public LongFunction<? extends Op> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        LongFunction<IndexRequest> irqF = AOSRequests.index(op);
        return l -> new AOSIndexOp(clientF.apply(l), irqF.apply(l));
    }

}
