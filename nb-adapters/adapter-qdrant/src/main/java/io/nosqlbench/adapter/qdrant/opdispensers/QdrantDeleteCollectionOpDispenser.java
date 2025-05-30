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
import io.nosqlbench.adapter.qdrant.ops.QdrantDeleteCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.DeleteCollection;

import java.util.List;
import java.util.function.LongFunction;

public class QdrantDeleteCollectionOpDispenser
    extends QdrantBaseOpDispenser<DeleteCollection, Collections.CollectionOperationResponse> {

    /**
     * Create a new {@link QdrantDeleteCollectionOpDispenser} subclassed from {@link QdrantBaseOpDispenser}.
     *
     * @param adapter        The associated {@link QdrantDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Qdrant object for this Op
     * @see <a href="https://qdrant.github.io/qdrant/redoc/index.html#tag/collections/operation/delete_collection">Qdrant Delete Collection</a>.
     */
    public QdrantDeleteCollectionOpDispenser(QdrantDriverAdapter adapter,
                                             ParsedOp op,
                                             LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<DeleteCollection> getParamFunc(LongFunction<QdrantClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        LongFunction<DeleteCollection.Builder> ebF =
            l -> DeleteCollection.newBuilder().setCollectionName(targetF.apply(l));
        return l -> ebF.apply(l).build();
    }

    @Override
    public LongFunction<QdrantBaseOp<DeleteCollection, Collections.CollectionOperationResponse>> createOpFunc(LongFunction<DeleteCollection> paramF, LongFunction<QdrantClient> clientF, ParsedOp op, LongFunction<String> targetF) {
        return l -> new QdrantDeleteCollectionOp(clientF.apply(l), paramF.apply(l));
    }

}
