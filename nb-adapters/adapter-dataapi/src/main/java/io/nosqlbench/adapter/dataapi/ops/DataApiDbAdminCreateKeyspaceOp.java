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

package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.datastax.astra.client.admin.DatabaseAdmin;

public class DataApiDbAdminCreateKeyspaceOp extends DataApiDBAdminOp {
    private final String keyspace;
    private final Optional<Object> replication;

    public DataApiDbAdminCreateKeyspaceOp(Database db, DatabaseAdmin admin, String keyspace, Optional<Object> replication) {
        super(db, admin);
        this.keyspace = keyspace;
        this.replication = replication;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object apply(long value) {
        KeyspaceDefinition definition = new KeyspaceDefinition().name(keyspace);
        if (replication.isPresent()) {
            Object replValue = replication.get();
            if (replValue instanceof Integer factor) {
                definition = definition.simpleStrategy(factor);
            } else if (replValue instanceof Map<?, ?> map) {
                definition = definition.networkTopologyStrategy((Map<String, Integer>) replValue);
            } else {
                throw new RuntimeException("Invalid value type for 'replication'");
            }
        }
        admin.createKeyspace(definition);
        return true;
    }
}
