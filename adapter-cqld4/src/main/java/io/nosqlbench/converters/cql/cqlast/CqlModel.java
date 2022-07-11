/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.converters.cql.cqlast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CqlModel {

    Map<String, CqlKeyspace> keyspaces = new LinkedHashMap<>();
    Map<String, Map<String, CqlTable>> tables = new LinkedHashMap<>();

    transient
    CqlKeyspace keyspace = null;
    transient
    CqlTable table;


    public void newKeyspace() {
        keyspace = new CqlKeyspace();
    }

    public void saveKeyspace(String text,String refddl) {
        keyspace.setKeyspaceName(text);
        keyspace.setRefDdl(refddl);
        this.keyspaces.put(text, keyspace);
        keyspace=null;
    }

    public void newTable() {
        table = new CqlTable();
    }

    public void saveTable(String keyspace, String text, String refddl) {
        table.setKeyspace(keyspace);
        table.setName(text);
        table.setRefDdl(refddl);
        this.tables.computeIfAbsent(keyspace, ks->new LinkedHashMap<>()).put(text, table);
        table = null;
    }

    public void saveColumnDefinition(String coltype, String colname) {
        table.addcolumnDef(coltype, colname);
    }

    public Map<String, CqlKeyspace> getKeyspaces() {
        return keyspaces;
    }

    public Map<String, Map<String, CqlTable>> getTablesByKeyspace() {
        return tables;
    }

    public List<CqlTable> getAllTables() {
        return tables.values().stream().flatMap(m->m.values().stream()).toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String ks : keyspaces.keySet()) {
            CqlKeyspace keyspace = keyspaces.get(ks);
            sb.append("keyspace '").append(keyspace.getKeyspaceName()).append("':\n");
            sb.append(keyspace.toString()).append("\n");

            tables.getOrDefault(ks,Map.of()).values().stream()
                .forEach(table -> {
                    sb.append("table '").append(table.getTableName()).append("':\n");
                    sb.append(table.toString());
                });
        }
        return sb.toString();
    }
}
