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

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Sort;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiDeleteOneOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiDeleteOneOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiDeleteOneOpDispenser.class);
    private final LongFunction<DataApiDeleteOneOp> opFunction;

    public DataApiDeleteOneOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiDeleteOneOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            DeleteOneOptions options = getDeleteOneOptions(op, l);

            return new DataApiDeleteOneOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                options
            );
        };
    }

    private DeleteOneOptions getDeleteOneOptions(ParsedOp op, long l) {
        DeleteOneOptions options = new DeleteOneOptions();
        Sort sort = getSortFromOp(op, l);
        float[] vector = getVectorFromOp(op, l);
        if (sort != null) {
            options = (vector != null) ? options.sort(vector, sort) : options.sort(sort);
        } else if (vector != null) {
            options = options.sort(vector, null);
        }
        return options;
    }

    private float[] getVectorFromOp(ParsedOp op, long l) {
        if (op.isDefined("vector")) {
            return getVectorValues(op.get("vector", l));
        }
        return null;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
