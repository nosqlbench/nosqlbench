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

/**
 * <p>This is not a core exception. It was added to the CQL activity type
 * driver for nosqlbench specifically to catch the following unexpected
 * condition:
 * Paging would be needed to read all the results from a read query, but the user
 * is not expecting to intentionally check and iterate the result sets for paging.
 * <p>
 * This should only be thrown if a result set would need paging, but configuration
 * options specific that it should not expect to. Rather than assume paging is completely
 * expected or unexpected, we simply assume that only 1 page is allowed, being the
 * first page, or what is thought of as "not paging".
 * <p>If this error is thrown, and paging is expected, then the user can adjust
 * fetchsize or maxpages in order to open up paging to the degree that is allowable or
 * expected.
 */
public class UnexpectedPagingException extends CqlGenericCycleException {

    private final ResultSet resultSet;
    private final String queryString;
    private final int fetchSize;
    private final int fetchedPages;
    private final int maxpages;

    public UnexpectedPagingException(
            long cycle,
            ResultSet resultSet,
            String queryString,
            int fetchedPages,
            int maxpages,
            int fetchSize) {
        super(cycle);
        this.resultSet = resultSet;
        this.queryString = queryString;
        this.fetchedPages = fetchedPages;
        this.maxpages = maxpages;
        this.fetchSize = fetchSize;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Additional paging would be required to read the results from this query fully" +
                ", but the user has not explicitly indicated that paging was expected.")
                .append(" fetched/allowed: ").append(fetchedPages).append("/").append(maxpages)
                .append(" fetchSize(").append(fetchSize).append("): ").append(queryString);
        return sb.toString();
    }
}
