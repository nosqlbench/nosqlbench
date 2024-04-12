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
import io.milvus.param.index.DropIndexParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusDropIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.LongFunction;

public class MilvusDropIndexOpDispenser extends MilvusBaseOpDispenser<DropIndexParam> {
    /**
     * <P>Create a new MilvusDeleteOpDispenser subclassed from {@link MilvusBaseOpDispenser}.</P>
     * <P>{@see <a href="https://milvus.io/docs/drop_collection.md">Drop Index</a>}</P>
     *
     *
     * @param adapter
     *     The associated {@link MilvusDriverAdapter}
     * @param op
     *     The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction
     *     A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusDropIndexOpDispenser(MilvusDriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<DropIndexParam> getParamFunc(LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        LongFunction<DropIndexParam.Builder> f =
            l -> DropIndexParam.newBuilder().withIndexName(targetF.apply(l));
        f = op.enhanceFunc(f, List.of("collection_name","collection"),String.class,
            DropIndexParam.Builder::withCollectionName);
        LongFunction<DropIndexParam.Builder> finalF = f;
        return l -> finalF.apply(1).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<DropIndexParam>> createOpFunc(LongFunction<DropIndexParam> paramF, LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> new MilvusDropIndexOp(clientF.apply(l),paramF.apply(l));
    }

}
