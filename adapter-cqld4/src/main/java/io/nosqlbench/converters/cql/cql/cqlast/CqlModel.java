package io.nosqlbench.converters.cql.cql.cqlast;

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
