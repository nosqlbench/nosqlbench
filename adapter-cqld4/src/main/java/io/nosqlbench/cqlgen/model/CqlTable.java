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

import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.labels.Labeled;
import io.nosqlbench.cqlgen.core.CGTableStats;
import io.nosqlbench.cqlgen.transformers.ComputedTableStats;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CqlTable implements NBNamedElement, Labeled {
    private CqlKeyspaceDef keyspace;
    String name = "";
    CGTableStats tableAttributes = null;
    int[] partitioning = new int[0];
    int[] clustering = new int[0];
    List<String> clusteringOrders = new ArrayList<>();
    List<CqlTableColumn> coldefs = new ArrayList<>();
    private boolean compactStorage;
    private ComputedTableStats computedTableStats;

    public CqlTable() {
    }

    public boolean isCompactStorage() {
        return compactStorage;
    }

    public CGTableStats getTableAttributes() {
        return tableAttributes;
    }

    public void setStats(CGTableStats tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    public void addcolumnDef(CqlTableColumn cqlField) {
        cqlField.setTable(this);
        this.coldefs.add(cqlField);
    }

    public void setName(String tableName) {
        this.name = tableName;
    }

    @Override
    public String toString() {
        return "cql table: '" + this.name + "':\n"
            + this.coldefs.stream()
            .map(Object::toString)
            .map(s -> "   " + s)
            .collect(Collectors.joining("\n"));
    }

    public List<CqlTableColumn> getColumnDefs() {
        return this.coldefs;
    }

    public String getName() {
        return this.name;
    }

    public void setKeyspace(CqlKeyspaceDef keyspace) {
        this.keyspace = keyspace;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "keyspace", this.keyspace.getName(),
            "name", this.name,
            "type", "table"
        );
    }

    public void addPartitionKey(String pkey) {
        int[] new_partitioning = partitioning;
        for (int idx = 0; idx < coldefs.size(); idx++) {
            if (coldefs.get(idx).getName().equals(pkey)) {
                coldefs.get(idx).setPosition(ColumnPosition.Partitioning);
                new_partitioning = new int[partitioning.length + 1];
                System.arraycopy(partitioning, 0, new_partitioning, 0, partitioning.length);
                new_partitioning[new_partitioning.length - 1] = idx;
                break;
            }
        }
        if (new_partitioning==partitioning) {
            throw new RuntimeException("Unable to assign partition key '" + pkey + "' to a known column of the same name.");
        } else {
            this.partitioning = new_partitioning;
        }

    }

    public void addClusteringColumn(String ccol) {
        int[] new_clustering = clustering;

        for (int i = 0; i < coldefs.size(); i++) {
            if (coldefs.get(i).getName().equals(ccol)) {
                coldefs.get(i).setPosition(ColumnPosition.Clustering);
                new_clustering= new int[clustering.length + 1];
                System.arraycopy(clustering, 0, new_clustering, 0, clustering.length);
                new_clustering[new_clustering.length - 1] = i;
                break;
            }
        }
        if (new_clustering == clustering) {
            throw new RuntimeException("Unable to assign clustering field '" + ccol + " to a known column of the same name.");
        } else {
            this.clustering = new_clustering;
        }
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

    public CqlTableColumn getColumnDefForName(String colname) {
        Optional<CqlTableColumn> def = coldefs
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
        for (CqlTableColumn coldef : coldefs) {
            coldef.setName(renamer.apply(coldef.getName()));
        }
    }

    public List<CqlTableColumn> getNonKeyColumnDefinitions() {
        int last = partitioning[partitioning.length - 1];
        last = (clustering.length > 0 ? clustering[clustering.length - 1] : last);
        List<CqlTableColumn> nonkeys = new ArrayList<>();
        for (int nonkey = last; nonkey < coldefs.size(); nonkey++) {
            nonkeys.add(coldefs.get(nonkey));
        }
        return nonkeys;
    }

    public void setCompactStorage(boolean isCompactStorage) {
        this.compactStorage = isCompactStorage;
    }

    public String getFullName() {
        return (this.keyspace != null ? this.keyspace.getName() + "." : "") + this.name;
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

    public ComputedTableStats getComputedStats() {
        return this.computedTableStats;
    }

    public void setComputedStats(ComputedTableStats stats) {
        this.computedTableStats = stats;
    }

    public boolean hasStats() {
        return this.computedTableStats!=null;
    }

    public CqlKeyspaceDef getKeyspace() {
        return this.keyspace;
    }

    public void getReferenceErrors(List<String> errors) {
    }
}
