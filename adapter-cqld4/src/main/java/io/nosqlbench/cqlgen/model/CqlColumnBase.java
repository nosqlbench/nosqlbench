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

import java.util.Map;

public abstract class CqlColumnBase implements NBNamedElement, NBLabeledElement {

    private String name;
    private String typedef;
    private ColumnPosition position=ColumnPosition.NonKey;

    protected CqlColumnBase(final String colname, final String typedef) {
        this.typedef = typedef;
        name = colname;
    }

    public void setPosition(final ColumnPosition position) {
        this.position = position;
    }

    public ColumnPosition getPosition() {
        return position;
    }
    public void setTypeDef(final String type) {
        typedef = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getTypedef() {
        return this.typedef;
    }

    public String getTrimmedTypedef() {
        return this.typedef.replaceAll(" ", "");
    }

    @Override
    public String toString() {
        return this.getLabels().toString();
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "name", this.name,
            "type", "column"
        );
    }

    public boolean isCounter() {
        return "counter".equalsIgnoreCase(getTrimmedTypedef());
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSyntax() {
        return name + ' ' + this.getTrimmedTypedef();
    }

    public String getFullName() {
        return this.getParentFullName() + '.' + name;
    }

    protected abstract String getParentFullName();

}
