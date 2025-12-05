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
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiCollectionFindByIdOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiCollectionFindOneOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class DataApiCollectionFindByIdOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiCollectionFindByIdOpDispenser.class);
    private final LongFunction<DataApiCollectionFindByIdOp> opFunction;
    public DataApiCollectionFindByIdOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiCollectionFindByIdOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Object id = getIdFromOp(op, l);
            return new DataApiCollectionFindByIdOp(
                db,
                db.getCollection(targetFunction.apply(l)),
                id
            );
        };
    }

    private Object getIdFromOp(ParsedOp op, long l) {
        return op.getAsRequiredFunction("id", Object.class).apply(l);
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
