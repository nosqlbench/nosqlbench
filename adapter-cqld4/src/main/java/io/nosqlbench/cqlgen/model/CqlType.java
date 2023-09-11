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

import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.api.config.NBNamedElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CqlType implements NBNamedElement, NBLabeledElement {

    private String name;
    private CqlKeyspaceDef keyspace;
    private List<CqlTypeColumn> columnDefs = new ArrayList<>();
    private volatile boolean defined;

    public void setKeyspace(CqlKeyspaceDef keyspace) {
        this.keyspace = keyspace;
    }
    public void setName(String name) {
        this.name = name;
    }

    public CqlKeyspaceDef getKeyspace() {
        return keyspace;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void addColumn(CqlTypeColumn def) {
        this.columnDefs.add(this.columnDefs.size(),def);
        def.setPosition(ColumnPosition.TypeDef);
    }

    public List<CqlTypeColumn> columns() {
        return columnDefs;
    }

    @Override
    public NBLabels getLabels() {
        return NBLabels.forKV(
            "keyspace", keyspace.getName(),
            "type","type",
            "name", name
        );
    }

    public void setColumnDefs(List<CqlTypeColumn> columnDefs) {
        this.columnDefs = columnDefs;
    }

    public List<CqlTypeColumn> getColumnDefs() {
        return columnDefs;
    }

    public String getFullName() {
        return keyspace.getName()+ '.' + this.name;
    }

    public void getReferenceErrors(List<String> errors) {
        if (!defined) {
            errors.add("type " + this.name + " was referenced but not defined.");
        }
    }

    public void validate() {
        Objects.requireNonNull(this.name);
        Objects.requireNonNull(this.keyspace);
    }

    public void setDefined() {
        this.defined =true;
    }
}
