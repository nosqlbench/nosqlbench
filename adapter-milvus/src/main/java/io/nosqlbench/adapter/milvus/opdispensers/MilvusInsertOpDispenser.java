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
import io.milvus.param.dml.InsertParam;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusInsertOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class MilvusInsertOpDispenser extends MilvusBaseOpDispenser<InsertParam> {
    private static final Logger logger = LogManager.getLogger(MilvusInsertOpDispenser.class);

    /**
     * Create a new MilvusDeleteOpDispenser subclassed from {@link MilvusBaseOpDispenser}.
     *
     * @param adapter        The associated {@link MilvusDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusInsertOpDispenser(MilvusDriverAdapter adapter,
                                   ParsedOp op,
                                   LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<InsertParam> getParamFunc(LongFunction<MilvusServiceClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        LongFunction<InsertParam.Builder> f =
            l -> InsertParam.newBuilder().withCollectionName(targetF.apply(l));

        f = op.enhanceFuncOptionally(
            f, List.of("partition_name","partition"), String.class,
            InsertParam.Builder::withPartitionName
        );
        f = op.enhanceFuncOptionally(
            f, List.of("database_name","database"), String.class,
            InsertParam.Builder::withDatabaseName
        );

        Optional<LongFunction<List<JSONObject>>> optionalRowsF = MilvusOpUtils.getHighLevelRowsFunction(op, "rows");
        Optional<LongFunction<List<InsertParam.Field>>> optionalFieldsF = MilvusOpUtils.getFieldsFunction(op, "fields");

        if (optionalFieldsF.isPresent() && optionalRowsF.isPresent()) {
            throw new OpConfigError("Must provide either rows or fields, but not both.");
        }
        if (optionalFieldsF.isEmpty() && optionalRowsF.isEmpty()) {
            throw new OpConfigError("Must provide either rows or fields");
        }

        if (optionalRowsF.isPresent()) {
            var rf = optionalRowsF.get();
            LongFunction<InsertParam.Builder> finalF2 = f;
            f = l -> finalF2.apply(l).withRows(rf.apply(l));
        }

        if (optionalFieldsF.isPresent()) {
            var ff = optionalFieldsF.get();
            LongFunction<InsertParam.Builder> finalF3 = f;
            f = l -> finalF3.apply(l).withFields(ff.apply(l));
        }

        LongFunction<InsertParam.Builder> finalF = f;
        return l -> finalF.apply(l).build();
    }

    @Override
    public LongFunction<MilvusBaseOp<InsertParam>> createOpFunc(
        LongFunction<InsertParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op, LongFunction<String> targetF
    ) {
        return l -> new MilvusInsertOp(clientF.apply(l), paramF.apply(l));
    }

}
