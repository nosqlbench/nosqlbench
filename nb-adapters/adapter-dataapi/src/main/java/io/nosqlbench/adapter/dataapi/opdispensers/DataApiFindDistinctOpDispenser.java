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
 *
 */

package io.nosqlbench.adapter.dataapi.opdispensers;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Filter;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiFindDistinctOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiFindDistinctOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiFindDistinctOpDispenser.class);
    private final LongFunction<DataApiFindDistinctOp> opFunction;
    public DataApiFindDistinctOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiFindDistinctOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Filter filter = getFilterFromOp(op, l);
            Class<?> targetClass = getTargetClass(op, l);
            return new DataApiFindDistinctOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                op.getAsRequiredFunction("fieldName", String.class).apply(l),
                filter,
                targetClass
            );
        };
    }

    private Class<?> getTargetClass(ParsedOp op, long l) {
        String className = op.getAsFunctionOr("resultClass", "java.lang.String").apply(l);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataApiBaseOp getOp(long value) {
        return opFunction.apply(value);
    }
}
