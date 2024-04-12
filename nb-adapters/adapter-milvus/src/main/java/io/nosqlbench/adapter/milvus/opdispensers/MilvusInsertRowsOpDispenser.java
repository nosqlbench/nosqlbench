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


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.highlevel.dml.InsertRowsParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusInsertOp;
import io.nosqlbench.adapter.milvus.ops.MilvusInsertRowsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class MilvusInsertRowsOpDispenser extends MilvusBaseOpDispenser<InsertRowsParam> {
    private static final Logger logger = LogManager.getLogger(MilvusInsertRowsOpDispenser.class);

    /**
     * Create a new MilvusDeleteOpDispenser subclassed from {@link MilvusBaseOpDispenser}.
     *
     * @param adapter
     *     The associated {@link MilvusDriverAdapter}
     * @param op
     *     The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction
     *     A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusInsertRowsOpDispenser(MilvusDriverAdapter adapter,
                                       ParsedOp op,
                                       LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<InsertRowsParam> getParamFunc(LongFunction<MilvusServiceClient> clientF, ParsedOp op,
                                                      LongFunction<String> targetF) {
        LongFunction<InsertRowsParam.Builder> f =
            l -> InsertRowsParam.newBuilder().withCollectionName(targetF.apply(l));

        LongFunction<List<JSONObject>> rowsF = MilvusOpUtils
            .getHighLevelRowsFunction(op, "rows")
            .orElseThrow(() -> new RuntimeException("rows must be provided for op template '" + op.getName() + "'"));

        LongFunction<InsertRowsParam.Builder> finalF1 = f;
        f = l -> finalF1.apply(l).withRows(rowsF.apply(l));

        LongFunction<InsertRowsParam.Builder> finalF = f;
        return l -> finalF.apply(l).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<InsertRowsParam>> createOpFunc(
        LongFunction<InsertRowsParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op, LongFunction<String> targetF
    ) {
        return l -> new MilvusInsertRowsOp(clientF.apply(l), paramF.apply(l));
    }
}
