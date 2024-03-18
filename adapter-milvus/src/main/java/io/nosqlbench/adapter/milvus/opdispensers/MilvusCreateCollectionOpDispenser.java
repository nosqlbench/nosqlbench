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
     * @param adapter
     *     The associated {@link MilvusDriverAdapter}
     * @param op
     *     The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction
     *     A LongFunction that returns the specified Milvus Index for this Op
     */
    public MilvusCreateCollectionOpDispenser(MilvusDriverAdapter adapter,
                                             ParsedOp op,
                                             LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    // https://milvus.io/docs/create_collection.md
    @Override
    public LongFunction<MilvusCreateCollectionOp> createOpFunc(
        LongFunction<MilvusServiceClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {

        LongFunction<CreateCollectionParam.Builder> ebF =
            l -> CreateCollectionParam.newBuilder().withCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, "shards_num", Integer.class,
            CreateCollectionParam.Builder::withShardsNum);
        ebF = op.enhanceFuncOptionally(ebF, "description", String.class,
            CreateCollectionParam.Builder::withDescription);
        ebF = op.enhanceEnumOptionally(ebF, "consistency_level",
            ConsistencyLevelEnum.class, CreateCollectionParam.Builder::withConsistencyLevel);

        Map<String,Object> fieldTypesMap = op.getStaticValue("field_types", Map.class);
        List<FieldType> fieldTypes = buildFieldTypesStruct(fieldTypesMap);
        final LongFunction<CreateCollectionParam.Builder> f = ebF;
        ebF = l -> f.apply(l).withFieldTypes(fieldTypes);
        final LongFunction<CreateCollectionParam.Builder> lastF = ebF;
        final LongFunction<CreateCollectionParam> collectionParamF = l -> lastF.apply(l).build();

        return l -> new MilvusCreateCollectionOp(clientF.apply(l), collectionParamF.apply(l));
    }

    /**
     * Function to build the {@link FieldType}s for the {@link CreateCollectionParam}.
     *
     * @param fieldTypesData The static map of config data from the create collection request
     * @return a list of static field types
     */
    private List<FieldType> buildFieldTypesStruct(Map<String,Object> fieldTypesData) {

        List<FieldType> fieldTypes = new ArrayList<>();
        fieldTypesData.forEach((name, properties) -> {
            FieldType.Builder fieldTypeBuilder = FieldType.newBuilder()
                .withName(name);

            if (properties instanceof Map<?,?> map) {

                if (map.containsKey("primary_key")) {
                    fieldTypeBuilder.withPrimaryKey(Boolean.parseBoolean((String) map.get("primary_key")));
                }
                if (map.containsKey("auto_id")) {
                    fieldTypeBuilder.withAutoID(Boolean.parseBoolean((String) map.get("auto_id")));
                }
                if (map.containsKey("partition_key")) {
                    fieldTypeBuilder.withPartitionKey(Boolean.parseBoolean((String) map.get("partition_key")));
                }
                if (map.containsKey("dimension")) {
                    fieldTypeBuilder.withDimension(Integer.parseInt((String) map.get("dimension")));
                }
                if (map.containsKey("data_type")) {
                    fieldTypeBuilder.withDataType(DataType.valueOf((String) map.get("data_type")));
                }
            } else {
                throw new RuntimeException("Invalid type for field_types specified." +
                    " It needs to be a map. Check out the examples in the driver documentation.");
            }

            fieldTypes.add(fieldTypeBuilder.build());
        });
        return fieldTypes;
    }
}
