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
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.qdrant.client.*;
import io.qdrant.client.grpc.JsonWithInt.ListValue;
import io.qdrant.client.grpc.JsonWithInt.NullValue;
import io.qdrant.client.grpc.JsonWithInt.Struct;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.Vector;
import io.qdrant.client.grpc.Points.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
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
        ebF = op.enhanceFuncOptionally(ebF, "ordering", Number.class, (UpsertPoints.Builder b, Number n) ->
            b.setOrdering(WriteOrdering.newBuilder().setType(WriteOrderingType.forNumber(n.intValue()))));

        // request body begins here
        ebF = op.enhanceFuncOptionally(ebF, "shard_key", String.class, (UpsertPoints.Builder b, String sk) ->
            b.setShardKeySelector(ShardKeySelectorFactory.shardKeySelector(sk)));
        LongFunction<List<PointStruct>> pointsF = constructVectorPointsFunc(op);
        final LongFunction<UpsertPoints.Builder> pointsOfNamedVectorsF = ebF;
        ebF = l -> pointsOfNamedVectorsF.apply(l).addAllPoints(pointsF.apply(l));

        final LongFunction<UpsertPoints.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    /**
     * @param op the {@link ParsedOp} from which the vector objects will be built
     * @return an Iterable Collection of {@link PointStruct} objects to be added to a Qdrant {@link UpsertPoints} request.
     * <p>
     * This method interrogates the subsection of the ParsedOp defined for vector parameters and constructs a list of
     * vector (dense plus sparse) points based on the included values, or returns null if this section is not populated.
     * The base function returns either the List of vectors or null, while the interior function builds the vectors
     * with a Builder pattern based on the values contained in the source ParsedOp.
     */
    private LongFunction<List<PointStruct>> constructVectorPointsFunc(ParsedOp op) {
        Optional<LongFunction<List>> baseFunc =
            op.getAsOptionalFunction("points", List.class);
        return baseFunc.<LongFunction<List<PointStruct>>>map(listLongFunction -> l -> {
            List<PointStruct> returnVectorPoints = new ArrayList<>();
            List<Map<String, Object>> vectorPoints = listLongFunction.apply(l);
            PointStruct.Builder pointBuilder;
            for (Map<String, Object> point : vectorPoints) {
                pointBuilder = PointStruct.newBuilder();
                // 'id' field is mandatory, if not present, server will throw an exception
                PointId.Builder pointId = PointId.newBuilder();
                if (point.get("id") instanceof Number) {
                    pointId.setNum(((Number) point.get("id")).longValue());
                } else if (point.get("id") instanceof String) {
                    pointId.setUuid((String) point.get("id"));
                } else {
                    logger.warn("Unsupported 'id' value type [{}] specified for 'points'. Ignoring.",
                        point.get("id").getClass().getSimpleName());
                }
                pointBuilder.setId(pointId);
                if (point.containsKey("payload")) {
                    pointBuilder.putAllPayload(getPayloadValues(point.get("payload")));
                }
                pointBuilder.setVectors(VectorsFactory.namedVectors(getNamedVectorMap(point.get("vector"))));
                returnVectorPoints.add(pointBuilder.build());
            }
            return returnVectorPoints;
        }).orElse(null);
    }

    private Map<String, Vector> getNamedVectorMap(Object rawVectorValues) {
        Map<String, Vector> namedVectorMapData;
        if (rawVectorValues instanceof Map) {
            namedVectorMapData = new HashMap<>();
            List<Float> sparseVectors = new ArrayList<>();
            List<Integer> sparseIndices = new ArrayList<>();
            BiConsumer<String, Object> namedVectorsToPointsVectorValue = (nvkey, nvVal) -> {
                Vector targetVectorVal;
                if (nvVal instanceof Map) {
                    // Deal with named sparse vectors here
                    ((Map<String, Object>) nvVal).forEach(
                        (svKey, svValue) -> {
                            if ("values".equals(svKey)) {
                                sparseVectors.addAll((List<Float>) svValue);
                            } else if ("indices".equals(svKey)) {
                                sparseIndices.addAll((List<Integer>) svValue);
                            } else {
                                logger.warn("Unrecognized sparse vector field [{}] provided. Ignoring.", svKey);
                            }
                        }
                    );
                    targetVectorVal = VectorFactory.vector(sparseVectors, sparseIndices);
                } else if (nvVal instanceof List) {
                    // Deal with regular named dense vectors here
                    targetVectorVal = VectorFactory.vector((List<Float>) nvVal);
                } else
                    throw new RuntimeException("Unsupported 'vector' value type [" + nvVal.getClass().getSimpleName() + " ]");
                namedVectorMapData.put(nvkey, targetVectorVal);
            };
            ((Map<String, Object>) rawVectorValues).forEach(namedVectorsToPointsVectorValue);
        } else {
            throw new OpConfigError("Invalid format of type" +
                " [" + rawVectorValues.getClass().getSimpleName() + "] specified for 'vector'");
        }
        return namedVectorMapData;
    }

    private Map<String, Value> getPayloadValues(Object rawPayloadValues) {
        if (rawPayloadValues instanceof Map) {
            Map<String, Object> payloadMap = (Map<String, Object>) rawPayloadValues;
            Map<String, Value> payloadMapData = new HashMap<>(payloadMap.size());
            payloadMap.forEach((pKey, pVal) -> {
                switch (pVal) {
                    case Boolean b -> payloadMapData.put(pKey, ValueFactory.value(b));
                    case Double v -> payloadMapData.put(pKey, ValueFactory.value(v));
                    case Integer i -> payloadMapData.put(pKey, ValueFactory.value(i));
                    case String s -> payloadMapData.put(pKey, ValueFactory.value(s));
                    case ListValue listValue -> payloadMapData.put(pKey, ValueFactory.list((List<Value>) pVal));
                    case NullValue nullValue -> payloadMapData.put(pKey, ValueFactory.nullValue());
                    case Struct struct -> payloadMapData.put(pKey, Value.newBuilder().setStructValue(struct).build());
                    default -> logger.warn("Unknown payload value type passed." +
                        " Only https://qdrant.tech/documentation/concepts/payload/#payload-types are supported." +
                        " {} will be ignored.", pVal.toString());
                }
            });
            return payloadMapData;
        } else {
            throw new RuntimeException("Invalid format of type" +
                " [" + rawPayloadValues.getClass().getSimpleName() + "] specified for payload");
        }
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
