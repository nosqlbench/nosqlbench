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
import io.nosqlbench.adapter.opensearch.OpenSearchAdapter;
import io.nosqlbench.adapter.opensearch.ops.BulkOp;
import io.nosqlbench.adapter.opensearch.ops.IndexOp;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpDef;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpDef;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpType;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.VersionType;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class BulkOpDispenser extends BaseOpenSearchOpDispenser {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public BulkOpDispenser(OpenSearchAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
    }

    @Override
    public LongFunction<? extends Op> createOpFunc(
        LongFunction<OpenSearchClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<BulkRequest> func = OpenSearchRequests.bulk(op,targetF);
        return l -> new BulkOp(clientF.apply(l), func.apply(l));
    }

}
