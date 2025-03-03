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
import io.milvus.param.credential.ListCredUsersParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusSpace;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusListCredUsersOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class MilvusListCredUsersOpDispenser extends MilvusBaseOpDispenser<ListCredUsersParam> {

    public MilvusListCredUsersOpDispenser(MilvusDriverAdapter adapter,
                                          ParsedOp op,
                                          LongFunction<String> targetFunction,
                                          LongFunction<MilvusSpace> spaceF
    ) {
        super(adapter, op, targetFunction, spaceF);
    }

    @Override
    public LongFunction<ListCredUsersParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> ListCredUsersParam.newBuilder().build();
    }

    @Override
    public LongFunction<MilvusBaseOp<ListCredUsersParam>> createOpFunc(
        LongFunction<ListCredUsersParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusListCredUsersOp(clientF.apply(l),paramF.apply(l));
    }
}
