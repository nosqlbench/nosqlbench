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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CqlTable {
    String name = "";
    String keyspace = "";
    List<CqlColumnDef> coldefs = new ArrayList();
    String refddl;

    public CqlTable() {
    }

    public void addcolumnDef(CqlColumnDef cqlField) {
        this.coldefs.add(cqlField);
    }

    public void setName(String tableName) {
        this.name = tableName;
    }

    public void addcolumnDef(String type, String fieldName) {
        coldefs.add(new CqlColumnDef(type, fieldName));
    }

    @Override
    public String toString() {
        return "cql table: '" + this.name + "':\n"
            + this.coldefs.stream()
            .map(Object::toString)
            .map(s -> "   " +s)
            .collect(Collectors.joining("\n"));
    }

    public List<CqlColumnDef> getColumnDefinitions() {
        return this.coldefs;
    }

    public String getTableName() {
        return this.name;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace=keyspace;
    }

    public String getRefDdl() {
        return this.refddl;
    }

    public void setRefDdl(String refddl) {
        this.refddl=refddl;
    }

    public String getRefddl() {
        return refddl;
    }


    public String getKeySpace() {
        return this.keyspace;
    }
}
