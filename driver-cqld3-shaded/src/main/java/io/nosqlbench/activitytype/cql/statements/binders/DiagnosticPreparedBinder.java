package io.nosqlbench.activitytype.cql.statements.binders;

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


import com.datastax.driver.core.*;
import io.nosqlbench.activitytype.cql.core.CQLBindHelper;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

/**
 * This binder is not meant to be used primarily by default. It gives detailed
 * diagnostics, but in order to do so by default it does lots of processing.
 * Other binders will call to this one in an exception handler when needed in
 * order to explain in more detail what is happening for users.
 */
public class DiagnosticPreparedBinder implements ValuesArrayBinder<PreparedStatement, Statement> {
    public static final Logger logger = LogManager.getLogger(DiagnosticPreparedBinder.class);
    @Override
    public Statement bindValues(PreparedStatement prepared, Object[] values) {
        ColumnDefinitions columnDefinitions = prepared.getVariables();
        BoundStatement bound = prepared.bind();
        List<ColumnDefinitions.Definition> columnDefList;
        if (columnDefinitions.asList().size() == values.length) {
            columnDefList = columnDefinitions.asList();
        } else {
            throw new RuntimeException("The number of named anchors in your statement does not match the number of bindings provided.");
        }

        int i = 0;
        for (Object value : values) {
            if (columnDefList.size() <= i) {
                logger.error("what gives?");
            }
            ColumnDefinitions.Definition columnDef = columnDefList.get(i);
            String colName = columnDef.getName();
            DataType.Name type = columnDef.getType().getName();
            try {
                bound = CQLBindHelper.bindStatement(bound, colName, value, type);
            } catch (ClassCastException e) {
                logger.error(String.format("Unable to bind column %s to cql type %s with value %s", colName, type, value));
                throw e;
            }
            i++;
        }
        return bound;
    }
}
