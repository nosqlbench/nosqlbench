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

package io.nosqlbench.adapter.dataapi.opdispensers.tables;

import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableDeleteManyOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiTableDeleteManyOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;

public class DataApiTableDeleteManyOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiTableDeleteManyOpDispenser.class);
    private final LongFunction<DataApiTableDeleteManyOp> opFunction;

    public DataApiTableDeleteManyOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiTableDeleteManyOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Table<Row> table = db.getTable(targetFunction.apply(l));
            Filter filter = getFilterFromOp(op, l);
            return new DataApiTableDeleteManyOp(
                db,
                table,
                filter,
                new TableDeleteManyOptions()
            );
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
