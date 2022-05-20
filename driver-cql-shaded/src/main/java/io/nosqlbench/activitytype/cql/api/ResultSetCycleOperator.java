package io.nosqlbench.activitytype.cql.api;

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

/**
 * An operator interface for performing a modular action on CQL ResultSets per-cycle.
 */
public interface ResultSetCycleOperator {
    /**
     * Perform an action on a result set for a specific cycle.
     * @param resultSet The ResultSet for the given cycle
     * @param statement The statement for the given cycle
     * @param cycle The cycle for which the statement was submitted
     * @return A value, only meaningful when used with aggregated operators
     */
    int apply(ResultSet resultSet, Statement statement, long cycle);
}
