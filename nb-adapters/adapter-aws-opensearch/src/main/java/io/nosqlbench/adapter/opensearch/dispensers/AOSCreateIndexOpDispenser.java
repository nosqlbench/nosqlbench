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
import io.nosqlbench.adapter.opensearch.ops.AOSCreateIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.*;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;

import java.util.Map;
import java.util.function.LongFunction;

public class AOSCreateIndexOpDispenser extends AOSBaseOpDispenser {

    private final ParsedOp pop;
    private final int dimensions;
    private final int ef_construction;
    private final int m;

    public AOSCreateIndexOpDispenser(AOSAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
        this.pop = op;
        this.dimensions = pop.getStaticValue("dimensions",Integer.class).intValue();
        this.ef_construction = pop.getStaticValue("ef_construction",Integer.class).intValue();
        this.m = pop.getStaticValue("m",Integer.class).intValue();
    }

    @Override
    public LongFunction<AOSCreateIndexOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op,
                                                       LongFunction<String> targetF) {
        CreateIndexRequest.Builder eb = new CreateIndexRequest.Builder();
        LongFunction<CreateIndexRequest.Builder> bfunc =
            l -> new CreateIndexRequest.Builder()
                .settings(b -> b.knn(true))
                .index(targetF.apply(1));
        bfunc = op.enhanceFunc(bfunc, "mappings", Map.class, this::resolveTypeMapping);

        LongFunction<CreateIndexRequest.Builder> finalBfunc = bfunc;
        return (long l) -> new AOSCreateIndexOp(clientF.apply(l), finalBfunc.apply(l).build());
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
                            .dimension(dimensions)
                            .method(
                                new KnnVectorMethod.Builder()
                                    .name("hnsw")
                                    .engine("faiss")
                                    .spaceType("l2")
                                    .parameters(
                                        Map.of(
                                            "ef_construction", JsonData.of(ef_construction),
                                            "m", JsonData.of(m)
                                        )
                                    )
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
