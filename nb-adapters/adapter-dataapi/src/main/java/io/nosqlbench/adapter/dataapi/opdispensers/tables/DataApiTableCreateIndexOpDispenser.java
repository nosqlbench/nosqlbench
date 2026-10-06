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
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import io.nosqlbench.adapter.dataapi.DataApiDriverAdapter;
import io.nosqlbench.adapter.dataapi.opdispensers.DataApiOpDispenser;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.tables.DataApiTableCreateIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser for the {@code table_create_index} op type.
 *
 * <p>Required YAML fields:
 * <ul>
 *   <li>{@code target} — table name</li>
 *   <li>{@code index_name} — name of the index to create</li>
 *   <li>{@code index_definition.column} — column to index</li>
 * </ul>
 * Optional index definition fields: {@code ascii}, {@code normalize}, {@code case_sensitive}.
 * Optional: {@code if_not_exists} (boolean).
 */
public class DataApiTableCreateIndexOpDispenser extends DataApiOpDispenser {
    private static final Logger logger = LogManager.getLogger(DataApiTableCreateIndexOpDispenser.class);
    private final LongFunction<DataApiTableCreateIndexOp> opFunction;

    public DataApiTableCreateIndexOpDispenser(DataApiDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<DataApiTableCreateIndexOp> createOpFunction(ParsedOp op) {
        return (l) -> {
            Database db = spaceFunction.apply(l).getDatabase();
            Table<Row> table = db.getTable(targetFunction.apply(l));
            String indexName = op.getAsRequiredFunction("index_name", String.class).apply(l);
            TableRegularIndexDefinition definition = getRegularIndexDefinitionFromOp(op, l);
            CreateIndexOptions options = getCreateIndexOptionsFromOp(op, l);
            return new DataApiTableCreateIndexOp(db, table, indexName, definition, options);
        };
    }

    @Override
    public DataApiBaseOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
