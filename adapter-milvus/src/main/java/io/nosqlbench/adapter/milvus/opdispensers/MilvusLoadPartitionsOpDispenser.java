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
import io.milvus.param.partition.LoadPartitionsParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusLoadPartitionsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusLoadPartitionsOpDispenser extends MilvusBaseOpDispenser<LoadPartitionsParam> {

    /**
     * TODO: Refactor this class after API refinements for more type and target variation
     *
     * @param adapter
     * @param op
     * @param targetFunction
     */
    public MilvusLoadPartitionsOpDispenser(MilvusDriverAdapter adapter,
                                           ParsedOp op,
                                           LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<LoadPartitionsParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<LoadPartitionsParam.Builder> ebF =
            l -> LoadPartitionsParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFunc(ebF, "partition_names", List.class, LoadPartitionsParam.Builder::withPartitionNames);
        ebF = op.enhanceFuncOptionally(ebF, "resource_groups", List.class, LoadPartitionsParam.Builder::withResourceGroups);
        ebF = op.enhanceFuncOptionally(ebF, "database_name", String.class, LoadPartitionsParam.Builder::withDatabaseName);
        ebF = op.enhanceFuncOptionally(ebF, "refresh", Boolean.class, LoadPartitionsParam.Builder::withRefresh);
        ebF = op.enhanceFuncOptionally(ebF, "database_name", String.class, LoadPartitionsParam.Builder::withDatabaseName);
        ebF = op.enhanceFuncOptionally(ebF, "replica_number", Integer.class, LoadPartitionsParam.Builder::withReplicaNumber);
        ebF = op.enhanceFuncOptionally(ebF,"sync_load",Boolean.class,LoadPartitionsParam.Builder::withSyncLoad);
        ebF = op.enhanceFuncOptionally(ebF,"sync_load_waiting_interval",Long.class,LoadPartitionsParam.Builder::withSyncLoadWaitingInterval);
        ebF = op.enhanceFuncOptionally(ebF,"sync_load_waiting_timeout",Long.class,
            LoadPartitionsParam.Builder::withSyncLoadWaitingTimeout);

        final LongFunction<LoadPartitionsParam.Builder> lastF = ebF;
        final LongFunction<LoadPartitionsParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<LoadPartitionsParam>> createOpFunc(
        LongFunction<LoadPartitionsParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusLoadPartitionsOp(clientF.apply(l), paramF.apply(l));
    }
}
