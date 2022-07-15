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

import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.labels.Labeled;

import java.util.*;
import java.util.stream.Collectors;

public class CqlTable implements NBNamedElement, Labeled {
    String name = "";
    String keyspace = "";
    List<CqlColumnDef> coldefs = new ArrayList<>();
    Map<String, String> tableAttributes = new HashMap<String, String>();
    List<String> partitionKeys = new ArrayList<>();
    List<String> clusteringColumns = new ArrayList<>();
    private String refddl;

    public CqlTable() {
    }

    public Map<String, String> getTableAttributes() {
        return tableAttributes;
    }

    public void setTableAttributes(Map<String, String> tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    public void addcolumnDef(CqlColumnDef cqlField) {
        this.coldefs.add(cqlField);
    }

    public void setName(String tableName) {
        this.name = tableName;
        for (CqlColumnDef coldef : coldefs) {
            coldef.setTable(tableName);
        }
    }

    public void addcolumnDef(String colname, String typedef, String refColumnDdl) {
        coldefs.add(new CqlColumnDef(colname, typedef, refColumnDdl));
    }

    @Override
    public String toString() {
        return "cql table: '" + this.name + "':\n"
            + this.coldefs.stream()
            .map(Object::toString)
            .map(s -> "   " + s)
            .collect(Collectors.joining("\n"));
    }

    public List<CqlColumnDef> getColumnDefinitions() {
        return this.coldefs;
    }

    public String getName() {
        return this.name;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
        for (CqlColumnDef coldef : coldefs) {
            coldef.setKeyspace(keyspace);
        }
    }

    public String getRefDdl() {
        return this.refddl;
    }

    public void setRefDdl(String refddl) {
        this.refddl = refddl;
    }

    public String getRefddl() {
        return refddl;
    }


    public String getKeySpace() {
        return this.keyspace;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "keyspace", this.keyspace,
            "table", this.name
        );
    }

    public void addPartitionKey(String pkey) {
        this.partitionKeys.add(pkey);
    }

    public void addClusteringColumn(String ccol) {
        this.clusteringColumns.add(ccol);
    }

    public List<String> getPartitionKeys() {
        return this.partitionKeys;
    }

    public List<String> getClusteringColumns() {
        return this.clusteringColumns;
    }

    public CqlColumnDef getColumnDefForName(String colname) {
        Optional<CqlColumnDef> def = coldefs
            .stream()
            .filter(c -> c.getName().equalsIgnoreCase(colname))
            .findFirst();
        if (!def.isPresent()) {
            throw new RuntimeException("Unable to find column definition in table '" +
                this.getName() + "' for column '" + colname + "'");
        }
        return def.orElseThrow();
    }

    public List<CqlColumnDef> getNonKeyColumnDefinitions() {
        return coldefs.stream()
            .filter(n -> !partitionKeys.contains(n.getName()))
            .filter(n -> !clusteringColumns.contains(n.getName()))
            .toList();
    }

}
