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

package io.nosqlbench.adapter.dataapi.ops.tables;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTextIndexOptions;
import com.datastax.astra.client.tables.definition.indexes.TableTextIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

public class DataApiTableCreateTextIndexOp extends DataApiBaseOp {
    private final Table<Row> table;
    private final String indexName;
    private final TableTextIndexDefinition definition;
    private final CreateTextIndexOptions options;

    public DataApiTableCreateTextIndexOp(Database db, Table<Row> table, String indexName, TableTextIndexDefinition definition, CreateTextIndexOptions options) {
        super(db);
        this.table = table;
        this.indexName = indexName;
        this.definition = definition;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        table.createTextIndex(indexName, definition, options);
        return null;
    }
}
