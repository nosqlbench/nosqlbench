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
import io.nosqlbench.adapter.opensearch.ops.AOSKnnSearchOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class AOSKnnSearchOpDispenser extends AOSBaseOpDispenser {
    private Class<?> schemaClass;

    public AOSKnnSearchOpDispenser(AOSAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
        String schemaClassStr = op.getStaticConfigOr("schema", "io.nosqlbench.adapter.opensearch.pojos.Doc");
        try {
            schemaClass = Class.forName(schemaClassStr);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load schema class: " + schemaClassStr, e);
        }
    }

    @Override
    public LongFunction<AOSKnnSearchOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        LongFunction<KnnQuery.Builder> knnfunc = l -> new KnnQuery.Builder();
        knnfunc = op.enhanceFuncOptionally(knnfunc, "k",Integer.class, KnnQuery.Builder::k);
        knnfunc = op.enhanceFuncOptionally(knnfunc, "vector", List.class, this::convertVector);
        knnfunc = op.enhanceFuncOptionally(knnfunc, "field",String.class, KnnQuery.Builder::field);

        Optional<LongFunction<Map>> filterFunction = op.getAsOptionalFunction("filter", Map.class);
        if (filterFunction.isPresent()) {
            LongFunction<KnnQuery.Builder> finalFunc = knnfunc;
            LongFunction<Query> builtFilter = buildFilterQuery(filterFunction.get());
            knnfunc = l -> finalFunc.apply(l).filter(builtFilter.apply(l));
        }
        LongFunction<KnnQuery.Builder> finalKnnfunc = knnfunc;
        LongFunction<SearchRequest.Builder> bfunc =
            l -> new SearchRequest.Builder().size(op.getStaticValueOr("size", 100))
                .index(targetF.apply(l))
                .query(new Query.Builder().knn(finalKnnfunc.apply(l).build()).build());

        return (long l) -> new AOSKnnSearchOp(clientF.apply(l), bfunc.apply(l).build(), schemaClass);
    }

    private LongFunction<Query> buildFilterQuery(LongFunction<Map> mapLongFunction) {
        return l -> {
            Map<String,String> filterFields = mapLongFunction.apply(l);
            String field = filterFields.get("field");
            String comparator = filterFields.get("comparator");
            String value = filterFields.get("value");
            return switch (comparator) {
                case "gte" -> Query.of(f -> f
                        .bool(b -> b
                                .must(m -> m
                                        .range(r -> r
                                                .field(field)
                                                .gte(JsonData.of(Integer.valueOf(value)))))));
                case "lte" -> Query.of(f -> f
                        .bool(b -> b
                                .must(m -> m
                                        .range(r -> r
                                                .field(field)
                                                .lte(JsonData.of(Integer.valueOf(value)))))));
                case "eq" -> Query.of(f -> f
                        .bool(b -> b
                                .must(m -> m
                                        .term(t -> t
                                                .field(field)
                                                .value(FieldValue.of(value))))));
                default -> throw new RuntimeException("Invalid comparator specified");
            };
        };
    }

    private KnnQuery.Builder convertVector(KnnQuery.Builder builder, List list) {
        float[] vector = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            vector[i] = (float) list.get(i);
        }
        return builder.vector(vector);
    }
}
