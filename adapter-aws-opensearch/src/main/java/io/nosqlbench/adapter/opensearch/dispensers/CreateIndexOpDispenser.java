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
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.*;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;

import java.util.Map;
import java.util.function.LongFunction;

public class CreateIndexOpDispenser extends BaseOpenSearchOpDispenser {

    private final LongFunction<String> targetF;

    public CreateIndexOpDispenser(OpenSearchAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op);
        this.targetF = targetF;
    }

    @Override
    public LongFunction<CreateIndexOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op) {
        CreateIndexRequest.Builder eb = new CreateIndexRequest.Builder();
        LongFunction<CreateIndexRequest.Builder> bfunc =
            l -> new CreateIndexRequest.Builder()
                .settings(b -> b.knn(true))
                .index(targetF.apply(1));
        bfunc = op.enhanceFunc(bfunc, "mappings", Map.class, this::resolveTypeMapping);

        LongFunction<CreateIndexRequest.Builder> finalBfunc = bfunc;
        return (long l) -> new CreateIndexOp(clientF.apply(l), finalBfunc.apply(l).build());
    }

    // https://opensearch.org/docs/latest/search-plugins/knn/knn-index/
    private CreateIndexRequest.Builder resolveTypeMapping(CreateIndexRequest.Builder eb, Map<?, ?> mappings) {
        TypeMapping.Builder builder = new TypeMapping.Builder().properties(
                Map.of(
                    "key",
                    new Property.Builder()
                        .text(b -> b)
                        .build(),
                    "value",
                    new Property.Builder()
                        .knnVector(new KnnVectorProperty.Builder()
                            .dimension(25)
                            .method(
                                new KnnVectorMethod.Builder()
                                    .name("hnsw")
                                    .engine("faiss")
                                    .spaceType("l2")
                                    .parameters(Map.of("ef_construction", JsonData.of(256), "m", JsonData.of(8)))
                                    .build()
                            ).build()
                        ).build()
                ))
            .fieldNames(new FieldNamesField.Builder()
                .enabled(true).build()
            );
        return eb.mappings(b -> builder);
    }

}
