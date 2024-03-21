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
import io.milvus.param.alias.AlterAliasParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusAlterAliasOp;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusCreateCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class MilvusAlterAliasOpDispenser extends MilvusBaseOpDispenser<AlterAliasParam> {
    private static final Logger logger = LogManager.getLogger(MilvusAlterAliasOpDispenser.class);

    public MilvusAlterAliasOpDispenser(MilvusDriverAdapter adapter,
                                       ParsedOp op,
                                       LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<AlterAliasParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<AlterAliasParam.Builder> ebF =
            l -> AlterAliasParam.newBuilder().withAlias(targetF.apply(l));
        // Add enhancement functions here

        ebF = op.enhanceFuncOptionally(
            ebF,List.of("collection_name","collection"),String.class,AlterAliasParam.Builder::withCollectionName);

        final LongFunction<AlterAliasParam.Builder> lastF = ebF;
        final LongFunction<AlterAliasParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<AlterAliasParam>> createOpFunc(
        LongFunction<AlterAliasParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusAlterAliasOp(clientF.apply(l),paramF.apply(l));
    }
}
