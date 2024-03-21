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
import io.milvus.grpc.ShowType;
import io.milvus.param.collection.ShowCollectionsParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.MilvusAdapterUtils;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusShowCollectionsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusShowCollectionsOpDispenser extends MilvusBaseOpDispenser<ShowCollectionsParam> {

    public MilvusShowCollectionsOpDispenser(MilvusDriverAdapter adapter,
                                            ParsedOp op,
                                            LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<ShowCollectionsParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<ShowCollectionsParam.Builder> ebF =
            l -> ShowCollectionsParam.newBuilder();
        LongFunction<List<String>> collectionsF = l -> MilvusAdapterUtils.splitNames(targetF.apply(l));
        LongFunction<ShowCollectionsParam.Builder> finalEbF = ebF;
        ebF = l -> finalEbF.apply(l).withCollectionNames(collectionsF.apply(l));
        ebF = op.enhanceFuncOptionally(ebF,List.of("database_name","database"),String.class,
            ShowCollectionsParam.Builder::withDatabaseName);
        ebF = op.enhanceEnumOptionally(ebF,"show_type", ShowType.class,ShowCollectionsParam.Builder::withShowType);
        logger.warn(this.getClass().getSimpleName() + " is deprecated, use get_loading_progress instead");

        final LongFunction<ShowCollectionsParam.Builder> lastF = ebF;
        final LongFunction<ShowCollectionsParam> collectionParamF = l -> lastF.apply(l).build();
        return collectionParamF;
    }

    @Override
    public LongFunction<MilvusBaseOp<ShowCollectionsParam>> createOpFunc(
        LongFunction<ShowCollectionsParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusShowCollectionsOp(clientF.apply(l),paramF.apply(l));
    }
}
