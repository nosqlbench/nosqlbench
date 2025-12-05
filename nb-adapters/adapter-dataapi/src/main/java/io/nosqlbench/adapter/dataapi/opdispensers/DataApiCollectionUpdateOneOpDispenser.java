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
import com.datastax.astra.client.collections.commands.options.CollectionUpdateOneOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.collections.commands.Update;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiUpdateOneOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class DataApiCollectionUpdateOneOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionUpdateOneOpDispenser.class);
    private final LongFunction<DataApiUpdateOneOp> opFunction;

    public DataApiCollectionUpdateOneOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiUpdateOneOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            CollectionUpdateOneOptions options = getCollectionUpdateOneOptions(op, l);
            LongFunction<Map> docMapFunc = op.getAsRequiredFunction("update", Map.class);

            return new DataApiUpdateOneOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                new Update(docMapFunc.apply(l)),
                options
            );
        };
    }

    private CollectionUpdateOneOptions getCollectionUpdateOneOptions(ParsedOp op, long l) {
        CollectionUpdateOneOptions options = new CollectionUpdateOneOptions();
        Sort sort = getSortFromOp(op, l);
        float[] vector = getVectorFromOp(op, l);

        Optional<LongFunction<Boolean>> upsertFunction = op.getAsOptionalFunction("upsert", Boolean.class);
        if (upsertFunction.isPresent()) {
            options = options.upsert(upsertFunction.get().apply(l));
        }
        if (sort != null) {
            options = (vector != null) ? options.sort(Sort.vector(vector), sort) : options.sort(sort);
        }
        return options;
    }

    private float[] getVectorFromOp(ParsedOp op, long l) {
        return getVectorValues(op.get("vector", l));
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
