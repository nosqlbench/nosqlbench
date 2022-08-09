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

public abstract class CqlColumnBase implements NBNamedElement, Labeled {

    private String name;
    private String typedef;
    private ColumnPosition position=ColumnPosition.NonKey;

    protected CqlColumnBase(String colname, String typedef) {
        this.typedef = typedef;
        this.name = colname;
    }

    public void setPosition(ColumnPosition position) {
        this.position = position;
    }

    public ColumnPosition getPosition() {
        return this.position;
    }
    public void setTypeDef(String type) {
        this.typedef = type;
    }

    public String getName() {
        return name;
    }

    public String getTypedef() {
        return typedef;
    }

    public String getTrimmedTypedef() {
        return typedef.replaceAll(" ", "");
    }

    @Override
    public String toString() {
        return getLabels().toString();
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "name", name,
            "type", "column"
        );
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

    public String getFullName() {
        return getParentFullName() + "." + getName();
    }

    protected abstract String getParentFullName();

}
