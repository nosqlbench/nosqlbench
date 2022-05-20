package io.nosqlbench.adapter.cqld4;

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


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Statement;

public class Cqld4ReboundStatement extends Cqld4Op {
    private final BoundStatement stmt;

    public Cqld4ReboundStatement(CqlSession session, int maxpages, boolean retryreplace, Cqld4OpMetrics metrics, BoundStatement rebound, RSProcessors processors) {
        super(session,maxpages,retryreplace,metrics,processors);
        this.stmt = rebound;
    }

    @Override
    public Statement<?> getStmt() {
        return stmt;
    }

    @Override
    public String getQueryString() {
        return stmt.getPreparedStatement().getQuery();
    }
}
