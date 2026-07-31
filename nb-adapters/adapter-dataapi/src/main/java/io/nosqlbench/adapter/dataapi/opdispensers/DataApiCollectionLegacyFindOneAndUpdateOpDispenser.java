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
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneAndUpdateOptions;

import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiCollectionLegacyFindOneAndUpdateOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiCollectionLegacyFindOneAndUpdateOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionLegacyFindOneAndUpdateOpDispenser.class);
    private final LongFunction<DataApiCollectionLegacyFindOneAndUpdateOp> opFunction;
    public DataApiCollectionLegacyFindOneAndUpdateOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCollectionLegacyFindOneAndUpdateOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            Update update = getLegacyUpdateFromOp(op, l);
            CollectionFindOneAndUpdateOptions options = getCollectionLegacyFindOneAndUpdateOptions(op, l);

            return new DataApiCollectionLegacyFindOneAndUpdateOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                update,
                options
            );
        };
    }

    private CollectionFindOneAndUpdateOptions getCollectionLegacyFindOneAndUpdateOptions(ParsedOp op, long l) {
        CollectionFindOneAndUpdateOptions options = new CollectionFindOneAndUpdateOptions();
        Sort sort = getSortFromOp(op, l);
        Boolean upsert = getUpsertFromOp(op, l);
        if (sort != null) {
            options = options.sort(sort);
        }
        if ( upsert != null ){
            options = options.upsert(upsert);
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
