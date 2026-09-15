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
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import com.datastax.astra.client.collections.commands.ReturnDocument;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneAndReplaceOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import com.datastax.astra.client.collections.definition.documents.Document;
import io.nosqlbench.adapter.dataapi.ops.DataApiCollectionFindOneAndReplaceOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.LongFunction;

public class DataApiCollectionFindOneAndReplaceOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionFindOneAndReplaceOpDispenser.class);
    private final LongFunction<DataApiCollectionFindOneAndReplaceOp> opFunction;

    public DataApiCollectionFindOneAndReplaceOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCollectionFindOneAndReplaceOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            CollectionFindOneAndReplaceOptions options = getCollectionFindOneAndReplaceOptions(op, l);
            Document replacement = getReplacementFromOp(op, l);

            return new DataApiCollectionFindOneAndReplaceOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                replacement,
                options
            );
        };
    }

    private CollectionFindOneAndReplaceOptions getCollectionFindOneAndReplaceOptions(ParsedOp op, long l) {
        CollectionFindOneAndReplaceOptions options = new CollectionFindOneAndReplaceOptions();
        Sort sort = getSortFromOp(op, l);
        if (sort != null) {
            options = options.sort(sort);
        }
        Projection[] projection = getProjectionFromOp(op, l);
        if (projection != null) {
            options = options.projection(projection);
        }
        Boolean upsert = getUpsertFromOp(op, l);
        if (upsert != null) {
            options = options.upsert(upsert);
        }
        ReturnDocument returnDocument = getReturnDocumentFromOp(op, l);
        if (returnDocument != null){
            options = options.returnDocument(returnDocument);
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
