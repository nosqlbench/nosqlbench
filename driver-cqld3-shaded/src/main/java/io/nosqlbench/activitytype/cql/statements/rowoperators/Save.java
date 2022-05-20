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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Save specific variables to the thread local object map
 */
public class Save implements RowCycleOperator {
    private final static Logger logger = LogManager.getLogger(Save.class);

    ThreadLocal<HashMap<String, Object>> tl_objectMap = SharedState.tl_ObjectMap;

    private final String[] varnames;

    public Save(String... varnames) {
        this.varnames = varnames;
    }

    @Override
    public int apply(Row row, long cycle) {
        try {
            HashMap<String, Object> tlvars= tl_objectMap.get();
            for (String varname : varnames) {
                Object object = row.getObject(varname);
                tlvars.put(varname,object);
            }
        } catch (Exception e) {
            List<ColumnDefinitions.Definition> definitions = row.getColumnDefinitions().asList();
            logger.error("Unable to save '" + Arrays.toString(varnames) + "' from " +
                    definitions.stream().map(ColumnDefinitions.Definition::getName)
            .collect(Collectors.joining(",","[","]")) + ": ",e);
            throw e;
        }
        return 0;
    }

}
