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
import io.milvus.param.collection.LoadCollectionParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusLoadCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusLoadCollectionOpDispenser extends MilvusBaseOpDispenser<LoadCollectionParam> {

    public MilvusLoadCollectionOpDispenser(MilvusDriverAdapter adapter,
                                           ParsedOp op,
                                           LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<LoadCollectionParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<LoadCollectionParam.Builder> ebF =
            l -> LoadCollectionParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, List.of("database_name", "database"), String.class,
            LoadCollectionParam.Builder::withDatabaseName);

        ebF = op.enhanceFuncOptionally(ebF, "refresh", Boolean.class, LoadCollectionParam.Builder::withRefresh);
        ebF = op.enhanceFuncOptionally(ebF, "sync_load", Boolean.class, LoadCollectionParam.Builder::withSyncLoad);
        ebF = op.enhanceFuncOptionally(ebF, "replica_number", Number.class,
            (LoadCollectionParam.Builder b, Number n) -> b.withReplicaNumber(n.intValue()));
        ebF = op.enhanceFuncOptionally(ebF, "resource_groups", List.class, LoadCollectionParam.Builder::withResourceGroups);
        ebF = op.enhanceFuncOptionally(ebF, "sync_load_waiting_interval", Number.class,
            (LoadCollectionParam.Builder b, Number n) -> b.withSyncLoadWaitingInterval(n.longValue()));
        ebF = op.enhanceFuncOptionally(ebF, "sync_load_waiting_timeout", Number.class,
            (LoadCollectionParam.Builder b, Number n) -> b.withSyncLoadWaitingTimeout(n.longValue()));

        final LongFunction<LoadCollectionParam.Builder> lastF = ebF;
        final LongFunction<LoadCollectionParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<LoadCollectionParam>> createOpFunc(
        LongFunction<LoadCollectionParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusLoadCollectionOp(clientF.apply(l), paramF.apply(l));
    }
}
