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

import java.util.LinkedHashMap;
import java.util.Map;

public class CqlType {
    private String keyspace;
    private String name;
    private String refddl;
    private final Map<String,String> fields = new LinkedHashMap<>();

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setRefddl(String ddl) {
        this.refddl = ddl;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getName() {
        return this.name;
    }

    public void addField(String name, String typedef, String typedefRefDdl) {
        this.fields.put(name, typedef);
    }

    public Map<String, String> getFields() {
        return fields;
    }
}
