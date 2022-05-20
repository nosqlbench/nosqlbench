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


import com.datastax.driver.core.Row;

import java.util.Map;

/**
 * This exception is thrown when read verification fails.
 */
public class RowVerificationException extends CqlGenericCycleException {

    private Map<String, Object> expected;
    private Row row;

    public RowVerificationException(long cycle, Row row, Map<String, Object> expected, String detail) {
        super(cycle, detail);
        this.expected = expected;
        this.row = row;
    }

    @Override
    public String getMessage() {
        return "cycle:" + getCycle() + ": " + super.getMessage();
    }

    public Map<String,Object> getExpectedValues() {
        return expected;
    }

    public Row getRow() {
        return row;
    }
}
