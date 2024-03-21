package io.nosqlbench.adapter.milvus.opdispensers;

/*
 * Copyright (c) 2024 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.DeleteParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusDeleteParamOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.milvus.param.dml.DeleteParam.Builder;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusDeleteOpDispenser extends MilvusBaseOpDispenser<DeleteParam> {
    public MilvusDeleteOpDispenser(MilvusDriverAdapter adapter,
                                   ParsedOp op,
                                   LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<DeleteParam> getParamFunc(LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        LongFunction<DeleteParam.Builder> f =
            l -> DeleteParam.newBuilder().withCollectionName(targetF.apply(l));
        f = op.enhanceFuncOptionally(f, List.of("partition_name","partition"), String.class,
            DeleteParam.Builder::withPartitionName);
        f = op.enhanceFuncOptionally(f, "expression", String.class, DeleteParam.Builder::withExpr);
        f = op.enhanceFuncOptionally(f, "expr", String.class, Builder::withExpr);
        LongFunction<DeleteParam.Builder> finalF = f;
        return l -> finalF.apply(l).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<DeleteParam>> createOpFunc(LongFunction<DeleteParam> paramF, LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> new MilvusDeleteParamOp(clientF.apply(l), paramF.apply(l));
    }

}
