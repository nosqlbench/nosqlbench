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
import io.milvus.param.partition.ShowPartitionsParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusAdapterUtils;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusShowPartitionsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusShowPartitionsOpDispenser extends MilvusBaseOpDispenser<ShowPartitionsParam> {

    public MilvusShowPartitionsOpDispenser(MilvusDriverAdapter adapter,
                                           ParsedOp op,
                                           LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<ShowPartitionsParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<ShowPartitionsParam.Builder> ebF =
            l -> ShowPartitionsParam.newBuilder();

        LongFunction<List<String>> partitionsF = l -> MilvusAdapterUtils.splitNames(targetF.apply(l));
        LongFunction<ShowPartitionsParam.Builder> finalEbF = ebF;
        ebF = l -> finalEbF.apply(l).withPartitionNames(partitionsF.apply(l));
        ebF = op.enhanceFuncOptionally(ebF,List.of("collection_name","collection"),String.class,
            ShowPartitionsParam.Builder::withCollectionName);
        final LongFunction<ShowPartitionsParam.Builder> lastF = ebF;
        final LongFunction<ShowPartitionsParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<ShowPartitionsParam>> createOpFunc(
        LongFunction<ShowPartitionsParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusShowPartitionsOp(clientF.apply(l),paramF.apply(l));
    }
}
