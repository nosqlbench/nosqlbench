/*
 * Copyright (c) 2020-2024 nosqlbench
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
import io.nosqlbench.adapter.qdrant.ops.QdrantUpsertPointsOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.VectorFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt.ListValue;
import io.qdrant.client.grpc.JsonWithInt.NullValue;
import io.qdrant.client.grpc.JsonWithInt.Struct;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class QdrantUpsertPointsOpDispenser extends QdrantBaseOpDispenser<UpsertPoints> {
    private static final Logger logger = LogManager.getLogger(QdrantUpsertPointsOpDispenser.class);

    /**
     * Create a new {@link QdrantUpsertPointsOpDispenser} implementing the {@link OpDispenser} interface.
     * @param adapter
     * @param op
     * @param targetFunction
     * @see <a href="https://qdrant.github.io/qdrant/redoc/index.html#tag/points/operation/upsert_points">Upsert Points</a>
     */
    public QdrantUpsertPointsOpDispenser(QdrantDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<UpsertPoints> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<UpsertPoints.Builder> ebF =
            l -> UpsertPoints.newBuilder().setCollectionName(targetF.apply(l));

        // set wait and ordering query params
        ebF = op.enhanceFuncOptionally(ebF, "wait", Boolean.class, UpsertPoints.Builder::setWait);
        WriteOrdering.Builder writeOrdering = WriteOrdering.newBuilder();
        op.getOptionalStaticValue("ordering", Number.class)
            .ifPresent((Number ordering) -> {
                writeOrdering.setType(WriteOrderingType.forNumber(ordering.intValue()));
            });
        final LongFunction<UpsertPoints.Builder> orderingF = ebF;
        ebF = l -> orderingF.apply(l).setOrdering(writeOrdering);

        // request body begins here
        ShardKeySelector.Builder shardKeySelector = ShardKeySelector.newBuilder();
        op.getOptionalStaticValue("shard_key", Number.class)
            .ifPresent((Number value) -> {
                shardKeySelector.setShardKeys(0, Collections.ShardKey.newBuilder().setNumber(value.longValue()));
            });

        List<PointStruct> allPoints = buildPointsStructWithNamedVectors(op);
        final LongFunction<UpsertPoints.Builder> pointsOfNamedVectorsF = ebF;
        ebF = l -> pointsOfNamedVectorsF.apply(l).addAllPoints(allPoints);

        final LongFunction<UpsertPoints.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    private List<PointStruct> buildPointsStructWithNamedVectors(ParsedOp op) {
        List<PointStruct> allPoints = new ArrayList<>();
        PointStruct.Builder pointBuilder = PointStruct.newBuilder();

        PointId.Builder pointId = PointId.newBuilder();
        // id is mandatory
        Object idObject = op.getAsRequiredFunction("id", Object.class).apply(0L);
        if (idObject instanceof Number) {
            pointId.setNum(((Number) idObject).longValue());
        } else if (idObject instanceof String) {
            pointId.setUuid((String) idObject);
        }
        pointBuilder.setId(pointId);

        if (op.isDefined("payload")) {
            LongFunction<Map> payloadMapF = op.getAsRequiredFunction("payload", Map.class);
            Map<String, Value> payloadMapData = new HashMap<>();
            payloadMapF.apply(0L).forEach((pKey, pVal) -> {
                if(pVal instanceof Boolean) {
                    payloadMapData.put((String) pKey, ValueFactory.value((Boolean) pVal));
                } else if(pVal instanceof Double) {
                    payloadMapData.put((String) pKey, ValueFactory.value((Double) pVal));
                } else if(pVal instanceof Integer) {
                    payloadMapData.put((String) pKey, ValueFactory.value((Integer) pVal));
                } else if(pVal instanceof String) {
                    payloadMapData.put((String) pKey, ValueFactory.value((String) pVal));
                } else if(pVal instanceof ListValue) {
                    payloadMapData.put((String) pKey, ValueFactory.list((List<Value>) pVal));
                } else if(pVal instanceof NullValue) {
                    payloadMapData.put((String) pKey, ValueFactory.nullValue());
                } else if(pVal instanceof Struct) {
                    payloadMapData.put((String) pKey, Value.newBuilder().setStructValue((Struct) pVal).build());
                } else {
                    logger.warn("Unknown payload type passed." +
                        " Only https://qdrant.tech/documentation/concepts/payload/#payload-types are supported." +
                        " {} will be inored.", pVal.toString());
                }
            });
            pointBuilder.putAllPayload(payloadMapData);
        }

        LongFunction<Map> namedVectorMapF = op.getAsRequiredFunction("vector", Map.class);
        Map<String, Vector> namedVectorMapData = new HashMap<>();
        List<Float> sparseVectors = new ArrayList<>();
        List<Integer> sparseIndices = new ArrayList<>();
        namedVectorMapF.apply(0L).forEach((nvKey, nvVal) -> {
            if (nvVal instanceof Map) {
                // we deal with named sparse vectors here
                Map<String, Object> namedSparseVectorsMap = (Map<String, Object>) nvVal;
                if (namedSparseVectorsMap.containsKey("indices") && namedSparseVectorsMap.containsKey("values")) {
                    sparseVectors.addAll((List<Float>) namedSparseVectorsMap.get("values"));
                    sparseIndices.addAll((List<Integer>) namedSparseVectorsMap.get("indices"));
                }
                namedVectorMapData.put((String) nvKey, VectorFactory.vector(sparseVectors, sparseIndices));
            } else {
                // Deal with regular named dense vectors here
                namedVectorMapData.put((String) nvKey, VectorFactory.vector((List<Float>) nvVal));
            }
        });
        pointBuilder.setVectors(VectorsFactory.namedVectors(namedVectorMapData));
        allPoints.add(pointBuilder.build());

        return allPoints;
    }

    /**
     * Create a new {@link QdrantUpsertPointsOp} implementing the {@link QdrantBaseOp} interface.
     * @see <a href="https://qdrant.tech/documentation/concepts/points/">Upsert Points</a>
     */
    @Override
    public LongFunction<QdrantBaseOp<UpsertPoints>> createOpFunc(
        LongFunction<UpsertPoints> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new QdrantUpsertPointsOp(clientF.apply(l), paramF.apply(l));
    }
}
