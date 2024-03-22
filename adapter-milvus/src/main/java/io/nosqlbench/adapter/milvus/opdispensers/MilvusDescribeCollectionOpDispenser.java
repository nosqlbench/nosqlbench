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
import io.milvus.param.collection.DescribeCollectionParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusDescribeCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusDescribeCollectionOpDispenser extends MilvusBaseOpDispenser<DescribeCollectionParam> {

    public MilvusDescribeCollectionOpDispenser(MilvusDriverAdapter adapter,
                                               ParsedOp op,
                                               LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<DescribeCollectionParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<DescribeCollectionParam.Builder> ebF =
            l -> DescribeCollectionParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, List.of("database","database_name"),String.class,
            DescribeCollectionParam.Builder::withDatabaseName);

        final LongFunction<DescribeCollectionParam.Builder> lastF = ebF;
        final LongFunction<DescribeCollectionParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<DescribeCollectionParam>> createOpFunc(
        LongFunction<DescribeCollectionParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusDescribeCollectionOp(clientF.apply(l),paramF.apply(l));
    }
}
