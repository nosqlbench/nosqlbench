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

package io.nosqlbench.adapter.milvus.opdispensers;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.MetricType;
import io.milvus.param.dml.SearchParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusSearchOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class MilvusSearchOpDispenser extends MilvusOpDispenser {
    private static final Logger logger = LogManager.getLogger(MilvusSearchOpDispenser.class);

    /**
     * Create a new {@ link MilvusSearchOpDispenser} subclassed from {@link MilvusOpDispenser}.
     *
     * @param adapter        The associated {@link MilvusDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusSearchOpDispenser(
        MilvusDriverAdapter adapter,
        ParsedOp op,
        LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<MilvusSearchOp> createOpFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF) {
        LongFunction<SearchParam.Builder> f =
            l -> SearchParam.newBuilder().withCollectionName(targetF.apply(l));
        f = op.enhanceFuncOptionally(f, "partition_name", String.class, SearchParam.Builder::addPartitionName);
        if(op.isDefined("partition_names", List.class)) {
            LongFunction<List<String>> partitionNamesF = createPartitionNamesF(op);
            LongFunction<SearchParam.Builder> finalF = f;
            f = l -> finalF.apply(l).withPartitionNames(partitionNamesF.apply(l));
            f = op.enhanceFuncOptionally(f, "partition_names", List.class, SearchParam.Builder::withPartitionNames);
        }
        f = op.enhanceFuncOptionally(f, "out_field", String.class, SearchParam.Builder::addOutField);
        if(op.isDefined("out_fields", List.class)) {
            LongFunction<List<String>> outFieldsF = createOutFieldsF(op);
            LongFunction<SearchParam.Builder> finalF1 = f;
            f = l -> finalF1.apply(l).withOutFields(outFieldsF.apply(l));
        }

        f = op.enhanceEnumOptionally(f, "consistency_level", ConsistencyLevelEnum.class, SearchParam.Builder::withConsistencyLevel);
        f = op.enhanceFuncOptionally(f, "expr", String.class, SearchParam.Builder::withExpr);
        f = op.enhanceDefaultFunc(f, "top_k", Integer.class, 100, SearchParam.Builder::withTopK);
        f = op.enhanceDefaultFunc(f, "metric_type", MetricType.class, MetricType.COSINE, SearchParam.Builder::withMetricType);
        f = op.enhanceFuncOptionally(f, "round_decimal", Integer.class, SearchParam.Builder::withRoundDecimal);
        f = op.enhanceFuncOptionally(f, "ignore_growing", Boolean.class, SearchParam.Builder::withIgnoreGrowing);
        f = op.enhanceFuncOptionally(f, "params", String.class, SearchParam.Builder::withParams);

        f = op.enhanceFunc(f, "vector_field_name", String.class, SearchParam.Builder::withVectorFieldName);
        LongFunction<List<?>> queryVectorsF = createQueryVectorsF(op);
        LongFunction<SearchParam.Builder> finalF2 = f;
        f = l -> finalF2.apply(l).withVectors(queryVectorsF.apply(l));

        LongFunction<SearchParam.Builder> searchParamsF = f;
        LongFunction<MilvusSearchOp> searchOpF = l -> new MilvusSearchOp(clientF.apply(l), searchParamsF.apply(l).build());
        return searchOpF;
    }

    /**
     * Prepare the {@code query_vectors} parameter list for the search operation.
     * @param op {@link ParsedOp}
     * @return {@link LongFunction<List<?>>}
     */
    private LongFunction<List<?>> createQueryVectorsF(ParsedOp op) {
        LongFunction<Map> outVectorF = op.getAsRequiredFunction("vectors", Map.class);
        LongFunction<List<?>> outFieldsListF = l -> {
            Map<String, Object> fieldmap = outVectorF.apply(l);
            //List<?> floatVectorList = new ArrayList<Float>();
            //List<?> byteBufferVectorList = new ArrayList<>();
            List<Object> finalVectorList = new ArrayList<>();
            fieldmap.forEach((name, value) -> {
                // TODO - validate if we really need to do these type checking here or let the DB barf at us if we
                // use it otherwise https://milvus.io/api-reference/java/v2.3.x/Query%20and%20Search/search().md
//                if(value instanceof Float) {
//                    floatVectorList.add((Float) value);
//                } else {
//                    byteBufferVectorList.add((ByteBuffer) value);
//                }
                finalVectorList.add(value);
            });
            return finalVectorList;
        };
        return outFieldsListF;
    }

    /**
     * Prepare the {@code out_fields} parameter list for the search operation.
     * @param op {@link ParsedOp}
     * @return {@link LongFunction<List<String>>}
     */
    private LongFunction<List<String>> createOutFieldsF(ParsedOp op) {
        LongFunction<Map> outFieldDataF = op.getAsRequiredFunction("out_fields", Map.class);
        LongFunction<List<String>> outFieldsListF = l -> {
            Map<String, Object> fieldmap = outFieldDataF.apply(l);
            List<String> fields = new ArrayList<>();
            fieldmap.forEach((name, value) -> {
                fields.add(String.valueOf(value));
            });
            return fields;
        };
        return outFieldsListF;
    }

    /**
     * Prepare the {@code partition_names} parameter list for the search operation.
     * @param op {@link ParsedOp}
     * @return {@link LongFunction<List<String>>}
     */
    private LongFunction<List<String>> createPartitionNamesF(ParsedOp op) {
        LongFunction<Map> partitionNamesDataF = op.getAsRequiredFunction("partition_names", Map.class);
        LongFunction<List<String>> partitionNamesListF = l -> {
            Map<String, Object> fieldmap = partitionNamesDataF.apply(l);
            List<String> fields = new ArrayList<>();
            fieldmap.forEach((name, value) -> {
                fields.add(String.valueOf(value));
            });
            return fields;
        };
        return partitionNamesListF;
    }
}
