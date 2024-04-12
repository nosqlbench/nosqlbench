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
import io.nosqlbench.adapter.pinecone.ops.PineconeQueryOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryVector;
import io.pinecone.proto.SparseValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;

public class PineconeQueryOpDispenser extends PineconeOpDispenser {
    private static final Logger logger = LogManager.getLogger(PineconeQueryOpDispenser.class);
    private final LongFunction<QueryRequest.Builder> queryRequestFunc;
    private final LongFunction<Collection<QueryVector>> queryVectorFunc;


    /**
     * Create a new PineconeQueryOpDispenser subclassed from {@link PineconeOpDispenser}.
     *
     * @param adapter           The associated {@link PineconeDriverAdapter}
     * @param op                The {@link ParsedOp} encapsulating the activity for this cycle
     * @param pcFunction        A function to return the associated context of this activity (see {@link PineconeSpace})
     * @param targetFunction    A LongFunction that returns the specified Pinecone Index for this Op
     */
    public PineconeQueryOpDispenser(PineconeDriverAdapter adapter,
                                    ParsedOp op,
                                    LongFunction<PineconeSpace> pcFunction,
                                    LongFunction<String> targetFunction) {
        super(adapter, op, pcFunction, targetFunction);
        queryRequestFunc = createQueryRequestFunc(op);
        queryVectorFunc = createQueryVectorFunc(op);
    }

    /**
     * @param op The ParsedOp used to build the Request
     * @return A function that will take a long (the current cycle) and return a Pinecone QueryRequest Builder
     * <p>
     * The pattern used here is to accommodate the way Request types are constructed for Pinecone.
     * Requests use a Builder pattern, so at time of instantiation the methods should be chained together.
     * For each method in the chain a function is created here and added to the chain of functions
     * called at time of instantiation.
     * <p>
     * The QueryVector objects used by the QueryRequest as sufficiently sophisticated in their own building process
     * that it has been broken out into a separate method. At runtime, they are built separately and then added
     * to the build chain by the builder returned by this method.
     */
    private LongFunction<QueryRequest.Builder> createQueryRequestFunc(ParsedOp op) {
        LongFunction<QueryRequest.Builder> rFunc = l -> QueryRequest.newBuilder();

        Optional<LongFunction<String>> nFunc = op.getAsOptionalFunction("namespace", String.class);
        if (nFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<String> af = nFunc.get();
            rFunc = l -> finalFunc.apply(l).setNamespace(af.apply(l));
        }

        Optional<LongFunction<Integer>> tFunc = op.getAsOptionalFunction("top_k", Integer.class);
        if (tFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<Integer> af = tFunc.get();
            rFunc = l -> finalFunc.apply(l).setTopK(af.apply(l));
        }

        Optional<LongFunction<Boolean>> mFunc = op.getAsOptionalFunction("include_metadata", Boolean.class);
        if (mFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<Boolean> af = mFunc.get();
            rFunc = l -> finalFunc.apply(l).setIncludeMetadata(af.apply(l));
        }

        Optional<LongFunction<Boolean>> ivFunc = op.getAsOptionalFunction("include_values", Boolean.class);
        if (ivFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<Boolean> af = ivFunc.get();
            rFunc = l -> finalFunc.apply(l).setIncludeValues(af.apply(l));
        }

        Optional<LongFunction<Object>> vFunc = op.getAsOptionalFunction("vector", Object.class);
        if (vFunc.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<Object> af = vFunc.get();

            LongFunction<List<Float>> alf = extractFloatVals(af);
            rFunc = l -> finalFunc.apply(l).addAllVector(alf.apply(l));
        }

        Optional<LongFunction<Map>> filterFunction = op.getAsOptionalFunction("filter", Map.class);
        if (filterFunction.isPresent()) {
            LongFunction<QueryRequest.Builder> finalFunc = rFunc;
            LongFunction<Struct> builtFilter = buildFilterStruct(filterFunction.get());
            rFunc = l -> finalFunc.apply(l).setFilter(builtFilter.apply(l));
        }

        return rFunc;
    }


    /**
     * @param op the ParsedOp from which the Query Vector objects will be built
     * @return an Iterable Collection of QueryVector objects to be added to a Pinecone QueryRequest
     * <p>
     * This method interrogates the subsection of the ParsedOp defined for QueryVector parameters and constructs
     * a list of QueryVectors based on the included values, or returns null if this section is not populated. The
     * base function returns either the List of vectors or null, while the interior function builds the vectors
     * with a Builder pattern based on the values contained in the source ParsedOp.
     */
    private LongFunction<Collection<QueryVector>> createQueryVectorFunc(ParsedOp op) {
        Optional<LongFunction<List>> baseFunc =
            op.getAsOptionalFunction("query_vectors", List.class);
        return baseFunc.<LongFunction<Collection<QueryVector>>>map(listLongFunction -> l -> {
            List<QueryVector> returnVectors = new ArrayList<>();
            List<Map<String, Object>> vectors = listLongFunction.apply(l);
            for (Map<String, Object> vector : vectors) {
                QueryVector.Builder qvb = QueryVector.newBuilder();
                qvb.addAllValues(getVectorValues(vector.get("values")));
                qvb.setNamespace((String) vector.get("namespace"));
                if (vector.containsKey("top_k")) {
                    qvb.setTopK((Integer) vector.get("top_k"));
                }
                if (vector.containsKey("filter")) {
                    LongFunction<Struct> builtFilter = buildFilterStruct(l2 -> (Map) vector.get("filter"));
                    qvb.setFilter(builtFilter.apply(l));
                }
                if (vector.containsKey("sparse_values")) {
                    Map<String,String> sparse_values = (Map<String, String>) vector.get("sparse_values");
                    qvb.setSparseValues(SparseValues.newBuilder()
                        .addAllValues(getVectorValues(sparse_values.get("values")))
                        .addAllIndices(getIndexValues(sparse_values.get("indices")))
                        .build());
                }
                returnVectors.add(qvb.build());
            }
            return returnVectors;
        }).orElse(null);
    }

    @Override
    public PineconeOp apply(long value) {
        QueryRequest.Builder qrb = queryRequestFunc.apply(value);
        if (queryVectorFunc != null) {
            qrb.addAllQueries(queryVectorFunc.apply(value));
        }
        return new PineconeQueryOp(pcFunction.apply(value).getConnection(targetFunction.apply(value)), qrb.build());
    }
}
