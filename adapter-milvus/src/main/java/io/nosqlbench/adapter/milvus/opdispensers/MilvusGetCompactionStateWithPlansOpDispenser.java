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
import io.milvus.param.control.GetCompactionPlansParam;
import io.milvus.param.control.GetCompactionPlansParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusGetCompactionStateWithPlansOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class MilvusGetCompactionStateWithPlansOpDispenser extends MilvusBaseOpDispenser<GetCompactionPlansParam> {

    public MilvusGetCompactionStateWithPlansOpDispenser(MilvusDriverAdapter adapter,
                                                        ParsedOp op,
                                                        LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<GetCompactionPlansParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<GetCompactionPlansParam.Builder> ebF =
            l -> GetCompactionPlansParam.newBuilder().withCompactionID(Long.parseLong(targetF.apply(l)));

        final LongFunction<GetCompactionPlansParam.Builder> lastF = ebF;
        final LongFunction<GetCompactionPlansParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<GetCompactionPlansParam>> createOpFunc(
        LongFunction<GetCompactionPlansParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusGetCompactionStateWithPlansOp(clientF.apply(l),paramF.apply(l));
    }
}
