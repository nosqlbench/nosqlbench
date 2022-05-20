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


import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Statement;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;

/**
 * This is now the main binder again, but if there are any exceptions, it delegates to the diagnostic
 * one in order to explain what happened. This is to allow for higher performance in the general
 * case, but with better user support when something goes wrong.
 *
 * If you want to force the client to use the array passing method of initializing a statement,
 * use this one, known as 'directarray'. This does give up the benefit of allowing unset values
 * to be modeled, and at no clear benefit. Thus the {@link CqlBinderTypes#unset_aware} one
 * will become the default.
 */
public class DirectArrayValuesBinder implements ValuesArrayBinder<PreparedStatement, Statement> {
    public final static Logger logger = LogManager.getLogger(DirectArrayValuesBinder.class);

    @Override
    public Statement bindValues(PreparedStatement preparedStatement, Object[] objects) {
        try {
            return preparedStatement.bind(objects);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error binding objects to prepared statement directly, falling back to diagnostic binding layer:");
            sb.append(Arrays.toString(objects));
            logger.warn(sb.toString(),e);
            DiagnosticPreparedBinder diag = new DiagnosticPreparedBinder();
            return diag.bindValues(preparedStatement, objects);
        }
    }
}
