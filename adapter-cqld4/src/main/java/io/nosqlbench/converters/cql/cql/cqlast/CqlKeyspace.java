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

package io.nosqlbench.converters.cql.cql.cqlast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CqlKeyspace {
    String keyspaceName= "";
    List<CqlTable> tables = new ArrayList<>();
    CqlTable lastAddedTable = null;

    public CqlKeyspace() {
    }

    public void setKeyspaceName(String name) {
        this.keyspaceName=name;
    }

    public void addTable() {
        lastAddedTable = new CqlTable();
        tables.add(lastAddedTable);
    }

    public void addColumnDef(String type, String name) {
        lastAddedTable.addcolumnDef(new CqlField(type, name));
    }

    public void setTableName(String tableName) {
        lastAddedTable.setName(tableName);
    }

    public void addTableColumn(String type, String fieldName) {
        lastAddedTable.addcolumnDef(type,fieldName);
    }

    @Override
    public String toString() {
        return "keyspace:" + keyspaceName+"\n"+
            " tables:\n"+
            (tables.stream().map(Object::toString)
                .map(s -> "  "+s)
                .collect(Collectors.joining("\n")));
    }
}
