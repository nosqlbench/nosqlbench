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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CqlType implements NBNamedElement, NBLabeledElement {

    private String name;
    private CqlKeyspaceDef keyspace;
    private List<CqlTypeColumn> columnDefs = new ArrayList<>();
    private volatile boolean defined;

    public void setKeyspace(final CqlKeyspaceDef keyspace) {
        this.keyspace = keyspace;
    }
    public void setName(final String name) {
        this.name = name;
    }

    public CqlKeyspaceDef getKeyspace() {
        return this.keyspace;
    }

    @Override
    public String getName() {
        return name;
    }

    public void addColumn(final CqlTypeColumn def) {
        columnDefs.add(columnDefs.size(),def);
        def.setPosition(ColumnPosition.TypeDef);
    }

    public List<CqlTypeColumn> columns() {
        return this.columnDefs;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "keyspace", this.keyspace.getName(),
            "type","type",
            "name", this.name
        );
    }

    public void setColumnDefs(final List<CqlTypeColumn> columnDefs) {
        this.columnDefs = columnDefs;
    }

    public List<CqlTypeColumn> getColumnDefs() {
        return this.columnDefs;
    }

    public String getFullName() {
        return this.keyspace.getName()+ '.' + name;
    }

    public void getReferenceErrors(final List<String> errors) {
        if (!this.defined) errors.add("type " + name + " was referenced but not defined.");
    }

    public void validate() {
        Objects.requireNonNull(name);
        Objects.requireNonNull(keyspace);
    }

    public void setDefined() {
        defined=true;
    }
}
