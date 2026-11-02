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

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiTableInsertManyOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.LongFunction;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;

public class DataApiTableInsertManyOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiTableInsertManyOpDispenser.class);
    private final LongFunction<DataApiTableInsertManyOp> opFunction;

    public DataApiTableInsertManyOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiTableInsertManyOp> createOpFunction(ParsedOp op) {
        Boolean batched = getBatchedFromOp(op, false, false);
        if (batched) {
            ensureOpKeysAbsent(op, new String[]{"rows"});
            ensureOpKeysPresent(op, new String[]{"batch_size", "row_template"});
            Integer batchSize = getBatchSizeFromOp(op, true, null);
            LongFunction<List<Row>> rowsFunc = getTemplatedRowsFunctionFromOp(op, batchSize);
            return (l) -> {
                List<Row> rows = rowsFunc.apply(l);
                Database db = spaceFunction.apply(l).getDatabase();
                Table<Row> table = db.getTable(targetFunction.apply(l));
                return new DataApiTableInsertManyOp(
                    db,
                    table,
                    rows,
                    getTableInsertManyOptions(op, l)
                );
            };
        }
        // regular explicit-row-list behaviour
        ensureOpKeysAbsent(op, new String[]{"batch_size", "row_template"});
        return (l) -> {
            List<Row> rows = getRowsFromOp(op, l);
            Database db = spaceFunction.apply(l).getDatabase();
            Table<Row> table = db.getTable(targetFunction.apply(l));
            return new DataApiTableInsertManyOp(
                db,
                table,
                rows,
                getTableInsertManyOptions(op, l)
            );
        };
    }

    private TableInsertManyOptions getTableInsertManyOptions(ParsedOp op, long l) {
        TableInsertManyOptions options = new TableInsertManyOptions();
        options = options.concurrency(1); // hardcoded: avoid client concurrency, let NB use its own.
        Boolean ordered = getOrderedFromOp(op, l);
        Integer chunkSize = getChunkSizeFromOp(op, l);
        if (chunkSize != null) {
            logger.warn(() -> "Setting insertion 'chunk_size'. This is discouraged, as it usually signals an intention to not have 1:1 between ops and HTTP requests.");
            options = options.chunkSize(chunkSize);
        }
        if (ordered != null) {
            options = options.ordered(ordered);
        }
        return options;
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
