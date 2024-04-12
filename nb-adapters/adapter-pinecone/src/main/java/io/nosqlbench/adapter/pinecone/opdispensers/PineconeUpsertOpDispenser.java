/*
 * Copyright (c) 2023 nosqlbench
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
import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.adapter.pinecone.ops.PineconeUpsertOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.pinecone.proto.SparseValues;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;

public class PineconeUpsertOpDispenser extends PineconeOpDispenser {
    private static final Logger logger = LogManager.getLogger(PineconeUpsertOpDispenser.class);
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

    /**
     * @param op the ParsedOp from which the Vector objects will be built
     * @return an Iterable Collection of Vector objects to be added to a Pinecone UpsertRequest
     * <p>
     * This method interrogates the subsection of the ParsedOp defined for Vector parameters and constructs
     * a list of Vectors based on the included values, or returns null if this section is not populated. The
     * base function returns either the List of vectors or null, while the interior function builds the vectors
     * with a Builder pattern based on the values contained in the source ParsedOp.
     */
    private LongFunction<Collection<Vector>> createUpsertRequestVectorsFunc(ParsedOp op) {
        Optional<LongFunction<List>> baseFunc =
            op.getAsOptionalFunction("upsert_vectors", List.class);
        return baseFunc.<LongFunction<Collection<Vector>>>map(listLongFunction -> l -> {
            List<Vector> returnVectors = new ArrayList<>();
            List<Map<String, Object>> vectors = listLongFunction.apply(l);
            for (Map<String, Object> vector : vectors) {
                Vector.Builder vb = Vector.newBuilder();
                // No need to check for key, it is invalid if id is not there, let it throw an exception
                vb.setId(vector.get("id").toString());
                vb.addAllValues(getVectorValues(vector.get("values")));
                if (vector.containsKey("sparse_values")) {
                    Map<String,String> sparse_values = (Map<String, String>) vector.get("sparse_values");
                    vb.setSparseValues(SparseValues.newBuilder()
                        .addAllValues(getVectorValues(sparse_values.get("values")))
                        .addAllIndices(getIndexValues(sparse_values.get("indices")))
                        .build());
                }
                if (vector.containsKey("metadata")) {
                    Map<String, Object> metadata_values_map = (Map<String, Object>) vector.get("metadata");
                    vb.setMetadata(Struct.newBuilder().putAllFields(generateMetadataMap(metadata_values_map)).build());
                }
                returnVectors.add(vb.build());
            }
            return returnVectors;
        }).orElse(null);
    }

    /**
     * @param op The ParsedOp used to build the Request
     * @return A function that will take a long (the current cycle) and return a Pinecone UpsertRequest Builder
     * <p>
     * The pattern used here is to accommodate the way Request types are constructed for Pinecone.
     * Requests use a Builder pattern, so at time of instantiation the methods should be chained together.
     * For each method in the chain a function is created here and added to the chain of functions
     * called at time of instantiation.
     * <p>
     * The Vector objects used by the UpsertRequest are sufficiently sophisticated in their own
     * building process that they have been broken out into a separate method. At runtime, they are built separately
     * and then added to the build chain by the builder returned by this method.
     */
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
