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
import io.nosqlbench.converters.cql.exporters.CGTableStats;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CqlTable implements NBNamedElement, Labeled {
    String name = "";
    String keyspace = "";
    CGTableStats tableAttributes = null;
    int[] partitioning = new int[0];
    int[] clustering = new int[0];
    List<String> clusteringOrders = new ArrayList<>();
    List<CqlColumnDef> coldefs = new ArrayList<>();
    private boolean compactStorage;

    public CqlTable() {
    }

    public boolean isCompactStorage() {
        return compactStorage;
    }

    public CGTableStats getTableAttributes() {
        return tableAttributes;
    }

    public void setTableAttributes(CGTableStats tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    public void addcolumnDef(CqlColumnDef cqlField) {
        this.coldefs.add(cqlField);
    }

    public void setName(String tableName) {
        this.name = tableName;
    }

    public void addcolumnDef(String colname, String typedef, int position) {
        coldefs.add(new CqlColumnDef(this, coldefs.size(), colname, typedef));
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

    public void setKeyspace(String newKsName) {
        for (CqlColumnDef coldef : coldefs) {
            coldef.setKeyspace(newKsName);
        }
        this.keyspace = newKsName;

    }

    public String getKeySpace() {
        return this.keyspace;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "keyspace", this.keyspace,
            "name", this.name,
            "type", "table"
        );
    }

    public void addPartitionKey(String pkey) {
        int[] newdefs = new int[partitioning.length + 1];
        System.arraycopy(partitioning, 0, newdefs, 0, partitioning.length);
        for (int i = 0; i < coldefs.size(); i++) {
            if (coldefs.get(i).getName().equals(pkey)) {
                newdefs[newdefs.length - 1] = i;
                break;
            }
        }
        this.partitioning = newdefs;
    }

    public void addClusteringColumn(String ccol) {
        int[] newdefs = new int[clustering.length + 1];
        System.arraycopy(clustering, 0, newdefs, 0, clustering.length);
        for (int i = 0; i < coldefs.size(); i++) {
            if (coldefs.get(i).getName().equals(ccol)) {
                newdefs[newdefs.length - 1] = i;
                break;
            }
        }
        this.clustering = newdefs;
    }

    public void addTableClusteringOrder(String colname, String order) {
        clusteringOrders.add(order);
    }

    public List<String> getClusteringOrders() {
        return clusteringOrders;
    }


    public List<String> getPartitionKeys() {
        return Arrays.stream(partitioning).mapToObj(i -> this.coldefs.get(i).getName()).toList();
    }

    public List<String> getClusteringColumns() {
        return Arrays.stream(clustering).mapToObj(i -> this.coldefs.get(i).getName()).toList();
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

    public void renameColumns(Function<String, String> renamer) {
        for (CqlColumnDef coldef : coldefs) {
            coldef.setName(renamer.apply(coldef.getName()));
        }
    }

    public List<CqlColumnDef> getNonKeyColumnDefinitions() {
        int last = partitioning[partitioning.length - 1];
        last = (clustering.length > 0 ? clustering[clustering.length - 1] : last);
        List<CqlColumnDef> nonkeys = new ArrayList<>();
        for (int nonkey = last; nonkey < coldefs.size(); nonkey++) {
            nonkeys.add(coldefs.get(nonkey));
        }
        return nonkeys;
    }

    public void setCompactStorage(boolean isCompactStorage) {
        this.compactStorage = isCompactStorage;
    }

    public String getFullName() {
        return (this.keyspace != null ? this.keyspace + "." : "") + this.name;
    }

    public boolean isPartitionKey(int position) {
        return position < partitioning.length;
    }

    public boolean isLastPartitionKey(int position) {
        return position == partitioning.length - 1;
    }

    public boolean isClusteringColumn(int position) {
        return clustering.length > 0 && position < clustering[clustering.length - 1] && position >= clustering[0];
    }

    public boolean isLastClusteringColumn(int position) {
        return clustering.length > 0 && position == clustering[clustering.length - 1];
    }
}
