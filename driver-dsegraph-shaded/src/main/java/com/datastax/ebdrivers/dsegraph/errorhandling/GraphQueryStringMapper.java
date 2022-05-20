package com.datastax.ebdrivers.dsegraph.errorhandling;

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


import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.RegularGraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;

public class GraphQueryStringMapper {
    public static String getQueryString(GraphStatement statement) {
        String queryString;
        if (statement instanceof RegularGraphStatement) {
            queryString = ((SimpleGraphStatement) statement).getQueryString();
        } else {
            queryString = "(ERROR) Unknown statement type: " + statement.getClass().getCanonicalName();
        }
        return queryString;
    }

}
