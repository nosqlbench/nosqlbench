/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.cqld4.exceptions;


import com.datastax.oss.driver.api.core.cql.ResultSet;

/**
 * This was added to nosqlbench because the error handling logic was
 * starting to look a bit contrived. Because we need to be able
 * to respond to different result outcomes, it
 * is just simpler to have a single type of error-handling logic for all outcomes.
 */
public class ChangeUnappliedCycleException extends CqlGenericCycleException {

    private final ResultSet resultSet;
    private final String queryString;

    public ChangeUnappliedCycleException(ResultSet resultSet, String queryString) {
        super("Operation was not applied:" + queryString);
        this.resultSet = resultSet;
        this.queryString = queryString;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }
    public String getQueryString() { return queryString; }
}
