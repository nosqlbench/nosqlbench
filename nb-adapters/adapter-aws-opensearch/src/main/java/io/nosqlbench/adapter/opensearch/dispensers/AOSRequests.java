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

package io.nosqlbench.adapter.opensearch.dispensers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.opensearch._types.OpType;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.VersionType;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkOperationVariant;
import org.opensearch.client.opensearch.core.bulk.CreateOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

public class AOSRequests {

    private final static Logger logger = LogManager.getLogger(AOSIndexOpDispenser.class);
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> LongFunction<BulkRequest> bulk(ParsedOp op, LongFunction<String> targetF) {
        LongFunction<BulkRequest.Builder> func = l -> new BulkRequest.Builder();
        LongFunction<BulkRequest.Builder> finalFunc2 = func;
        func = l -> finalFunc2.apply(l).index(targetF.apply(l));
        func = op.enhanceFuncOptionally(func, "pipeline", String.class, BulkRequest.Builder::pipeline);
        func = op.enhanceEnumOptionally(func, "refresh", Refresh.class, BulkRequest.Builder::refresh);
        func = op.enhanceFuncOptionally(func, "routing", String.class, BulkRequest.Builder::routing);
        func = op.enhanceFuncOptionally(func, "requireAlias", boolean.class, BulkRequest.Builder::requireAlias);
        func = op.enhanceFuncOptionally(func, "sourceExcludes", List.class, BulkRequest.Builder::sourceExcludes);

        ParsedOp subop = op.getAsSubOp("op_template", ParsedOp.SubOpNaming.ParentAndSubKey);
        int repeat = subop.getStaticConfigOr("repeat", 1);

        TypeAndTarget<AOSBulkOpTypes, String> typeinfo =
            subop.getTypeAndTarget(AOSBulkOpTypes.class, String.class);

        LongFunction<BulkOperationVariant> bop = switch (typeinfo.enumId) {
            case create -> AOSRequests.createOperation(subop);
            case index -> AOSRequests.indexOperation(subop);
            default -> throw new OpConfigError("Unsupported type in bulk operation: '" + typeinfo.enumId + "'");
        };

        LongFunction<List<BulkOperation>> bov =
            l -> {
                ArrayList<BulkOperation> variants = new ArrayList<>();
                long floor = repeat * l;
                for (long i = floor; i < floor + repeat; i++) {
                    variants.add(bop.apply(i)._toBulkOperation());
                }
                return variants;
            };
        LongFunction<BulkRequest.Builder> finalFunc = func;
        func = l -> finalFunc.apply(l).operations(bov.apply(l));

//        func = op.enhanceFuncOptionally(func, "source", Object.class, this::resolveSourceConfigParam);
//        func = op.enhanceFuncOptionally(func, "waitForActiveShards", Object.class, this::resolveWaitForActiveShards);
//        func = op.enhanceFuncOptionally(func, "timeout", Object.class, this::resolveTimeout);
        LongFunction<BulkRequest.Builder> finalFunc1 = func;
        return l -> finalFunc1.apply(l).build();
    }

    private static LongFunction<BulkOperationVariant> createOperation(ParsedOp op) {
        LongFunction<CreateOperation.Builder> func = l -> new CreateOperation.Builder<>();
        func = op.enhanceFuncOptionally(func, "index", String.class, (b, v) -> (CreateOperation.Builder) b.index(v));
        func = op.enhanceFuncOptionally(func, "document", Object.class,
            (b, v) -> (CreateOperation.Builder) b.document(v));
        LongFunction<CreateOperation.Builder> finalFunc = func;
        return l -> finalFunc.apply(l).build();

    }

    public static <T> LongFunction<BulkOperationVariant> indexOperation(ParsedOp op) {
        LongFunction<IndexOperation.Builder> func = l -> new IndexOperation.Builder<>();
        func = op.enhanceFuncOptionally(func, "index", String.class, (b, v) -> (IndexOperation.Builder) b.index(v));
        func = op.enhanceFuncOptionally(func, "document", Object.class,
            (b, v) -> (IndexOperation.Builder) b.document(v));
        LongFunction<IndexOperation.Builder> finalFunc = func;
        return l -> finalFunc.apply(l).build();
    }

    public static <T> LongFunction<IndexRequest> index(ParsedOp op) {
        LongFunction<IndexRequest.Builder> func = l -> new IndexRequest.Builder<>();
        func = op.enhanceFuncOptionally(func, "index", String.class, IndexRequest.Builder::index);
        func = op.enhanceFuncOptionally(func, "id", String.class, IndexRequest.Builder::id);
        func = op.enhanceFuncOptionally(func, "ifPrimaryTerm", long.class, IndexRequest.Builder::ifPrimaryTerm);
        func = op.enhanceFuncOptionally(func, "ifSeqNo", long.class, IndexRequest.Builder::ifSeqNo);
        func = op.enhanceFuncOptionally(func, "pipeline", String.class, IndexRequest.Builder::pipeline);
        func = op.enhanceFuncOptionally(func, "routing", String.class, IndexRequest.Builder::routing);
        func = op.enhanceFuncOptionally(func, "requireAlias", boolean.class, IndexRequest.Builder::requireAlias);
        func = op.enhanceFuncOptionally(func, "version", long.class, IndexRequest.Builder::version);
        func = op.enhanceEnumOptionally(func, "opType", OpType.class, IndexRequest.Builder::opType);
        func = op.enhanceEnumOptionally(func, "versionType", VersionType.class, IndexRequest.Builder::versionType);
        func = op.enhanceFuncPivot(func, "document", Object.class, AOSRequests::bindDocument);
        LongFunction<IndexRequest.Builder> finalFunc1 = func;
        return l -> finalFunc1.apply(l).build();
    }


    private static <T> IndexRequest.Builder<T> bindDocument(IndexRequest.Builder<T> builder, T docdata) {
        String document = gson.toJson(docdata);
//        if (diag.equals("true")) {
//            logger.debug("index_op document:\n----\n" + document + "\n----\n");
//        }
        return builder.document(docdata);
    }
}
