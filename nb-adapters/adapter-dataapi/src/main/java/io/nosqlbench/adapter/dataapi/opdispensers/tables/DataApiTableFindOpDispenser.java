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
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiTableFindOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.LongFunction;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;

public class DataApiTableFindOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiTableFindOpDispenser.class);
    private final LongFunction<DataApiTableFindOp> opFunction;

    public DataApiTableFindOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiTableFindOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Table<Row> table = db.getTable(targetFunction.apply(l));
            Filter filter = getFilterFromOp(op, l);
            TableFindOptions options = new TableFindOptions();
            Sort[] sorts = getSortFromOp(op, l);
            if (sorts != null) {
                options = options.sort(sorts);
            }
            Projection[] projection = getProjectionFromOp(op, l);
            if (projection != null) {
                options = options.projection(projection);
            }
            Optional<Integer> limit = getLimitFromOp(op, l);
            if (limit.isPresent()) {
                options = options.limit(limit.get());
            }
            Optional<Integer> skip = getSkipFromOp(op, l);
            if (skip.isPresent()) {
                options = options.skip(skip.get());
            }
            return new DataApiTableFindOp(
                db,
                table,
                filter,
                options
            );
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
