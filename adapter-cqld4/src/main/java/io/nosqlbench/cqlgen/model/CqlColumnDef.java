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

import java.util.Map;

public class CqlColumnDef implements NBNamedElement, Labeled {
    private CqlTable table;
    private String keyspace;
    private String name;
    private String type;
    private final int position;

    public CqlColumnDef(CqlTable table, int position, String colname, String typedef) {
        this.table = table;
        this.position = position;
        this.type = typedef;
        this.name = colname;
    }

    public void setTypeDef(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTrimmedTypedef() {
        return type.replaceAll(" ", "");
    }

    public String getTableName() {
        return table.getName();
    }

    public String getKeyspace() {
        return keyspace;
    }

    @Override
    public String toString() {
        return getLabels().toString();
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "name", name,
            "typedef", type,
            "table", table.getName(),
            "keyspace", keyspace,
            "type", "column"
        );
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public void setTable(CqlTable table) {
        this.table = table;
    }

    public boolean isCounter() {
        return getTrimmedTypedef().equalsIgnoreCase("counter");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSyntax() {
        return getName() + " " + getTrimmedTypedef();
    }

    public boolean isPartitionKey() {
        return table.isPartitionKey(position);
    }

    public boolean isLastPartitionKey() {
        return table.isLastPartitionKey(position);
    }

    public boolean isClusteringColumn() {
        return table.isClusteringColumn(position);
    }

    public boolean isLastClusteringColumn() {
        return table.isLastClusteringColumn(position);
    }

    public CqlTable getTable() {
        return this.table;
    }

    public String getFullName() {
        return getKeyspace() + "." + getTable().getName() + "." + getName() + "(column)";
    }
}
