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
import java.util.Map;

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.databases.Database;

import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
public class DataApiDbCreateTableOp extends DataApiBaseOp {
    private final String tableName;
    private final Map<String, Object> definition;

    public DataApiDbCreateTableOp(Database db, String tableName, Map<String, Object> definition) {
        super(db);
        this.tableName = tableName;
        this.definition = definition;
    }

    @Override
    public Object apply(long value) {
        Document payload = new Document();
        payload.append("name", tableName);
        payload.append("definition", definition);
        Command command = new Command("createTable", payload);
        return db.runCommand(command);
    }
}
