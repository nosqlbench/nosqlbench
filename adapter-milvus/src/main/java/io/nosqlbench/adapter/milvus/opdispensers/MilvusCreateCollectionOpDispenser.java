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
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.ops.MilvusCreateCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class MilvusCreateCollectionOpDispenser extends MilvusBaseOpDispenser<CreateCollectionParam> {
    private static final Logger logger = LogManager.getLogger(MilvusCreateCollectionOpDispenser.class);

    /**
     * Create a new MilvusCreateCollectionOpDispenser subclassed from {@link MilvusBaseOpDispenser}.
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

    @Override
    public LongFunction<CreateCollectionParam> getParamFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<CreateCollectionParam.Builder> ebF =
            l -> CreateCollectionParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, "shards_num", Number.class,
            (CreateCollectionParam.Builder b, Number n) -> b.withShardsNum(n.intValue()));
        ebF = op.enhanceFuncOptionally(ebF, "partition_num", Number.class,
            (CreateCollectionParam.Builder b, Number n) -> b.withPartitionsNum(n.intValue()));
        ebF = op.enhanceFuncOptionally(ebF, "description", String.class,
            CreateCollectionParam.Builder::withDescription);
        ebF = op.enhanceEnumOptionally(ebF, "consistency_level",
            ConsistencyLevelEnum.class, CreateCollectionParam.Builder::withConsistencyLevel);
        ebF = op.enhanceFuncOptionally(ebF, "database_name", String.class,
            CreateCollectionParam.Builder::withDatabaseName);

        List<FieldType> fieldTypes = buildFieldTypesStruct(
            op.getAsSubOps("field_types", ParsedOp.SubOpNaming.SubKey),
            ebF
        );
        final LongFunction<CreateCollectionParam.Builder> f = ebF;
        ebF = l -> f.apply(l).withFieldTypes(fieldTypes);

        final LongFunction<CreateCollectionParam.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    // https://milvus.io/docs/create_collection.md
    @Override
    public LongFunction<MilvusBaseOp<CreateCollectionParam>> createOpFunc(
        LongFunction<CreateCollectionParam> paramF,
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new MilvusCreateCollectionOp(clientF.apply(l), paramF.apply(l));
    }

    /**
     * Function to build the {@link FieldType}s for the {@link CreateCollectionParam}.
     *
     * @param fieldTypesData The static map of config data from the create collection request
     * @param ebF
     * @return a list of static field types
     */
    private List<FieldType> buildFieldTypesStruct(Map<String, ParsedOp> fieldTypesData, LongFunction<CreateCollectionParam.Builder> ebF) {
        List<FieldType> fieldTypes = new ArrayList<>();
        fieldTypesData.forEach((name, fieldspec) -> {
            FieldType.Builder builder = FieldType.newBuilder()
                .withName(name);

            fieldspec.getOptionalStaticValue("primary_key", Boolean.class)
                .ifPresent(builder::withPrimaryKey);
            fieldspec.getOptionalStaticValue("auto_id", Boolean.class)
                .ifPresent(builder::withAutoID);
            fieldspec.getOptionalStaticConfig("max_length", Number.class)
                .ifPresent((Number n) -> builder.withMaxLength(n.intValue()));
            fieldspec.getOptionalStaticConfig("max_capacity", Number.class)
                .ifPresent((Number n) -> builder.withMaxCapacity(n.intValue()));
            fieldspec.getOptionalStaticValue(List.of("partition_key", "partition"), Boolean.class)
                .ifPresent(builder::withPartitionKey);
            fieldspec.getOptionalStaticValue("dimension", Number.class)
                .ifPresent((Number n) -> builder.withDimension(n.intValue()));
            fieldspec.getOptionalStaticConfig("data_type", String.class)
                .map(DataType::valueOf)
                .ifPresent(builder::withDataType);
            fieldspec.getOptionalStaticConfig("type_params", Map.class)
                .ifPresent(builder::withTypeParams);
            fieldspec.getOptionalStaticConfig("element_type", String.class)
                .map(DataType::valueOf)
                .ifPresent(builder::withElementType);

            fieldTypes.add(builder.build());
        });
        return fieldTypes;
    }
}
