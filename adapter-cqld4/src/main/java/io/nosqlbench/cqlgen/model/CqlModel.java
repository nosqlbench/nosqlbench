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

package io.nosqlbench.cqlgen.model;

import io.nosqlbench.cqlgen.api.CqlModelInfo;
import io.nosqlbench.cqlgen.core.CGKeyspaceStats;
import io.nosqlbench.cqlgen.core.CGSchemaStats;
import io.nosqlbench.cqlgen.core.CGTableStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

/**
 * <p>
 * This model contains definition level details for schema elements which are parsed from the
 * Antlr4 CQL grammar.
 * Key elements include:
 * <UL>
 * <LI>keyspace definitions, organized by keyspace name</LI>
 * <li>type definitions, organized by keyspace name</li>
 * <li>table definitions with included column definitions, organized by keyspace name</li>
 * </UL>
 * </p>
 *
 * <p>Because keyspace, table, and type elements are handled sometimes in different ways,
 * these are stored in separate data structures, mapped by the logical keyspace name. This means
 * that you will see table definitions for named keyspaces even if those named keyspaces are not represented
 * in the keyspace definitions. This allows for sub-selecting of rendered elements by logical
 * name without requiring a fully-interconnected keyspace->table->column object graph.
 * </p>
 */
public class CqlModel implements CqlModelInfo {
    private final static Logger logger = LogManager.getLogger(CqlModel.class);

    private final Supplier<List<String>> errors;
    private final Map<String, CqlKeyspace> keyspaceDefs = new LinkedHashMap<>();
    private final Map<String, Map<String, CqlTable>> tableDefs = new LinkedHashMap<>();
    private final Map<String, Map<String, CqlType>> typeDefs = new LinkedHashMap<>();

    private CGSchemaStats schemaStats = null;
    private ComputedSchemaStats computedSchemaStats;

    @Override
    public CGSchemaStats getStats() {
        return schemaStats;
    }

    @Override
    public boolean hasStats() {
        return schemaStats!=null;
    }

    public ComputedSchemaStats getComputedStats() {
        return computedSchemaStats;
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
                if (modelTables != null) {
                    CqlTable modelTable = modelTables.get(statsTableName);
                    if (modelTable != null) {
                        logger.debug("setting table stats for '" + statsKeyspacename + "." + statsTableName + "'");
                        modelTable.setTableAttributes(tableStats);
                    } else {
                        logger.debug("       skipping table stats for '" + statsKeyspacename + "." + statsTableName + "'");
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


    public CqlModel(Supplier<List<String>> errorSource) {
        this.errors = errorSource;
    }

    public List<String> getErrors() {
        return errors.get();
    }

    public void newKeyspace() {
        keyspace = new CqlKeyspace();
    }

    public void saveKeyspace(String text, String refddl) {
        keyspace.setKeyspaceName(text);
        this.keyspaceDefs.put(text, keyspace);
        keyspace = null;
    }

    public void newTable() {
        table = new CqlTable();
    }

    public void saveTable(String keyspace, String text) {
        table.setKeyspace(keyspace);
        table.setName(text);
        this.tableDefs.computeIfAbsent(keyspace, ks -> new LinkedHashMap<>()).put(text, table);
        table = null;
    }

    public void saveColumnDefinition(String colname, String typedef, boolean isPrimaryKey, int position) {
        this.table.addcolumnDef(colname, typedef, position);
        if (isPrimaryKey) {
            this.table.addPartitionKey(colname);
        }
    }

    @Override
    public Map<String, CqlKeyspace> getKeyspacesByName() {
        return keyspaceDefs;
    }

    @Override
    public List<CqlKeyspace> getKeyspaceDefs() {
        return new ArrayList<>(this.keyspaceDefs.values());
    }

    @Override
    public Map<String, Map<String, CqlTable>> getTableDefsByKeyspaceThenTable() {
        return tableDefs;
    }

    @Override
    public List<CqlTable> getTablesForKeyspace(String ksname) {
        Map<String, CqlTable> tables = this.tableDefs.get(ksname);
        if (tables != null) {
            return new ArrayList<>(tables.values());
        }
        return List.of();
    }

    @Override
    public List<CqlTable> getTableDefs() {
        return tableDefs.values().stream().flatMap(m -> m.values().stream()).toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String ks : keyspaceDefs.keySet()) {
            CqlKeyspace keyspace = keyspaceDefs.get(ks);
            sb.append("keyspace '").append(keyspace.getName()).append("':\n");
            sb.append(keyspace).append("\n");

            tableDefs.getOrDefault(ks, Map.of()).values().stream()
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
     *
     * @return A list of all known keyspace names
     */
    @Override
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
        Map<String, CqlType> ksTypes = this.typeDefs.computeIfAbsent(keyspace, ks -> new LinkedHashMap<>());
        ksTypes.put(udt.getName(), udt);
        udt = null;
    }

    @Override
    public List<CqlType> getTypeDefs() {
        ArrayList<CqlType> list = new ArrayList<>();
        for (Map<String, CqlType> cqlTypesByKeyspace : typeDefs.values()) {
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
        this.typeDefs.remove(name);
    }

    @Override
    public String getSummaryLine() {
        return "keyspaces: " + keyspaceDefs.size() + ", tables: " + getTableDefs().size() +
            ", columns: " + getTableDefs().stream().mapToInt(t -> t.getColumnDefinitions().size()).sum() +
            ", types: " + getTypeDefs().size();
    }

    public void renamekeyspace(String keyspaceName, String newKeyspaceName) {
        if (this.keyspaceDefs.containsKey(keyspaceName)) {
            CqlKeyspace keyspace = this.keyspaceDefs.remove(keyspaceName);
            keyspace.setKeyspaceName(newKeyspaceName);
            this.keyspaceDefs.put(newKeyspaceName, keyspace);
        }
        if (this.tableDefs.containsKey(keyspaceName)) {
            Map<String, CqlTable> tablesForKeyspace = this.tableDefs.remove(keyspaceName);
            if (tablesForKeyspace != null) {
                for (CqlTable table : tablesForKeyspace.values()) {
                    table.setKeyspace(newKeyspaceName);
                }
            }
            this.tableDefs.put(newKeyspaceName, tablesForKeyspace);
        }
        if (this.typeDefs.containsKey(keyspaceName)) {
            Map<String, CqlType> typesForKeyspace = this.typeDefs.remove(keyspaceName);
            if (typesForKeyspace != null) {
                for (CqlType cqltype : typesForKeyspace.values()) {
                    cqltype.setKeyspace(newKeyspaceName);
                }
            }
            this.typeDefs.put(newKeyspaceName, typesForKeyspace);
        }
    }

    public void renameTable(CqlTable extant, String newTableName) {
        Map<String, CqlTable> tablesInKs = tableDefs.get(extant.getKeySpace());
        CqlTable table = tablesInKs.get(extant.getName());
        table.setName(newTableName);
        tablesInKs.put(table.getName(), table);
    }

    public void renameType(String keyspaceName, String typeName, String newTypeName) {
        Map<String, CqlType> typesInKeyspace = typeDefs.get(keyspaceName);
        CqlType cqlType = typesInKeyspace.remove(typeName);
        cqlType.setName(newTypeName);
        typesInKeyspace.put(newTypeName, cqlType);
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

    @Override
    public Map<String, Map<String, CqlType>> getTypesByKeyspaceThenName() {
        return typeDefs;
    }

    public void addClusteringOrder(String colname, String order) {
        table.addTableClusteringOrder(colname, order);
    }

    public boolean isEmpty() {
        return this.keyspaceDefs.size() == 0 && this.tableDefs.size() == 0 && this.typeDefs.size() == 0;
    }
}
