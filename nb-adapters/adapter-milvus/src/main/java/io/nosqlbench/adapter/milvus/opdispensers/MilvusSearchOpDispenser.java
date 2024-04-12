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
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusSearchOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusSearchOpDispenser extends MilvusBaseOpDispenser<SearchParam> {
    public MilvusSearchOpDispenser(
        MilvusDriverAdapter adapter,
        ParsedOp op,
        LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<SearchParam> getParamFunc(LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {


        LongFunction<SearchParam.Builder> ebF =
            l -> SearchParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, List.of("partition_names", "partitions"), List.class, SearchParam.Builder::withPartitionNames);
        ebF = op.enhanceFuncOptionally(ebF, "out_fields", List.class, SearchParam.Builder::withOutFields);


        ebF = op.enhanceEnumOptionally(ebF, "consistency_level", ConsistencyLevelEnum.class, SearchParam.Builder::withConsistencyLevel);
        ebF = op.enhanceFuncOptionally(ebF, "expr", String.class, SearchParam.Builder::withExpr);
        ebF = op.enhanceDefaultFunc(ebF, "top_k", Number.class, 100,
            (SearchParam.Builder b, Number n) -> b.withTopK(n.intValue()));
        ebF = op.enhanceEnumOptionally(ebF, "metric_type", MetricType.class, SearchParam.Builder::withMetricType);
        ebF = op.enhanceFuncOptionally(ebF, "round_decimal", Number.class,
            (SearchParam.Builder b, Number n) -> b.withRoundDecimal(n.intValue()));
        ebF = op.enhanceFuncOptionally(ebF, "ignore_growing", Boolean.class, SearchParam.Builder::withIgnoreGrowing);
        ebF = op.enhanceFuncOptionally(ebF, "params", String.class, SearchParam.Builder::withParams);
        ebF = op.enhanceFunc(ebF, List.of("vector_field_name", "vector_field"), String.class,
            SearchParam.Builder::withVectorFieldName);
        // TODO: sanity check List of Floats vs List of List of Floats at func construction time.
        ebF = op.enhanceFuncOptionally(ebF, "vectors", List.class, SearchParam.Builder::withVectors);
        ebF = op.enhanceFuncOptionally(ebF, "vector", List.class, (b, l) -> b.withVectors(List.of(l)));
        LongFunction<SearchParam.Builder> finalEbF = ebF;
        return l -> finalEbF.apply(l).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<SearchParam>> createOpFunc(
        LongFunction<SearchParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusSearchOp(clientF.apply(l), paramF.apply(l));
    }

}
