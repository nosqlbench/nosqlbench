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


import com.alibaba.fastjson.JSONObject;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusDeleteOp;
import io.nosqlbench.adapter.milvus.ops.MilvusInsertOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class MilvusDeleteOpDispenser extends MilvusOpDispenser {
    private static final Logger logger = LogManager.getLogger(MilvusDeleteOpDispenser.class);

    /**
     * Create a new MilvusDeleteOpDispenser subclassed from {@link MilvusOpDispenser}.
     *
     * @param adapter        The associated {@link MilvusDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusDeleteOpDispenser(MilvusDriverAdapter adapter,
                                   ParsedOp op,
                                   LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<MilvusDeleteOp> createOpFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {

        LongFunction<DeleteParam.Builder> f =
            l -> DeleteParam.newBuilder().withCollectionName(targetF.apply(l));
        f = op.enhanceFuncOptionally(f, "partition", String.class, DeleteParam.Builder::withPartitionName);
        f = op.enhanceFuncOptionally(f, "expression", String.class, DeleteParam.Builder::withExpr);
        f = op.enhanceFuncOptionally(f, "expr", String.class, DeleteParam.Builder::withExpr);

        LongFunction<DeleteParam.Builder> finalF = f;
        LongFunction<MilvusDeleteOp> opF = l -> new MilvusDeleteOp(clientF.apply(l), finalF.apply(l).build());
        return opF;
    }

}
