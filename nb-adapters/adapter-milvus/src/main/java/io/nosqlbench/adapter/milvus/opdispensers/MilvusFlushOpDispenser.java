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
import io.milvus.param.collection.FlushParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusFlushOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

public class MilvusFlushOpDispenser extends MilvusBaseOpDispenser<FlushParam> {

    public MilvusFlushOpDispenser(MilvusDriverAdapter adapter,
                                  ParsedOp op,
                                  LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<FlushParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {

        LongFunction<FlushParam.Builder> ebF =
            l -> FlushParam.newBuilder();

        LongFunction<List<String>> cnames = l -> {
            List<String> collectionNames = Arrays.asList(targetF.apply(l).split("[\\s,]"));
            return collectionNames;
        };
        LongFunction<FlushParam.Builder> finalEbF = ebF;
        ebF = l -> finalEbF.apply(l).withCollectionNames(cnames.apply(l));
        ebF = op.enhanceFuncOptionally(ebF, List.of("database_name", "database"), String.class,
            FlushParam.Builder::withDatabaseName);
        ebF = op.enhanceFuncOptionally(ebF, "sync_flush_waiting_interval", Number.class,
            (FlushParam.Builder b, Number n) -> b.withSyncFlushWaitingInterval(n.longValue()));
        ebF = op.enhanceFuncOptionally(ebF, "sync_flush_waiting_timeout", Number.class,
            (FlushParam.Builder b, Number n) -> b.withSyncFlushWaitingTimeout(n.longValue()));

        final LongFunction<FlushParam.Builder> lastF = ebF;
        final LongFunction<FlushParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<FlushParam>> createOpFunc(
        LongFunction<FlushParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusFlushOp(clientF.apply(l), paramF.apply(l));
    }
}
