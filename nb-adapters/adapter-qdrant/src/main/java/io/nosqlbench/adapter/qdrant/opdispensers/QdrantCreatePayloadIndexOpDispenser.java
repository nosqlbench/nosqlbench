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
import io.nosqlbench.adapter.qdrant.ops.QdrantCreatePayloadIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.CreateFieldIndexCollection;
import io.qdrant.client.grpc.Points.FieldType;
import io.qdrant.client.grpc.Points.WriteOrdering;
import io.qdrant.client.grpc.Points.WriteOrderingType;

import java.util.Optional;
import java.util.function.LongFunction;

public class QdrantCreatePayloadIndexOpDispenser extends QdrantBaseOpDispenser<CreateFieldIndexCollection, Points.UpdateResult> {
    public QdrantCreatePayloadIndexOpDispenser(
        QdrantDriverAdapter adapter,
        ParsedOp op,
        LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<CreateFieldIndexCollection> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF) {
        LongFunction<CreateFieldIndexCollection.Builder> ebF =
            l -> CreateFieldIndexCollection.newBuilder().setCollectionName(targetF.apply(l));
        // https://github.com/qdrant/java-client/blob/v1.9.1/src/main/java/io/qdrant/client/QdrantClient.java#L2240-L2248

        ebF = op.enhanceFuncOptionally(ebF, "field_name", String.class, CreateFieldIndexCollection.Builder::setFieldName);

        LongFunction<String> fieldTypeF = op.getAsRequiredFunction("field_type", String.class);
        final LongFunction<CreateFieldIndexCollection.Builder> ftF = ebF;
        ebF = l -> ftF.apply(l).setFieldType(FieldType.valueOf(fieldTypeF.apply(l)));

        Optional<LongFunction<String>> writeOrderingF = op.getAsOptionalFunction("ordering", String.class);
        if (writeOrderingF.isPresent()) {
            LongFunction<CreateFieldIndexCollection.Builder> woF = ebF;
            LongFunction<WriteOrdering> writeOrdrF = buildWriteOrderingFunc(writeOrderingF.get());
            ebF = l -> woF.apply(l).setOrdering(writeOrdrF.apply(l));
        }
        ebF = op.enhanceFuncOptionally(ebF, "wait", Boolean.class, CreateFieldIndexCollection.Builder::setWait);

        final LongFunction<CreateFieldIndexCollection.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    @Override
    public LongFunction<QdrantBaseOp<CreateFieldIndexCollection, Points.UpdateResult>> createOpFunc(
        LongFunction<CreateFieldIndexCollection> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF) {
        return l -> new QdrantCreatePayloadIndexOp(clientF.apply(l), paramF.apply(l));
    }

    private LongFunction<WriteOrdering> buildWriteOrderingFunc(LongFunction<String> stringLongFunction) {
        return l -> {
            return WriteOrdering.newBuilder().setType(WriteOrderingType.valueOf(stringLongFunction.apply(l))).build();
        };
    }
}
