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

import io.nosqlbench.converters.cql.exporters.CGKeyspaceStats;
import io.nosqlbench.converters.cql.exporters.CGSchemaStats;
import io.nosqlbench.converters.cql.exporters.CGTableStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

/**
 * This model contains definition level details for schema elements which are parsed from the
 * Antlr4 CQL grammar.
 * Because keyspace, table, column, and type elements are handled sometimes in different ways,
 * these are stored in separate data structures.
 * When you see a *refddl or similar field, this is a copy of the text image from the original
 * parsed syntax. These are used for populating schema blocks without doing a full parse.
 * If you update either the refddl or the actual AST level elements for any of the types in this
 * model, you are required to update the other version along with it, using string substitution
 * if necessary.
 */
public class CqlModel {
    private final static Logger logger = LogManager.getLogger(CqlModel.class);

    private final Supplier<List<String>> errors;
    Map<String, CqlKeyspace> keyspaceDefs = new LinkedHashMap<>();
    Map<String, Map<String, CqlTable>> tableDefs = new LinkedHashMap<>();
    Map<String, Map<String, CqlType>> types = new LinkedHashMap<>();

   CGSchemaStats schemaStats = null;

    public CGSchemaStats getKeyspaceAttributes() {
        return schemaStats;
    }

    public void setKeyspaceAttributes(CGSchemaStats schemaStats) {
        this.schemaStats = schemaStats;
        for (String statsKeyspacename : schemaStats.getKeyspaces().keySet()) {
            CGKeyspaceStats keyspaceStats = schemaStats.getKeyspace(statsKeyspacename);
            if (keyspaceDefs.containsKey(statsKeyspacename)) {
                logger.debug("setting         keyspace stats for '" + statsKeyspacename + "'");
                keyspaceDefs.get(statsKeyspacename).setStats(keyspaceStats);
            } else {
                logger.debug("       skipping keyspace stats for '" + statsKeyspacename + "'");
            }

            for (String statsTableName : keyspaceStats.getKeyspaceTables().keySet()) {
                CGTableStats tableStats = keyspaceStats.getKeyspaceTables().get(statsTableName);
                Map<String, CqlTable> modelTables = tableDefs.get(statsKeyspacename);
                if (modelTables!=null) {
                    CqlTable modelTable = modelTables.get(statsTableName);
                    if (modelTable!=null) {
                        logger.debug("setting table stats for '" + statsKeyspacename+"."+statsTableName+"'");
                        modelTable.setTableAttributes(tableStats);
                    } else {
                        logger.debug("       skipping table stats for '" + statsKeyspacename + "."+statsTableName+"'");
                    }
                } else {
                    logger.debug("       SKIPPING stats for all tables in keyspace '" + statsKeyspacename + "'");
                }
            }
        }
    }

    transient CqlKeyspace keyspace = null;
    transient CqlTable table;
    transient CqlType udt;

    public boolean hasStats() {
        return schemaStats!=null;
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
        this.keyspaceDefs.put(text, keyspace);
        keyspace=null;
    }

    public void newTable() {
        table = new CqlTable();
    }

    public void saveTable(String keyspace, String text) {
        table.setKeyspace(keyspace);
        table.setName(text);
        this.tableDefs.computeIfAbsent(keyspace, ks->new LinkedHashMap<>()).put(text, table);
        table = null;
    }

    public void saveColumnDefinition(String colname, String typedef, boolean isPrimaryKey, int position) {
        this.table.addcolumnDef(colname, typedef, position);
        if (isPrimaryKey) {
            this.table.addPartitionKey(colname);
        }
    }

    public Map<String, CqlKeyspace> getKeyspacesByName() {
        return keyspaceDefs;
    }

    public List<CqlKeyspace> getKeyspaceDefs() {
        return new ArrayList<>(this.keyspaceDefs.values());
    }

    public Map<String, Map<String, CqlTable>> getTablesByNameByKeyspace() {
        return tableDefs;
    }

    public List<CqlTable> getTablesForKeyspace(String ksname) {
        Map<String, CqlTable> tables = this.tableDefs.get(ksname);
        if (tables!=null) {
            return new ArrayList<>(tables.values());
        }
        return List.of();
    }

    public List<CqlTable> getTableDefs() {
        return tableDefs.values().stream().flatMap(m->m.values().stream()).toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String ks : keyspaceDefs.keySet()) {
            CqlKeyspace keyspace = keyspaceDefs.get(ks);
            sb.append("keyspace '").append(keyspace.getName()).append("':\n");
            sb.append(keyspace).append("\n");

            tableDefs.getOrDefault(ks,Map.of()).values().stream()
                .forEach(table -> {
                    sb.append("table '").append(table.getName()).append("':\n");
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
        ksnames.addAll(this.keyspaceDefs.keySet());
        ksnames.addAll(this.tableDefs.keySet());
        return ksnames;
    }

    public void addPartitionKey(String partitionKey) {
        table.addPartitionKey(partitionKey);
    }

    public void addClusteringColumn(String ccolumn) {
        table.addClusteringColumn(ccolumn);
    }

    public void newType() {
        udt = new CqlType();
    }

    public void addTypeField(String name, String typedef) {
        udt.addField(name, typedef);
    }

    public void saveType(String keyspace, String name) {
        udt.setKeyspace(keyspace);
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

    public void removeKeyspaceDef(String ksname) {
        this.keyspaceDefs.remove(ksname);
    }

    public void removeTablesForKeyspace(String ksname) {
        this.tableDefs.remove(ksname);
    }

    public void removeTypesForKeyspace(String name) {
        this.types.remove(name);
    }

    public String getSummaryLine() {
        return "keyspaces: " + keyspaceDefs.size() + ", tables: " + getTableDefs().size()  +
            ", columns: " + getTableDefs().stream().mapToInt(t -> t.getColumnDefinitions().size()).sum() +
            ", types: " + getTypes().size();
    }

    public void renamekeyspace(String keyspaceName, String newKeyspaceName) {
        if (this.keyspaceDefs.containsKey(keyspaceName)) {
            CqlKeyspace keyspace = this.keyspaceDefs.remove(keyspaceName);
            keyspace.setKeyspaceName(newKeyspaceName);
            this.keyspaceDefs.put(newKeyspaceName, keyspace);
        }
        if (this.tableDefs.containsKey(keyspaceName)) {
            Map<String, CqlTable> tablesForKeyspace = this.tableDefs.remove(keyspaceName);
            if (tablesForKeyspace!=null) {
                for (CqlTable table : tablesForKeyspace.values()) {
                    table.setKeyspace(newKeyspaceName);
                }
            }
            this.tableDefs.put(newKeyspaceName, tablesForKeyspace);
        }
        if (this.types.containsKey(keyspaceName)) {
            Map<String, CqlType> typesForKeyspace = this.types.remove(keyspaceName);
            if (typesForKeyspace!=null) {
                for (CqlType cqltype : typesForKeyspace.values()) {
                    cqltype.setKeyspace(newKeyspaceName);
                }
            }
            this.types.put(newKeyspaceName,typesForKeyspace);
        }
    }

    public void renameTable(CqlTable extant, String newTableName) {
        Map<String, CqlTable> tablesInKs = tableDefs.get(extant.getKeySpace());
        CqlTable table = tablesInKs.get(extant.getName());
        table.setName(newTableName);
        tablesInKs.put(table.getName(),table);
    }

    public void renameType(String keyspaceName, String typeName, String newTypeName) {
        Map<String,CqlType> typesInKeyspace = types.get(keyspaceName);
        CqlType cqlType = typesInKeyspace.remove(typeName);
        cqlType.setName(newTypeName);
        typesInKeyspace.put(newTypeName,cqlType);
    }

    public void setTableCompactStorage(boolean isCompactStorage) {
        table.setCompactStorage(isCompactStorage);
    }

    public void setKeyspaceDurableWrites(String booleanLiteral) {
        keyspace.setDurableWrites(Boolean.parseBoolean(booleanLiteral));
    }

    public void setReplicationData(String repldata) {
        keyspace.setReplicationData(repldata);
    }

    public Map<String, Map<String, CqlType>> getTypesByKeyspaceAndName() {
        return types;
    }

    public void addClusteringOrder(String colname, String order) {
        table.addTableClusteringOrder(colname, order);
    }
}
