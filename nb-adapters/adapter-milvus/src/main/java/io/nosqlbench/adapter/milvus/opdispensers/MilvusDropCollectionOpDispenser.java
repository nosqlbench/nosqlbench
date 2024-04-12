package io.nosqlbench.adapter.milvus.opdispensers;

/*
 * Copyright (c) 2022 nosqlbench
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
import io.milvus.param.collection.DropCollectionParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusDropCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusDropCollectionOpDispenser extends MilvusBaseOpDispenser<DropCollectionParam> {
    private static final Logger logger = LogManager.getLogger(MilvusDropCollectionOpDispenser.class);

    /**
     * <P>Create a new {@link MilvusDropCollectionOpDispenser} subclassed from {@link MilvusBaseOpDispenser}.</P>
     *
     * <P>{@see <A HREF="https://milvus.io/docs/drop_collection.md">Drop Collection</A>}</P>
     *
     * @param adapter        The associated {@link MilvusDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusDropCollectionOpDispenser(MilvusDriverAdapter adapter,
                                           ParsedOp op,
                                           LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<DropCollectionParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF) {
        LongFunction<DropCollectionParam.Builder> f =
            l -> DropCollectionParam.newBuilder().withCollectionName(targetF.apply(l));
        f = op.enhanceFuncOptionally(f, List.of("database","database_name"),String.class,
            DropCollectionParam.Builder::withDatabaseName);
        LongFunction<DropCollectionParam.Builder> finalF = f;
        return l -> finalF.apply(l).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<DropCollectionParam>> createOpFunc(LongFunction<DropCollectionParam> paramF,
                                                                        LongFunction<MilvusServiceClient> clientF,
                                                                        ParsedOp op, LongFunction<String> targetF) {

        return l -> new MilvusDropCollectionOp(clientF.apply(l), paramF.apply(l));
    }

}
