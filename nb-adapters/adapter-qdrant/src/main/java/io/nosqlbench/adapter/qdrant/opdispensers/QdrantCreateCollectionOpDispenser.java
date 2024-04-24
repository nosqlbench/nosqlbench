/*
 * Copyright (c) 2020-2020-2024 nosqlbench
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

package io.nosqlbench.adapter.qdrant.opdispensers;

import io.nosqlbench.adapter.qdrant.QdrantDriverAdapter;
import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapter.qdrant.ops.QdrantCreateCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class QdrantCreateCollectionOpDispenser extends QdrantBaseOpDispenser<CreateCollection> {
    private static final Logger logger = LogManager.getLogger(QdrantCreateCollectionOpDispenser.class);

    /**
     * Create a new QdrantCreateCollectionOpDispenser subclassed from {@link QdrantBaseOpDispenser}.
     *
     * @param adapter        The associated {@link QdrantDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Qdrant Index for this Op
     * @see <a href="https://qdrant.github.io/qdrant/redoc/index.html#tag/collections/operation/create_collection">Qdrant Create Collection</a>.
     */
    public QdrantCreateCollectionOpDispenser(QdrantDriverAdapter adapter,
                                             ParsedOp op,
                                             LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<CreateCollection> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<CreateCollection.Builder> ebF =
            l -> CreateCollection.newBuilder().setCollectionName(targetF.apply(l));

//        LongFunction<VectorParams.Builder> ebF =
//            l -> CreateCollectionParam.newBuilder().withCollectionName(targetF.apply(l));

        Map<String, VectorParams> namedVectorsMap = buildNamedVectorsStruct(
            op.getAsSubOps("vectors", ParsedOp.SubOpNaming.SubKey)
        );

//        ebF = op.enhanceFuncOptionally(ebF, "shards_num", Number.class,
//            (VectorParams.Builder b, Number n) -> b.withShardsNum(n.intValue()));
//        ebF = op.enhanceFuncOptionally(ebF, "partition_num", Number.class,
//            (CreateCollectionParam.Builder b, Number n) -> b.withPartitionsNum(n.intValue()));
//        ebF = op.enhanceFuncOptionally(ebF, "description", String.class,
//            VectorParams.Builder::withDescription);
//        ebF = op.enhanceEnumOptionally(ebF, "consistency_level",
//            ConsistencyLevelEnum.class, CreateCollectionParam.Builder::withConsistencyLevel);
//        ebF = op.enhanceFuncOptionally(ebF, "database_name", String.class,
//            CreateCollectionParam.Builder::withDatabaseName);

//        List<FieldType> fieldTypes = buildFieldTypesStruct(
//            op.getAsSubOps("field_types", ParsedOp.SubOpNaming.SubKey)
//        );
        // TODO - HERE
//        final LongFunction<VectorParams.Builder> f = ebF;
//        ebF = l -> f.apply(l).withSchema(CollectionSchemaParam.newBuilder().withFieldTypes(fieldTypes).build());
//
//        final LongFunction<VectorParams.Builder> lastF = ebF;
//        return l -> lastF.apply(l).build();
        return l -> ebF.apply(l).build();
    }

    private Map<String, VectorParams> buildNamedVectorsStruct(Map<String, ParsedOp> namedVectorsData) {
        Map<String, VectorParams> namedVectors = new HashMap<>();
        namedVectorsData.forEach((name, fieldspec) -> {
            VectorParams.Builder builder = VectorParams.newBuilder();
            // TODO  - these are mandatory items; see how to achieve this.
            fieldspec.getOptionalStaticConfig("distance", Distance.class)
                .ifPresent(builder::setDistance);
            fieldspec.getOptionalStaticConfig("size", Number.class)
                .ifPresent((Number n) -> builder.setSize(n.intValue()));

            namedVectors.put(name, builder.build());
        });
        return namedVectors;
    }

    // https://qdrant.tech/documentation/concepts/collections/#create-a-collection
    @Override
    public LongFunction<QdrantBaseOp<CreateCollection>> createOpFunc(
        LongFunction<CreateCollection> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new QdrantCreateCollectionOp(clientF.apply(l), paramF.apply(l));
    }

    /**
     * Function to build the {@link FieldType}s for the {@link VectorParams}.
     *
     * @param fieldTypesData The static map of config data from the create collection request
     * @return a list of static field types
     */
//    private List<FieldType> buildFieldTypesStruct(Map<String, ParsedOp> fieldTypesData) {
//        List<FieldType> fieldTypes = new ArrayList<>();
//        fieldTypesData.forEach((name, fieldspec) -> {
//            FieldType.Builder builder = FieldType.newBuilder()
//                .withName(name);
//
//            fieldspec.getOptionalStaticValue("primary_key", Boolean.class)
//                .ifPresent(builder::withPrimaryKey);
//            fieldspec.getOptionalStaticValue("auto_id", Boolean.class)
//                .ifPresent(builder::withAutoID);
//            fieldspec.getOptionalStaticConfig("max_length", Number.class)
//                .ifPresent((Number n) -> builder.withMaxLength(n.intValue()));
//            fieldspec.getOptionalStaticConfig("max_capacity", Number.class)
//                .ifPresent((Number n) -> builder.withMaxCapacity(n.intValue()));
//            fieldspec.getOptionalStaticValue(List.of("partition_key", "partition"), Boolean.class)
//                .ifPresent(builder::withPartitionKey);
//            fieldspec.getOptionalStaticValue("dimension", Number.class)
//                .ifPresent((Number n) -> builder.withDimension(n.intValue()));
//            fieldspec.getOptionalStaticConfig("data_type", String.class)
//                .map(DataType::valueOf)
//                .ifPresent(builder::withDataType);
//            fieldspec.getOptionalStaticConfig("type_params", Map.class)
//                .ifPresent(builder::withTypeParams);
//            fieldspec.getOptionalStaticConfig("element_type", String.class)
//                .map(DataType::valueOf)
//                .ifPresent(builder::withElementType);
//
//            fieldTypes.add(builder.build());
//        });
//        return fieldTypes;
//    }
}
