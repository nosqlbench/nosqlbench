package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

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

public class ResultSetVerificationException extends CQLResultSetException {

    public ResultSetVerificationException(
            long cycle, ResultSet resultSet, Statement statement, Throwable cause) {
        super(cycle, resultSet, statement, cause);
    }

    public ResultSetVerificationException(
            long cycle, ResultSet resultSet, Statement statement, String s) {
        super(cycle, resultSet, statement, s + ", \nquery string:\n" + getQueryString(statement));
    }
}
