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
import io.nosqlbench.adapter.milvus.ops.MilvusDropCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class MilvusDropCollectionOpDispenser extends MilvusOpDispenser {
    private static final Logger logger = LogManager.getLogger(MilvusDropCollectionOpDispenser.class);

    /**
     * Create a new MilvusDeleteOpDispenser subclassed from {@link MilvusOpDispenser}.
     *
     * @param adapter           The associated {@link MilvusDriverAdapter}
     * @param op                The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction    A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusDropCollectionOpDispenser(MilvusDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    // https://milvus.io/docs/drop_collection.md
    @Override
    public LongFunction<MilvusDropCollectionOp> createOpFunc(LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        DropCollectionParam.Builder eb = DropCollectionParam.newBuilder();
        LongFunction<DropCollectionParam.Builder> f =
            l -> DropCollectionParam.newBuilder().withCollectionName(targetF.apply(l));
        return l -> new MilvusDropCollectionOp(clientF.apply(l), f.apply(1).build());
    }
}
