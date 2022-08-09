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

import java.util.HashMap;
import java.util.Map;

public class CqlTableColumn extends CqlColumnBase {

    private CqlTable table;

    public CqlTableColumn(String colname, String typedef, CqlTable table) {
        super(colname, typedef);
        setTable(table);
    }

    @Override
    protected String getParentFullName() {
        return table.getFullName();
    }

    public CqlTable getTable() {
        return table;
    }

    public void setTable(CqlTable table) {
        this.table = table;
    }

    @Override
    public Map<String, String> getLabels() {
        HashMap<String, String> map = new HashMap<>(super.getLabels());
        map.put("table",getTable().getName());
        return map;
    }
}
