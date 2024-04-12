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
import io.milvus.param.control.GetFlushStateParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusGetFlushStateOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

public class MilvusGetFlushStateOpDispenser extends MilvusBaseOpDispenser<GetFlushStateParam> {

    public MilvusGetFlushStateOpDispenser(MilvusDriverAdapter adapter,
                                          ParsedOp op,
                                          LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<GetFlushStateParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<GetFlushStateParam.Builder> ebF =
            l -> GetFlushStateParam.newBuilder();

        // Add enhancement functions here
        LongFunction<List<Long>> idsF = l -> {
            List<Long> ids = Arrays.stream(targetF.apply(l).split("( +| *, *)"))
                .map(Long::valueOf).toList();
            return ids;
        };
        LongFunction<GetFlushStateParam.Builder> finalEbF = ebF;
        ebF = l -> finalEbF.apply(l).withSegmentIDs(idsF.apply(l));
        ebF = op.enhanceFuncOptionally(ebF, List.of("collection", "collection_name"), String.class,
            GetFlushStateParam.Builder::withCollectionName);
        ebF = op.enhanceFuncOptionally(ebF, "flush_ts", Number.class,
            (GetFlushStateParam.Builder b, Number n) -> b.withFlushTs(n.longValue()));

        final LongFunction<GetFlushStateParam.Builder> lastF = ebF;
        final LongFunction<GetFlushStateParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<GetFlushStateParam>> createOpFunc(
        LongFunction<GetFlushStateParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusGetFlushStateOp(clientF.apply(l), paramF.apply(l));
    }
}
