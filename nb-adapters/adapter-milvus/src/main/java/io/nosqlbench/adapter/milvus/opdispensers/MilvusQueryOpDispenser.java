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
import io.milvus.param.dml.QueryParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusQueryOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusQueryOpDispenser extends MilvusBaseOpDispenser<QueryParam> {

    public MilvusQueryOpDispenser(MilvusDriverAdapter adapter,
                                  ParsedOp op,
                                  LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<QueryParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<QueryParam.Builder> ebF =
            l -> QueryParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, List.of("partition_names", "partitions"), List.class,
            QueryParam.Builder::withPartitionNames);
        ebF = op.enhanceEnumOptionally(ebF, "consistency_level", ConsistencyLevelEnum.class, QueryParam.Builder::withConsistencyLevel);
        ebF = op.enhanceFuncOptionally(ebF, "expr", String.class, QueryParam.Builder::withExpr);
        ebF = op.enhanceFuncOptionally(ebF, "limit", Number.class, (QueryParam.Builder b, Number n) -> b.withLimit(n.longValue()));
        ebF = op.enhanceFuncOptionally(ebF, "offset", Number.class, (QueryParam.Builder b, Number n) -> b.withOffset(n.longValue()));
        ebF = op.enhanceFuncOptionally(ebF, "ignore_growing", Boolean.class, QueryParam.Builder::withIgnoreGrowing);
        ebF = op.enhanceFuncOptionally(ebF, "out_fields", List.class, QueryParam.Builder::withOutFields);

        final LongFunction<QueryParam.Builder> lastF = ebF;
        final LongFunction<QueryParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<QueryParam>> createOpFunc(
        LongFunction<QueryParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusQueryOp(clientF.apply(l), paramF.apply(l));
    }
}
