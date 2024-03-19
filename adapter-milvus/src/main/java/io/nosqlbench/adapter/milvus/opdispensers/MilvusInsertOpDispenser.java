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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        f = op.enhanceFuncOptionally(f, "partition", String.class, InsertParam.Builder::withPartitionName);
        f = op.enhanceFuncOptionally(f, "database", String.class, InsertParam.Builder::withDatabaseName);
        if (op.isDefined("rows")) {
            LongFunction<List<JSONObject>> rowsF = createRowsF(op);
            LongFunction<InsertParam.Builder> finalF = f;
            f = l -> finalF.apply(l).withRows(rowsF.apply(l));
            f = op.enhanceFuncOptionally(f, "rows", List.class, InsertParam.Builder::withRows);
        }
        LongFunction<InsertParam.Builder> finalF1 = f;
        LongFunction<List<InsertParam.Field>> fieldsF = createFieldsF(op);
        LongFunction<InsertParam> insertParamsF = l -> finalF1.apply(l).withFields(fieldsF.apply(l)).build();
        return insertParamsF;
    }

    @Override
    public LongFunction<MilvusBaseOp<InsertParam>> createOpFunc(
        LongFunction<InsertParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op, LongFunction<String> targetF
    ) {
        return l -> new MilvusInsertOp(clientF.apply(l), paramF.apply(l));
    }


    private LongFunction<List<InsertParam.Field>> createFieldsF(ParsedOp op) {
        LongFunction<Map> fieldDataF = op.getAsRequiredFunction("fields", Map.class);
        LongFunction<List<InsertParam.Field>> fieldsF = l -> {
            Map<String, Object> fieldmap = fieldDataF.apply(l);
            List<InsertParam.Field> fields = new ArrayList<>();
            fieldmap.forEach((name, value) -> {
                fields.add(new InsertParam.Field(name, (List) value));
            });
            return fields;
        };
        return fieldsF;
    }

    private LongFunction<List<JSONObject>> createRowsF(ParsedOp op) {
        throw new RuntimeException("This is not implemented yet");
    }
}
