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
import io.milvus.param.bulkinsert.BulkInsertParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusBulkInsertOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class MilvusBulkInsertOpDispenser extends MilvusBaseOpDispenser<BulkInsertParam> {

    public MilvusBulkInsertOpDispenser(MilvusDriverAdapter adapter,
                                       ParsedOp op,
                                       LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<BulkInsertParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<BulkInsertParam.Builder> ebF =
            l -> BulkInsertParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, "options", Map.class,
            (BulkInsertParam.Builder builder, Map map) -> {
                map.forEach((k, v) -> {
                    builder.withOption(k.toString(), v.toString());
                });
                return builder;
            }
        );
        ebF = op.enhanceFuncOptionally(ebF, "files", List.class, BulkInsertParam.Builder::withFiles);
        ebF = op.enhanceFuncOptionally(ebF, List.of("partition_name", "partition"), String.class,
            BulkInsertParam.Builder::withPartitionName);
        LongFunction<BulkInsertParam.Builder> finalEbF = ebF;
        return l -> finalEbF.apply(l).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<BulkInsertParam>> createOpFunc(
        LongFunction<BulkInsertParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusBulkInsertOp(clientF.apply(l), paramF.apply(l));
    }
}
