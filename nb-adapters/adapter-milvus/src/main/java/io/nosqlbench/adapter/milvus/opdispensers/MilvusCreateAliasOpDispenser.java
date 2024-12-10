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

package io.nosqlbench.adapter.milvus.opdispensers;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.alias.CreateAliasParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusSpace;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusCreateAliasOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusCreateAliasOpDispenser extends MilvusBaseOpDispenser<CreateAliasParam> {

    public MilvusCreateAliasOpDispenser(MilvusDriverAdapter adapter,
                                        ParsedOp op,
                                        LongFunction<String> targetFunction,
                                        LongFunction<MilvusSpace> spaceF
    ) {
        super(adapter, op, targetFunction,spaceF);
    }

    @Override
    public LongFunction<CreateAliasParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<CreateAliasParam.Builder> ebF =
            l -> CreateAliasParam.newBuilder().withAlias(targetF.apply(l));
        ebF = op.enhanceFuncOptionally(ebF, "collection",String.class,
            CreateAliasParam.Builder::withCollectionName);


        final LongFunction<CreateAliasParam.Builder> lastF = ebF;
        final LongFunction<CreateAliasParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<CreateAliasParam>> createOpFunc(
        LongFunction<CreateAliasParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusCreateAliasOp(clientF.apply(l),paramF.apply(l));
    }
}
