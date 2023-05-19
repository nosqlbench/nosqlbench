/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.pinecone.opdispensers;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeUpsertOp;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.pinecone.proto.SparseValues;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.LongFunction;

public class PineconeUpsertOpDispenser extends PineconeOpDispenser {
    private static final Logger LOGGER = LogManager.getLogger(PineconeUpsertOpDispenser.class);
    private final LongFunction<UpsertRequest.Builder> upsertRequestFunc;
    private final LongFunction<Collection<Vector>> upsertVectorFunc;

    /**
     * Create a new PineconeUpsertOpDispenser subclassed from {@link PineconeOpDispenser}.
     *
     * @param adapter           The associated {@link PineconeDriverAdapter}
     * @param op                The {@link ParsedOp} encapsulating the activity for this cycle
     * @param pcFunction        A function to return the associated context of this activity (see {@link PineconeSpace})
     * @param targetFunction    A LongFunction that returns the specified Pinecone Index for this Op
     */
    public PineconeUpsertOpDispenser(PineconeDriverAdapter adapter,
                                     ParsedOp op,
                                     LongFunction<PineconeSpace> pcFunction,
                                     LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);
        upsertRequestFunc = createUpsertRequestFunc(op);
        upsertVectorFunc = createUpsertRequestVectorsFunc(op);
    }

    private LongFunction<Collection<Vector>> createUpsertRequestVectorsFunc(ParsedOp op) {
        Optional<LongFunction<List>> baseFunc =
            op.getAsOptionalFunction("upsert_vectors", List.class);
        return baseFunc.<LongFunction<Collection<Vector>>>map(listLongFunction -> l -> {
            List<Vector> returnVectors = new ArrayList<>();
            List<Map<String, Object>> vectors = listLongFunction.apply(l);
            for (Map<String, Object> vector : vectors) {
                Vector.Builder vb = Vector.newBuilder();
                String[] rawValues = ((String) vector.get("values")).split(",");
                ArrayList<Float> floatValues = new ArrayList<>();
                for (String val : rawValues) {
                    floatValues.add(Float.valueOf(val));
                }
                vb.addAllValues(floatValues);
                if (vector.containsKey("sparse_values")) {
                    Map<String,String> sparse_values = (Map<String, String>) vector.get("sparse_values");
                    rawValues = ((String) sparse_values.get("values")).split(",");
                    floatValues = new ArrayList<>();
                    for (String val : rawValues) {
                        floatValues.add(Float.valueOf(val));
                    }
                    rawValues = sparse_values.get("indices").split(",");
                    List<Integer> intValues = new ArrayList<>();
                    for (String val : rawValues) {
                        intValues.add(Integer.valueOf(val));
                    }
                    vb.setSparseValues(SparseValues.newBuilder()
                        .addAllValues(floatValues)
                        .addAllIndices(intValues)
                        .build());
                }
                if (vector.containsKey("metadata")) {
                    Map<String, Value> metadata_map = new HashMap<String, Value>();
                    BiConsumer<String,Object> stringToValue = (key, val) -> {
                        Value targetval = null;
                        if (val instanceof String) targetval = Value.newBuilder().setStringValue((String)val).build();
                        else if (val instanceof Number) targetval = Value.newBuilder().setNumberValue((((Number) val).doubleValue())).build();
                        metadata_map.put(key, targetval);
                    };
                    Map<String, Object> metadata_values_map = (Map<String, Object>) vector.get("metadata");
                    metadata_values_map.forEach(stringToValue);
                    vb.setMetadata(Struct.newBuilder().putAllFields(metadata_map).build());
                }
                returnVectors.add(vb.build());
            }
            return returnVectors;
        }).orElse(null);
    }

    private LongFunction<UpsertRequest.Builder> createUpsertRequestFunc(ParsedOp op) {
        LongFunction<UpsertRequest.Builder> rFunc = l -> UpsertRequest.newBuilder();

        Optional<LongFunction<String>> nFunc = op.getAsOptionalFunction("namespace", String.class);
        if (nFunc.isPresent()) {
            LongFunction<UpsertRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = nFunc.get();
            rFunc = l -> finalFunc.apply(l).setNamespace(af.apply(l));
        }

        return rFunc;
    }

    @Override
    public PineconeOp apply(long value) {
        UpsertRequest.Builder urb = upsertRequestFunc.apply(value);
        if (upsertVectorFunc != null) {
            urb.addAllVectors(upsertVectorFunc.apply(value));
        }
        return new PineconeUpsertOp(pcFunction.apply(value).getConnection(targetFunction.apply(value)), urb.build());
    }
}
