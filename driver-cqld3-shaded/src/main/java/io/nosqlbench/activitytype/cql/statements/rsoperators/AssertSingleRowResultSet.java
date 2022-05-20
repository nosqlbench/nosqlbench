package io.nosqlbench.activitytype.cql.statements.rsoperators;

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


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import io.nosqlbench.activitytype.cql.api.ResultSetCycleOperator;
import io.nosqlbench.activitytype.cql.errorhandling.exceptions.ResultSetVerificationException;

/**
 * Throws a {@link ResultSetVerificationException} unless there is exactly one row in the result set.
 */
public class AssertSingleRowResultSet implements ResultSetCycleOperator {

    @Override
    public int apply(ResultSet resultSet, Statement statement, long cycle) {
        int rowsIncoming = resultSet.getAvailableWithoutFetching();
        if (rowsIncoming<1) {
            throw new ResultSetVerificationException(cycle, resultSet, statement, "no row in result set, expected exactly 1");
        }
        if (rowsIncoming>1) {
            throw new ResultSetVerificationException(cycle, resultSet, statement, "more than one row in result set, expected exactly 1");
        }
        return rowsIncoming;
    }

}
