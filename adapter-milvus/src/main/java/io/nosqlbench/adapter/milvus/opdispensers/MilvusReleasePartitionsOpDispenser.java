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
import io.milvus.param.partition.ReleasePartitionsParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusAdapterUtils;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusReleasePartitionsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusReleasePartitionsOpDispenser extends MilvusBaseOpDispenser<ReleasePartitionsParam> {

    public MilvusReleasePartitionsOpDispenser(MilvusDriverAdapter adapter,
                                              ParsedOp op,
                                              LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<ReleasePartitionsParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<ReleasePartitionsParam.Builder> ebF =
            l -> ReleasePartitionsParam.newBuilder();
        LongFunction<List<String>> partNamesF = l -> MilvusAdapterUtils.splitNames(targetF.apply(l));

        LongFunction<ReleasePartitionsParam.Builder> finalEbF = ebF;
        ebF = l -> finalEbF.apply(l).withPartitionNames(partNamesF.apply(l));
        ebF = op.enhanceFuncOptionally(ebF,List.of("collection_name","collection"),String.class,
            ReleasePartitionsParam.Builder::withCollectionName);

        final LongFunction<ReleasePartitionsParam.Builder> lastF = ebF;
        final LongFunction<ReleasePartitionsParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<ReleasePartitionsParam>> createOpFunc(
        LongFunction<ReleasePartitionsParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusReleasePartitionsOp(clientF.apply(l),paramF.apply(l));
    }
}
