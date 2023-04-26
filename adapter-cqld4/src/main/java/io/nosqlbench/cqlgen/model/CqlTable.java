/*
 * Copyright (c) 2022-2023 nosqlbench
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
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.cqlgen.core.CGTableStats;
import io.nosqlbench.cqlgen.transformers.ComputedTableStats;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CqlTable implements NBNamedElement, NBLabeledElement {
    private CqlKeyspaceDef keyspace;
    String name = "";
    CGTableStats tableAttributes;
    int[] partitioning = new int[0];
    int[] clustering = new int[0];
    List<String> clusteringOrders = new ArrayList<>();
    List<CqlTableColumn> coldefs = new ArrayList<>();
    private boolean compactStorage;
    private ComputedTableStats computedTableStats;

    public CqlTable() {
    }

    public boolean isCompactStorage() {
        return this.compactStorage;
    }

    public CGTableStats getTableAttributes() {
        return this.tableAttributes;
    }

    public void setStats(final CGTableStats tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    public void addcolumnDef(final CqlTableColumn cqlField) {
        cqlField.setTable(this);
        coldefs.add(cqlField);
    }

    public void setName(final String tableName) {
        name = tableName;
    }

    @Override
    public String toString() {
        return "cql table: '" + name + "':\n"
            + coldefs.stream()
            .map(Object::toString)
            .map(s -> "   " + s)
            .collect(Collectors.joining("\n"));
    }

    public List<CqlTableColumn> getColumnDefs() {
        return coldefs;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setKeyspace(final CqlKeyspaceDef keyspace) {
        this.keyspace = keyspace;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "keyspace", keyspace.getName(),
            "name", name,
            "type", "table"
        );
    }

    public void addPartitionKey(final String pkey) {
        int[] new_partitioning = this.partitioning;
        for (int idx = 0; idx < this.coldefs.size(); idx++)
            if (this.coldefs.get(idx).getName().equals(pkey)) {
                this.coldefs.get(idx).setPosition(ColumnPosition.Partitioning);
                new_partitioning = new int[this.partitioning.length + 1];
                System.arraycopy(this.partitioning, 0, new_partitioning, 0, this.partitioning.length);
                new_partitioning[new_partitioning.length - 1] = idx;
                break;
            }
        if (new_partitioning== this.partitioning)
            throw new RuntimeException("Unable to assign partition key '" + pkey + "' to a known column of the same name.");
        partitioning = new_partitioning;

    }

    public void addClusteringColumn(final String ccol) {
        int[] new_clustering = this.clustering;

        for (int i = 0; i < this.coldefs.size(); i++)
            if (this.coldefs.get(i).getName().equals(ccol)) {
                this.coldefs.get(i).setPosition(ColumnPosition.Clustering);
                new_clustering = new int[this.clustering.length + 1];
                System.arraycopy(this.clustering, 0, new_clustering, 0, this.clustering.length);
                new_clustering[new_clustering.length - 1] = i;
                break;
            }
        if (new_clustering == this.clustering)
            throw new RuntimeException("Unable to assign clustering field '" + ccol + " to a known column of the same name.");
        clustering = new_clustering;
    }

    public void addTableClusteringOrder(final String colname, final String order) {
        this.clusteringOrders.add(order);
    }

    public List<String> getClusteringOrders() {
        return this.clusteringOrders;
    }


    public List<String> getPartitionKeys() {
        return Arrays.stream(this.partitioning).mapToObj(i -> coldefs.get(i).getName()).toList();
    }

    public List<String> getClusteringColumns() {
        return Arrays.stream(this.clustering).mapToObj(i -> coldefs.get(i).getName()).toList();
    }

    public CqlTableColumn getColumnDefForName(final String colname) {
        final Optional<CqlTableColumn> def = this.coldefs
            .stream()
            .filter(c -> c.getName().equalsIgnoreCase(colname))
            .findFirst();
        if (!def.isPresent()) throw new RuntimeException("Unable to find column definition in table '" +
            name + "' for column '" + colname + '\'');
        return def.orElseThrow();
    }

    public void renameColumns(final Function<String, String> renamer) {
        for (final CqlTableColumn coldef : this.coldefs) coldef.setName(renamer.apply(coldef.getName()));
    }

    public List<CqlTableColumn> getNonKeyColumnDefinitions() {
        int last = this.partitioning[this.partitioning.length - 1];
        last = (0 < clustering.length) ? this.clustering[this.clustering.length - 1] : last;
        final List<CqlTableColumn> nonkeys = new ArrayList<>();
        for (int nonkey = last; nonkey < this.coldefs.size(); nonkey++) nonkeys.add(this.coldefs.get(nonkey));
        return nonkeys;
    }

    public void setCompactStorage(final boolean isCompactStorage) {
        compactStorage = isCompactStorage;
    }

    public String getFullName() {
        return (null != this.keyspace ? keyspace.getName() + '.' : "") + name;
    }

    public boolean isPartitionKey(final int position) {
        return position < this.partitioning.length;
    }

    public boolean isLastPartitionKey(final int position) {
        return position == (this.partitioning.length - 1);
    }

    public boolean isClusteringColumn(final int position) {
        return (0 < clustering.length) && (position < this.clustering[this.clustering.length - 1]) && (position >= this.clustering[0]);
    }

    public boolean isLastClusteringColumn(final int position) {
        return (0 < clustering.length) && (position == this.clustering[this.clustering.length - 1]);
    }

    public ComputedTableStats getComputedStats() {
        return computedTableStats;
    }

    public void setComputedStats(final ComputedTableStats stats) {
        computedTableStats = stats;
    }

    public boolean hasStats() {
        return null != this.computedTableStats;
    }

    public CqlKeyspaceDef getKeyspace() {
        return keyspace;
    }

    public void getReferenceErrors(final List<String> errors) {
    }
}
