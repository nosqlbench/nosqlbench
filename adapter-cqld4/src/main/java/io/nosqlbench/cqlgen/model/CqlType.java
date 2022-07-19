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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class CqlType implements NBNamedElement, Labeled {
    private String keyspace;
    private String name;
    private String refddl;
    private Map<String,String> fields = new LinkedHashMap<>();

    public void setKeyspace(String newksname) {
        this.keyspace = newksname;
        if (refddl!=null) {
            this.refddl = this.refddl.replaceAll(this.keyspace,newksname);
        }
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getName() {
        return this.name;
    }

    public void addField(String name, String typedef) {
        this.fields.put(name, typedef);
    }

    public Map<String, String> getFields() {
        return fields;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of(
            "keyspace", this.keyspace,
            "type","udt",
            "name",name
        );
    }

    public void renameColumns(Function<String, String> renamer) {
        Map<String,String> newColumns = new LinkedHashMap<>();
        fields.forEach((k,v)->newColumns.put(renamer.apply(k),v));
        this.fields = newColumns;
    }
}
