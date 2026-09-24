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

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.collections.commands.ReturnDocument;
import com.datastax.astra.client.collections.commands.options.CollectionReplaceOneOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiCollectionLegacyReplaceOneOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class DataApiCollectionLegacyReplaceOneOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionLegacyReplaceOneOpDispenser.class);
    private final LongFunction<DataApiCollectionLegacyReplaceOneOp> opFunction;

    public DataApiCollectionLegacyReplaceOneOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCollectionLegacyReplaceOneOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = legacyGetFilterFromOp(op, l);
            CollectionReplaceOneOptions options = getCollectionReplaceOneOptions(op, l);
            LongFunction<Map> docMapFunc = op.getAsRequiredFunction("document", Map.class);
            LongFunction<Document> docFunc = (long m) -> new Document(docMapFunc.apply(m));

            return new DataApiCollectionLegacyReplaceOneOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                docFunc.apply(l),
                options
            );
        };
    }

    private CollectionReplaceOneOptions getCollectionReplaceOneOptions(ParsedOp op, long l) {
        CollectionReplaceOneOptions options = new CollectionReplaceOneOptions();
        Sort[] sorts = legacyGetSortFromOp(op, l);
        if (sorts != null) {
            options = options.sort(sorts);
        }
        Projection[] projection = getProjectionFromOp(op, l);
        if (projection != null) {
            options = options.projection(projection);
        }
        Optional<LongFunction<Boolean>> upsertFunction = op.getAsOptionalFunction("upsert", Boolean.class);
        if (upsertFunction.isPresent()) {
            options = options.upsert(upsertFunction.get().apply(l));
        }
        if (op.isDefined("returnDocument")) {
            options = switch ((String) op.get("returnDocument", l)) {
                case "after" -> options.returnDocument(ReturnDocument.AFTER);
                case "before" -> options.returnDocument(ReturnDocument.BEFORE);
                default -> throw new RuntimeException("Invalid returnDocument value: " + op.get("returnDocument", l));
            };
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
