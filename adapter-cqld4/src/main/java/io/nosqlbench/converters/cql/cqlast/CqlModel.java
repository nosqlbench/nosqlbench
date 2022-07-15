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

import java.util.*;
import java.util.function.Supplier;

public class CqlModel {

    private final Supplier<List<String>> errors;
    Map<String, CqlKeyspace> keyspaces = new LinkedHashMap<>();
    Map<String, Map<String, CqlTable>> tables = new LinkedHashMap<>();
    Map<String, Map<String, CqlType>> types = new LinkedHashMap<>();

    transient CqlKeyspace keyspace = null;
    transient CqlTable table;
    transient CqlType udt;

    public boolean hasStats() {
        return keyspaces.size()>0 && keyspaces.values().iterator().next().getKeyspaceAttributes().size()!=0;
    }
    public CqlModel(Supplier<List<String>> errorSource) {
        this.errors = errorSource;
    }

    public List<String> getErrors() {
        return errors.get();
    }

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
        table.setTable(text);
        table.setRefDdl(refddl);
        this.tables.computeIfAbsent(keyspace, ks->new LinkedHashMap<>()).put(text, table);
        table = null;
    }

    public void saveColumnDefinition(String colname, String coltype, boolean isPrimaryKey, String refddl) {
        this.table.addcolumnDef(colname, coltype, refddl);
        if (isPrimaryKey) {
            this.table.addPartitionKey(colname);
        }
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
            sb.append(keyspace).append("\n");

            tables.getOrDefault(ks,Map.of()).values().stream()
                .forEach(table -> {
                    sb.append("table '").append(table.getTableName()).append("':\n");
                    sb.append(table);
                });
        }
        return sb.toString();
    }

    /**
     * Get all the keyspace names which have been referenced in any way, whether or not
     * this was in a keyspace definition or some other DDL like table or udt names.
     * @return A list of all known keyspace names
     */
    public Set<String> getAllKnownKeyspaceNames() {
        Set<String> ksnames = new LinkedHashSet<>();
        ksnames.addAll(this.keyspaces.keySet());
        ksnames.addAll(this.tables.keySet());
        return ksnames;
    }

    public void addPartitionKey(String partitionKey) {
        table.addPartitionKey(partitionKey);
    }

    public void addClusteringColumn(String ccolumn) {
        table.addClusteringColumn(ccolumn);
    }

    public void setReplicationText(String repldata) {
        keyspace.setRefReplDdl(repldata);
    }

    public void newType() {
        udt = new CqlType();
    }

    public void addTypeField(String name, String typedef, String typedefRefDdl) {
        udt.addField(name, typedef, typedefRefDdl);
    }

    public void saveType(String keyspace, String name, String refddl) {
        udt.setKeyspace(keyspace);
        udt.setRefddl(refddl);
        udt.setName(name);
        Map<String, CqlType> ksTypes = this.types.computeIfAbsent(keyspace, ks -> new LinkedHashMap<>());
        ksTypes.put(udt.getName(),udt);
        udt=null;
    }

    public List<CqlType> getTypes() {
        ArrayList<CqlType> list = new ArrayList<>();
        for (Map<String, CqlType> cqlTypesByKeyspace : types.values()) {
            for (CqlType cqlType : cqlTypesByKeyspace.values()) {
                list.add(cqlType);
            }
        }
        return list;

    }
}
