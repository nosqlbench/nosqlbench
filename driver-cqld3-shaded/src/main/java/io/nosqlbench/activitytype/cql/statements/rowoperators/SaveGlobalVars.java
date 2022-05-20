package io.nosqlbench.activitytype.cql.statements.rowoperators;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import io.nosqlbench.activitytype.cql.api.RowCycleOperator;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the current row into the global object map. Key names are set from the field names. Null values are stored
 * as empty strings.
 */
public class SaveGlobalVars implements RowCycleOperator {

    ConcurrentHashMap<String, Object> gl_vars = SharedState.gl_ObjectMap;

    @Override
    public int apply(Row row, long cycle) {
        List<ColumnDefinitions.Definition> cdlist = row.getColumnDefinitions().asList();
        for (ColumnDefinitions.Definition definition : cdlist) {
            String name = definition.getName();
            Object object = row.getObject(name);
            if (object == null){
                object = "";
            }
            gl_vars.put(name,object);
        }
        return 0;
    }

}
