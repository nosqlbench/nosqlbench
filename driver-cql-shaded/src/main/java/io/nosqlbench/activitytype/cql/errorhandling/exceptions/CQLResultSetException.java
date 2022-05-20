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


import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public abstract class CQLResultSetException extends CqlGenericCycleException {

    private final Statement statement;
    private final ResultSet resultSet;

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement, String message, Throwable cause) {
        super(cycle,message,cause);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement) {
        super(cycle);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement, String message) {
        super(cycle,message);
        this.resultSet = resultSet;
        this.statement=statement;
    }

    public CQLResultSetException(long cycle, ResultSet resultSet, Statement statement, Throwable cause) {
        super(cycle,cause);
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public Statement getStatement() {
        return statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    protected static String getQueryString(Statement stmt) {
        if (stmt instanceof BoundStatement) {
            return ((BoundStatement)stmt).preparedStatement().getQueryString();
        } else if (stmt instanceof SimpleStatement) {
            return ((SimpleStatement) stmt).getQueryString();
        } else {
            return "UNKNOWN Statement type:" + stmt.getClass().getSimpleName();
        }
    }


}
