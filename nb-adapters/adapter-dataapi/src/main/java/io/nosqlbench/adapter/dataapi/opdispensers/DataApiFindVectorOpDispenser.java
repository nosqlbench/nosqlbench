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
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.Projection;
import com.datastax.astra.client.model.Sort;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiFindVectorOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiFindVectorOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiFindVectorOpDispenser.class);
    private final LongFunction<DataApiFindVectorOp> opFunction;
    public DataApiFindVectorOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiFindVectorOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            float[] vector = getVectorValues(op, l);
            int limit = getLimit(op, l);
            return new DataApiFindVectorOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                vector,
                limit
            );
        };
    }

    private int getLimit(ParsedOp op, long l) {
        return op.getConfigOr("limit", 100, l);
    }

    @Override
    public DataApiBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
