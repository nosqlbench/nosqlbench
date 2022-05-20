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


import io.nosqlbench.activitytype.cql.statements.core.ReadyCQLStatement;

/**
 * In internal exception type that is used to saverow exception
 * context from within a CQL activity cycle.
 */
public class CQLCycleWithStatementException extends Exception {

    private final long cycleValue;
    private final long durationNanos;
    private final ReadyCQLStatement readyCQLStatement;

    public CQLCycleWithStatementException(long cycleValue, long durationNanos, Throwable e, ReadyCQLStatement readyCQLStatement) {
        super(e);
        this.cycleValue = cycleValue;
        this.durationNanos = durationNanos;
        this.readyCQLStatement = readyCQLStatement;
    }

    public long getCycleValue() {
        return cycleValue;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public ReadyCQLStatement getReadyCQLStatement() {
        return readyCQLStatement;
    }

    public String getStatement() {
        return readyCQLStatement.getQueryString(cycleValue);
    }

}
