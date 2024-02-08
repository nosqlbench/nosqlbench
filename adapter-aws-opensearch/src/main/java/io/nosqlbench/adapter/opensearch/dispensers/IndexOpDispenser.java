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
import io.nosqlbench.adapter.opensearch.ops.IndexOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpType;
import org.opensearch.client.opensearch._types.VersionType;
import org.opensearch.client.opensearch.core.IndexRequest;

import java.util.Map;
import java.util.function.LongFunction;

public class IndexOpDispenser extends BaseOpenSearchOpDispenser {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final LongFunction<String> targetF;

    public IndexOpDispenser(OpenSearchAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op);
        this.targetF =targetF;
    }

    @Override
    public LongFunction<? extends Op> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op) {
        LongFunction<IndexRequest.Builder<?>> func = l -> new IndexRequest.Builder<>();
        func = op.enhanceFuncOptionally(func, "index",String.class, IndexRequest.Builder::index);
        func = op.enhanceFuncOptionally(func,"id",String.class, IndexRequest.Builder::id);
        func = op.enhanceFuncOptionally(func,"ifPrimaryTerm",long.class, IndexRequest.Builder::ifPrimaryTerm);
        func = op.enhanceFuncOptionally(func,"ifSeqNo",long.class,IndexRequest.Builder::ifSeqNo);
        func = op.enhanceFuncOptionally(func,"pipeline", String.class, IndexRequest.Builder::pipeline);
        func = op.enhanceFuncOptionally(func,"routing", String.class, IndexRequest.Builder::routing);
        func = op.enhanceFuncOptionally(func,"requireAlias", boolean.class, IndexRequest.Builder::requireAlias);
        func = op.enhanceFuncOptionally(func,"version", long.class, IndexRequest.Builder::version);
        func = op.enhanceEnumOptionally(func,"opType", OpType.class,IndexRequest.Builder::opType);
        func = op.enhanceEnumOptionally(func,"versionType", VersionType.class,IndexRequest.Builder::versionType);
        func = op.enhanceFunc(func,"document",Object.class,(b1,d) -> this.bindDocument(b1,d));
        // TODO: func = op.enhanceFuncOptionally(func,"timeout",) ...

        LongFunction<IndexRequest.Builder<?>> finalFunc = func;
        return l -> new IndexOp(clientF.apply(l), finalFunc.apply(l).build());
    }

    private IndexRequest.Builder<?> bindDocument(IndexRequest.Builder builder, Object docdata) {
        String document = gson.toJson(docdata);
        return builder.document(document);
    }

}
