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
import io.nosqlbench.adapter.qdrant.ops.QdrantSearchPointsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points.*;

import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

public class QdrantSearchPointsOpDispenser extends QdrantBaseOpDispenser<SearchPoints> {
    public QdrantSearchPointsOpDispenser(QdrantDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<QdrantBaseOp<SearchPoints>> createOpFunc(
        LongFunction<SearchPoints> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op, LongFunction<String> targetF) {
        return l -> new QdrantSearchPointsOp(clientF.apply(l), paramF.apply(l));
    }

    @Override
    public LongFunction<SearchPoints> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF) {
        LongFunction<SearchPoints.Builder> ebF =
            l -> SearchPoints.newBuilder().setCollectionName(targetF.apply(l));

        ebF = op.enhanceFuncOptionally(ebF, "limit", Number.class,
            (SearchPoints.Builder b, Number n) -> b.setLimit(n.longValue()));
        ebF = op.enhanceFuncOptionally(ebF, "vector_name", String.class, SearchPoints.Builder::setVectorName);
        ebF = op.enhanceFuncOptionally(ebF, "with_payload", Boolean.class,
            (SearchPoints.Builder b, Boolean wp) -> b.setWithPayload(WithPayloadSelector.newBuilder().setEnable(wp).build()));
        ebF = op.enhanceFuncOptionally(ebF, "with_vector", Boolean.class,
            (SearchPoints.Builder b, Boolean wp) -> b.setWithVectors(WithVectorsSelector.newBuilder().setEnable(wp).build()));
        ebF = op.enhanceFuncOptionally(ebF, "read_consistency", Number.class,
            (SearchPoints.Builder b, Number rc) -> b.setReadConsistency(
                ReadConsistency.newBuilder().setType(ReadConsistencyType.forNumber(rc.intValue())).build()));
//        ebF = op.enhanceFunc(ebF, List.of("vector_vector", "vectors"), List.class,
//            (SearchPoints.Builder b, List<Float> vec) -> b.addAllVector(vec));

        Optional<LongFunction<List<Float>>> optionalVectorsF = getVectorFieldsFunction(op, "vector_vector");
        if(optionalVectorsF.isPresent()) {
            var rf = optionalVectorsF.get();
            LongFunction<SearchPoints.Builder> finalF2 = ebF;
            ebF = l -> finalF2.apply(l).addAllVector(rf.apply(l));
        }//ccvx  .getAsSubOps("vectors", ParsedOp.SubOpNaming.SubKey)
//        );
//        final LongFunction<Collections.CreateCollection.Builder> namedVectorsF = ebF;
//        ebF = l -> namedVectorsF.apply(l).setVectorsConfig(Collections.VectorsConfig.newBuilder().setParams(namedVectorsMap));
        else {
            throw new OpConfigError("Must provide values for vectors");
        }

        final LongFunction<SearchPoints.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    private Optional<LongFunction<List<Float>>> getVectorFieldsFunction(ParsedOp op, String namedVectors) {
//        return l -> {
//            if (!op.isDefined(namedVectors)) {
//                return Optional.empty();
//            }
//            List<Float> fields = op.get(namedVectors, 0L);
//            if (fields == null) {
//                fields = op.get(namedVectors, 0L);
//            }
//            return fields;
//        };
        LongFunction<Object> rowF = op.getAsRequiredFunction(namedVectors, Object.class);
        Object testObject = rowF.apply(0L);
        LongFunction<List<Float>> rowsF = null;
        if(testObject instanceof List<?> list) {
            if (list.isEmpty()) {
                throw new OpConfigError("Unable to detect type of list object for empty list for op named '" + op.getName() + "'");
            } else if (list.get(0) instanceof Float) {
                rowsF = l -> (List<Float>) rowF.apply(l);
            }
        }
        return Optional.ofNullable(rowsF);
    }
}
