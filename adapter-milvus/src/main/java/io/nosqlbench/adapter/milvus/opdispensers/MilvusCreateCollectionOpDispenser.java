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
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.nosqlbench.adapter.milvus.MilvusDriverAdapter;
import io.nosqlbench.adapter.milvus.ops.MilvusCreateCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class MilvusCreateCollectionOpDispenser extends MilvusOpDispenser {
    private static final Logger logger = LogManager.getLogger(MilvusCreateCollectionOpDispenser.class);

    /**
     * Create a new MilvusCreateCollectionOpDispenser subclassed from {@link MilvusOpDispenser}.
     *
     * @param adapter        The associated {@link MilvusDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusCreateCollectionOpDispenser(MilvusDriverAdapter adapter,
                                             ParsedOp op,
                                             LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    // https://milvus.io/docs/create_collection.md
    @Override
    public LongFunction<MilvusCreateCollectionOp> createOpFunc(LongFunction<MilvusServiceClient> clientF,
                                                               ParsedOp op, LongFunction<String> targetF) {
        LongFunction<CreateCollectionParam.Builder> eb = l -> CreateCollectionParam.newBuilder();

        LongFunction<CreateCollectionParam.Builder> colNameFunc = eb;
        eb = l -> colNameFunc.apply(l).withCollectionName(
            op.getStaticValueOr("collection_name", "vector")
        );

        LongFunction<CreateCollectionParam.Builder> shardsNumFunc = eb;
        eb = l -> shardsNumFunc.apply(l).withShardsNum(
            op.getStaticValueOr("shards_num", 0).intValue()
        );

        LongFunction<CreateCollectionParam.Builder> descriptionFunc = eb;
        eb = l -> descriptionFunc.apply(l).withDescription(
            op.getStaticValueOr("description", "Test vector collection")
        );

        LongFunction<CreateCollectionParam.Builder> consistencyLevelFunc = eb;
        eb = l -> consistencyLevelFunc.apply(l).withConsistencyLevel(
            ConsistencyLevelEnum.valueOf(op.getStaticValueOr("consistency_level", "BOUNDED"))
        );

        LongFunction<List> fieldTypesFunction = op.getAsRequiredFunction("field_types", List.class);
        LongFunction<CreateCollectionParam.Builder> finalFunc = eb;
        LongFunction<List<FieldType>> builtFieldTypes = buildFieldTypesStruct(fieldTypesFunction);
        eb = l -> finalFunc.apply(l).withFieldTypes(builtFieldTypes.apply(l));

        LongFunction<CreateCollectionParam.Builder> f = eb;
        return l -> new MilvusCreateCollectionOp(clientF.apply(l), f.apply(1).build());
    }

    /**
     * Function to build the {@link FieldType}s for the {@link CreateCollectionParam}.
     * @param fieldTypesFunction
     * @return LongFunction<List<FieldType>>
     */
    private LongFunction<List<FieldType>> buildFieldTypesStruct(LongFunction<List> fieldTypesFunction) {
        List<FieldType> finalFieldTypes = new ArrayList<>();
        return l -> {
            FieldType.Builder fieldTypeBuilder;
            List<Object> filterFields = fieldTypesFunction.apply(l);
            for (Object fieldTypes : filterFields) {
                fieldTypeBuilder = FieldType.newBuilder();
                if (fieldTypes instanceof Map) {
                    if (((Map<?, ?>) fieldTypes).containsKey("name")) {
                        fieldTypeBuilder.withName((String) ((Map<?, ?>) fieldTypes).get("name"));
                    }
                    if (((Map<?, ?>) fieldTypes).containsKey("primary_key")) {
                        fieldTypeBuilder.withPrimaryKey(Boolean.parseBoolean((String) ((Map<?, ?>) fieldTypes).get("primary_key")));
                    }
                    if (((Map<?, ?>) fieldTypes).containsKey("auto_id")) {
                        fieldTypeBuilder.withAutoID(Boolean.parseBoolean((String) ((Map<?, ?>) fieldTypes).get("auto_id")));
                    }
                    if (((Map<?, ?>) fieldTypes).containsKey("partition_key")) {
                        fieldTypeBuilder.withPartitionKey(Boolean.parseBoolean((String) ((Map<?, ?>) fieldTypes).get("partition_key")));
                    }
                    if (((Map<?, ?>) fieldTypes).containsKey("dimension")) {
                        fieldTypeBuilder.withDimension(Integer.parseInt((String) ((Map<?, ?>) fieldTypes).get("dimension")));
                    }
                    if (((Map<?, ?>) fieldTypes).containsKey("data_type")) {
                        fieldTypeBuilder.withDataType(DataType.valueOf((String) ((Map<?, ?>) fieldTypes).get("data_type")));
                    }
                } else {
                    throw new RuntimeException("Invalid type for field_types specified." +
                        " It needs to be a map. Check out the examples in the driver documentation.");
                }
                finalFieldTypes.add(fieldTypeBuilder.build());
            }
            return finalFieldTypes;
        };
    }
}
