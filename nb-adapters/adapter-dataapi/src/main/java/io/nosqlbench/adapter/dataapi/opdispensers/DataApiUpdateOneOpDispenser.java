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

package io.nosqlbench.adapter.dataapi.opdispensers;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.*;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiUpdateOneOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class DataApiUpdateOneOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiUpdateOneOpDispenser.class);
    private final LongFunction<DataApiUpdateOneOp> opFunction;

    public DataApiUpdateOneOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiUpdateOneOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            UpdateOneOptions options = getUpdateOneOptions(op, l);
            LongFunction<Map<String,Object>> docMapFunc = op.getAsRequiredFunction("update", Map.class);

            return new DataApiUpdateOneOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                filter,
                new Update(docMapFunc.apply(l)),
                options
            );
        };
    }

    private UpdateOneOptions getUpdateOneOptions(ParsedOp op, long l) {
        UpdateOneOptions options = new UpdateOneOptions();
        Sort sort = getSortFromOp(op, l);
        float[] vector = getVectorFromOp(op, l);

        Optional<LongFunction<Boolean>> upsertFunction = op.getAsOptionalFunction("upsert", Boolean.class);
        if (upsertFunction.isPresent()) {
            options = options.upsert(upsertFunction.get().apply(l));
        }
        if (sort != null) {
            options = (vector != null) ? options.vector(vector, sort) : options.sort(sort);
        }
        return options;
    }

    private float[] getVectorFromOp(ParsedOp op, long l) {
        return getVectorValues(op.get("vector", l));
    }

    @Override
    public DataApiBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
