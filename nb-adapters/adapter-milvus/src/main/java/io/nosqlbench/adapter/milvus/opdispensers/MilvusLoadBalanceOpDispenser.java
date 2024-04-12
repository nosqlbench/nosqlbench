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
import io.milvus.param.control.LoadBalanceParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusLoadBalanceOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class MilvusLoadBalanceOpDispenser extends MilvusBaseOpDispenser<LoadBalanceParam> {

    public MilvusLoadBalanceOpDispenser(MilvusDriverAdapter adapter,
                                        ParsedOp op,
                                        LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<LoadBalanceParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<LoadBalanceParam.Builder> ebF =
            l -> LoadBalanceParam.newBuilder();
        LongFunction<String> segmentsStringF = op.getAsRequiredFunction("segment_ids");
//        ebF = l -> ebF.apply(l).withSegmentIDs(MilvusAdapterUtils.splitLongs(segmentsStringF.apply(l)));

        throw new RuntimeException("implement me");
        // todo: implement me

//        final LongFunction<LoadBalanceParam.Builder> lastF = ebF;
//        final LongFunction<LoadBalanceParam> collectionParamF = l -> lastF.apply(l).build();
//        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<LoadBalanceParam>> createOpFunc(
        LongFunction<LoadBalanceParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusLoadBalanceOp(clientF.apply(l), paramF.apply(l));
    }
}
