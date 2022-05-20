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


import io.nosqlbench.activitytype.cql.api.RowCycleOperator;

public enum RowCycleOperators {

    saverows(SaveThreadRows.class),
    savevars(SaveThreadVars.class),
    saveglobalvars(SaveGlobalVars.class),
    print(Print.class);

    private final Class<? extends RowCycleOperator> implClass;

    RowCycleOperators(Class<? extends RowCycleOperator> traceLoggerClass) {
        this.implClass = traceLoggerClass;
    }

    public Class<? extends RowCycleOperator> getImplementation() {
        return implClass;
    }

    public RowCycleOperator getInstance() {
        try {
            return getImplementation().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RowCycleOperator newOperator(String name) {
        return RowCycleOperators.valueOf(name).getInstance();
    }

}
