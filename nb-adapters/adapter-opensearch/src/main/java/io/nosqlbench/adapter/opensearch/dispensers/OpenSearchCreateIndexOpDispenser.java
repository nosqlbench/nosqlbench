/*
 * Copyright (c) nosqlbench
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
import io.nosqlbench.adapter.opensearch.ops.OpenSearchBaseOp;
import io.nosqlbench.adapter.opensearch.ops.OpenSearchCreateIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.*;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class OpenSearchCreateIndexOpDispenser extends OpenSearchBaseOpDispenser {

    private final ParsedOp pop;
    private final int dimensions;
    private final int ef_construction;
    private final int m;

    public OpenSearchCreateIndexOpDispenser(OpenSearchAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op, targetF);
        this.pop = op;
        // Make vector parameters optional with defaults
        Integer dimensionsValue = pop.getStaticValue("dimensions", Integer.class);
        this.dimensions = dimensionsValue != null ? dimensionsValue.intValue() : 128;

        Integer efConstructionValue = pop.getStaticValue("ef_construction", Integer.class);
        this.ef_construction = efConstructionValue != null ? efConstructionValue.intValue() : 512;

        Integer mValue = pop.getStaticValue("m", Integer.class);
        this.m = mValue != null ? mValue.intValue() : 16;
    }

    @Override
    public LongFunction<? extends OpenSearchBaseOp> createOpFunc(LongFunction<OpenSearchClient> clientF, ParsedOp op,
                                                       LongFunction<String> targetF) {
        CreateIndexRequest.Builder eb = new CreateIndexRequest.Builder();
        LongFunction<CreateIndexRequest.Builder> bfunc =
            l -> new CreateIndexRequest.Builder()
                .settings(b -> b.knn(true))
                .index(targetF.apply(1));
        bfunc = op.enhanceFuncOptionally(bfunc, "mappings", Map.class, this::resolveTypeMapping);

        LongFunction<CreateIndexRequest.Builder> finalBfunc = bfunc;
        return (long l) -> new OpenSearchCreateIndexOp(clientF.apply(l), finalBfunc.apply(l).build());
    }

    // https://opensearch.org/docs/latest/search-plugins/knn/knn-index/
    private CreateIndexRequest.Builder resolveTypeMapping(CreateIndexRequest.Builder eb, Map<?, ?> mappings) {
        // Always use the mappings provided in the workload
        // Convert the Map to OpenSearch Property objects
        Map<String, Property> properties = new HashMap<>();
        if (mappings.containsKey("properties")) {
            Map<?, ?> props = (Map<?, ?>) mappings.get("properties");
            for (Map.Entry<?, ?> entry : props.entrySet()) {
                String fieldName = entry.getKey().toString();
                Map<?, ?> fieldDef = (Map<?, ?>) entry.getValue();
                String type = fieldDef.get("type").toString();

                Property.Builder propBuilder = new Property.Builder();
                switch (type) {
                    case "text":
                        propBuilder.text(t -> t);
                        break;
                    case "keyword":
                        propBuilder.keyword(k -> k);
                        break;
                    case "date":
                        propBuilder.date(d -> d);
                        break;
                    case "long":
                        propBuilder.long_(l -> l);
                        break;
                    case "integer":
                        propBuilder.integer(i -> i);
                        break;
                    case "float":
                        propBuilder.float_(f -> f);
                        break;
                    case "double":
                        propBuilder.double_(d -> d);
                        break;
                    case "boolean":
                        propBuilder.boolean_(b -> b);
                        break;
                    case "knn_vector":
                        // Handle KNN vector fields with custom configuration
                        KnnVectorProperty.Builder knnBuilder = new KnnVectorProperty.Builder();

                        // Get dimension from field definition or fall back to global dimensions
                        Integer fieldDimension = null;
                        if (fieldDef.containsKey("dimension")) {
                            fieldDimension = Integer.valueOf(fieldDef.get("dimension").toString());
                        }
                        int finalDimension = fieldDimension != null ? fieldDimension : dimensions;
                        knnBuilder.dimension(finalDimension);

                        // Handle method configuration if present
                        if (fieldDef.containsKey("method")) {
                            Map<?, ?> methodDef = (Map<?, ?>) fieldDef.get("method");
                            KnnVectorMethod.Builder methodBuilder = new KnnVectorMethod.Builder();

                            if (methodDef.containsKey("name")) {
                                methodBuilder.name(methodDef.get("name").toString());
                            }
                            if (methodDef.containsKey("space_type")) {
                                methodBuilder.spaceType(methodDef.get("space_type").toString());
                            }
                            if (methodDef.containsKey("engine")) {
                                methodBuilder.engine(methodDef.get("engine").toString());
                            }
                            if (methodDef.containsKey("parameters")) {
                                Map<?, ?> paramsDef = (Map<?, ?>) methodDef.get("parameters");
                                Map<String, JsonData> params = new HashMap<>();
                                for (Map.Entry<?, ?> paramEntry : paramsDef.entrySet()) {
                                    String paramKey = paramEntry.getKey().toString();
                                    Object paramValue = paramEntry.getValue();
                                    if (paramValue instanceof Number) {
                                        params.put(paramKey, JsonData.of(((Number) paramValue).intValue()));
                                    } else {
                                        params.put(paramKey, JsonData.of(paramValue.toString()));
                                    }
                                }
                                methodBuilder.parameters(params);
                            }
                            knnBuilder.method(methodBuilder.build());
                        } else {
                            // Use default method configuration
                            KnnVectorMethod.Builder defaultMethodBuilder = new KnnVectorMethod.Builder()
                                .name("hnsw")
                                .engine("faiss")
                                .spaceType("l2")
                                .parameters(
                                    Map.of(
                                        "ef_construction", JsonData.of(ef_construction),
                                        "m", JsonData.of(m)
                                    )
                                );
                            knnBuilder.method(defaultMethodBuilder.build());
                        }

                        propBuilder.knnVector(knnBuilder.build());
                        break;
                    default:
                        propBuilder.text(t -> t); // Default to text
                }
                properties.put(fieldName, propBuilder.build());
            }
        }

        TypeMapping.Builder builder = new TypeMapping.Builder().properties(properties);
        return eb.mappings(builder.build());
    }

}
