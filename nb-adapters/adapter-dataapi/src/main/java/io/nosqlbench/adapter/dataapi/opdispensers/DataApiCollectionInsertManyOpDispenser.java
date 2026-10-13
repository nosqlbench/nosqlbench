/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.adapter.dataapi.opdispensers;

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiCollectionInsertManyOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class DataApiCollectionInsertManyOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionInsertManyOpDispenser.class);
    private final LongFunction<DataApiCollectionInsertManyOp> opFunction;

    public DataApiCollectionInsertManyOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCollectionInsertManyOp> createOpFunction(ParsedOp op) {
        Boolean batched = getBatchedFromOp(op, false, false);
        if (batched) {
            ensureOpKeysAbsent(op, new String[]{"documents"});
            ensureOpKeysPresent(op, new String[]{"batch_size", "document_template"});
            Integer batchSize = getBatchSizeFromOp(op, true, null);
            LongFunction<List<Document>> docsFunc = getTemplatedDocumentsFunctionFromOp(op, batchSize);
            return (l) -> {
                List<Document> documents = docsFunc.apply(l);
                return new DataApiCollectionInsertManyOp(
                    spaceFunction.apply(l).getDatabase(),
                    targetFunction.apply(l),
                    documents,
                    getCollectionInsertManyOptions(op, l)
                );
            };
        }
        // regular explicit-document-list behaviour
        ensureOpKeysAbsent(op, new String[]{"batch_size", "document_template"});
        return (l) -> {
            List<Document> documents = getDocumentsFromOp(op, l);
            return new DataApiCollectionInsertManyOp(
                spaceFunction.apply(l).getDatabase(),
                targetFunction.apply(l),
                documents,
                getCollectionInsertManyOptions(op, l)
            );
        };
    }

    private CollectionInsertManyOptions getCollectionInsertManyOptions(ParsedOp op, long l) {
        CollectionInsertManyOptions options = new CollectionInsertManyOptions();
        options = options.concurrency(1); // hardcoded: avoid client concurrency, let NB use its own.
        Integer chunkSize = getChunkSizeFromOp(op, l);
        if (chunkSize != null) {
            logger.warn(() -> "Setting insertion 'chunk_size'. This is discouraged, as it usually signals an intention to not have 1:1 between ops and HTTP requests.");
            options = options.chunkSize(chunkSize);
        }
        Boolean ordered = getOrderedFromOp(op, l);
        if (ordered != null) {
            options = options.ordered(ordered);
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
