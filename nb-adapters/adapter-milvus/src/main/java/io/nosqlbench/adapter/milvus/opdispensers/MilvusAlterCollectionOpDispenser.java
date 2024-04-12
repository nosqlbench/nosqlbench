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
import io.milvus.param.collection.AlterCollectionParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusAlterCollectionOp;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class MilvusAlterCollectionOpDispenser extends MilvusBaseOpDispenser<AlterCollectionParam> {

    public MilvusAlterCollectionOpDispenser(MilvusDriverAdapter adapter,
                                            ParsedOp op,
                                            LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<AlterCollectionParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<AlterCollectionParam.Builder> ebF =
            l -> AlterCollectionParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, "ttl", Number.class,
            (AlterCollectionParam.Builder b, Number n) -> b.withTTL(n.intValue()));

        final LongFunction<AlterCollectionParam.Builder> lastF = ebF;
        final LongFunction<AlterCollectionParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<AlterCollectionParam>> createOpFunc(
        LongFunction<AlterCollectionParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusAlterCollectionOp(clientF.apply(l), paramF.apply(l));
    }
}
