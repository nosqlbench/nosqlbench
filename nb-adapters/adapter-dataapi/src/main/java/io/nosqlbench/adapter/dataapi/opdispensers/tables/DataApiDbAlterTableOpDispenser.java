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
import com.datastax.astra.client.tables.commands.AlterTableOperation;
import com.datastax.astra.client.tables.commands.options.AlterTableOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiDbAlterTableOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser for the {@code db_alter_table} op type.
 *
 * <p>Required YAML fields:
 * <ul>
 *   <li>{@code target} — table name</li>
 *   <li>{@code alter_table_operation} — map with one of:
 *     <ul>
 *       <li>{@code add} — map of columnName -&gt; type string to add</li>
 *       <li>{@code drop} — list of column names to remove</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class DataApiDbAlterTableOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiDbAlterTableOpDispenser.class);
    private final LongFunction<DataApiDbAlterTableOp> opFunction;

    public DataApiDbAlterTableOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiDbAlterTableOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Table<Row> table = db.getTable(targetFunction.apply(l));
            AlterTableOperation operation = getAlterTableOperationFromOp(op, l);
            return new DataApiDbAlterTableOp(db, table, operation, new AlterTableOptions());
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
