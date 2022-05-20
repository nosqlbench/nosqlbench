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


import io.nosqlbench.activitytype.cql.api.ResultSetCycleOperator;

public enum ResultSetCycleOperators {

    pushvars(PushVars.class),
    popvars(PopVars.class),
    clearvars(ClearVars.class),

    trace(TraceLogger.class),
    log(CqlResultSetLogger.class),
    assert_singlerow(AssertSingleRowResultSet.class),

    print(Print.class);

    private final Class<? extends ResultSetCycleOperator> implClass;

    ResultSetCycleOperators(Class<? extends ResultSetCycleOperator> traceLoggerClass) {
        this.implClass = traceLoggerClass;
    }


    public Class<? extends ResultSetCycleOperator> getImplementation() {
        return implClass;
    }

    public ResultSetCycleOperator getInstance() {
        try {
            return getImplementation().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultSetCycleOperator newOperator(String name) {
        return ResultSetCycleOperators.valueOf(name).getInstance();
    }

}
