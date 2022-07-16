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

import java.util.Map;

public class CqlColumnDef implements NBNamedElement, Labeled {
    private String refDefinitionDdl;
    private String refTypeDefddl;
    private String table;
    private String keyspace;
    private String name;
    private String type;
    private int position;
    private ColType coltype;

    public CqlColumnDef(String colname, String typedef, String refColumnDdl) {
        this.type = typedef;
        this.name = colname;
        this.refDefinitionDdl = refColumnDdl;
    }

    public String getDefinitionDdl() {
        return refDefinitionDdl;
    }

    public void setTypedfRefDdl(String textOfTypeDefOnly) {
        this.refTypeDefddl = textOfTypeDefOnly;
    }
    public void setDefinitionRefDdl(String textOfWholeDefinition) {
        this.refDefinitionDdl = textOfWholeDefinition;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTrimmedTypedef() {
        return type.replaceAll(" ","");
    }

    public String getTable() {
        return table;
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
            "column", name,
            "typedef", type,
            "table", table,
            "keyspace", keyspace
        );
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public boolean isCounter() {
        return getTrimmedTypedef().equalsIgnoreCase("counter");
    }

    public void setName(String name) {
        this.name = name;
    }
}
