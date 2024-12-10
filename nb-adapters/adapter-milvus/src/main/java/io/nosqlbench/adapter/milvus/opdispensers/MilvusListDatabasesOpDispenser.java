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
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusSpace;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusListDatabasesOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class MilvusListDatabasesOpDispenser extends MilvusBaseOpDispenser<Object> {

    public MilvusListDatabasesOpDispenser(MilvusDriverAdapter adapter,
                                          ParsedOp op,
                                          LongFunction<String> targetFunction,
                                          LongFunction<MilvusSpace> spaceF
    ) {
        super(adapter, op, targetFunction, spaceF);
    }

    @Override
    public LongFunction<Object> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> "";
    }

    @Override
    public LongFunction<MilvusBaseOp<Object>> createOpFunc(LongFunction<Object> paramF, LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> new MilvusListDatabasesOp(clientF.apply(l),null);

    }
}
