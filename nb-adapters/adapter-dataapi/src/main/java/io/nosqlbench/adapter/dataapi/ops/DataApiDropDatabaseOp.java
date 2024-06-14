/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;

import java.util.UUID;
import java.util.regex.Pattern;

public class DataApiDropDatabaseOp extends DataApiAdminOp {
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private final String nameOrUUID;
    private final boolean isUUID;

    public DataApiDropDatabaseOp(Database db, AstraDBAdmin admin, String nameOrUUID) {
        super(db, admin);
        this.nameOrUUID = nameOrUUID;
        this.isUUID = UUID_PATTERN.matcher(nameOrUUID).matches();
    }

    @Override
    public Object apply(long value) {
        return isUUID ? admin.dropDatabase(UUID.fromString(nameOrUUID)) : admin.dropDatabase(nameOrUUID);
    }
}
