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

package io.nosqlbench.adapter.dataapi.opdispensers.collections;

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filter;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.collections.DataApiCollectionCountDocumentsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;
import java.util.Optional;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;

public class DataApiCollectionCountDocumentsOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionCountDocumentsOpDispenser.class);
    private final LongFunction<DataApiCollectionCountDocumentsOp> opFunction;

    public DataApiCollectionCountDocumentsOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCollectionCountDocumentsOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            Optional<LongFunction<Integer>> ubf = op.getAsOptionalFunction("upper_bound", Integer.class);
            Optional<Integer> upperBound;
            if (ubf.isPresent()) {
                upperBound = Optional.of(ubf.get().apply(l));
            } else {
                upperBound = Optional.empty();
            }

            return new DataApiCollectionCountDocumentsOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                upperBound
            );
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
