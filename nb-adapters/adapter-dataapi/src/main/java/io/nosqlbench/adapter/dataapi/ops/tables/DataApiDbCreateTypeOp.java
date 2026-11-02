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
import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;

public class DataApiDbCreateTypeOp extends DataApiBaseOp {
    private final String typeName;
    private final TableUserDefinedTypeDefinition definition;
    private final CreateTypeOptions options;

    public DataApiDbCreateTypeOp(Database db, String typeName, TableUserDefinedTypeDefinition definition, CreateTypeOptions options) {
        super(db);
        this.typeName = typeName;
        this.definition = definition;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        db.createType(typeName, definition, options);
        return null;
    }
}
