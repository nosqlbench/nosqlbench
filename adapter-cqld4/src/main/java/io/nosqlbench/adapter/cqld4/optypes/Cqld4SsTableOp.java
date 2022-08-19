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

package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.RSProcessors;

// Need to create SsTableStatement
public class Cqld4SsTableOp extends Cqld4CqlOp {
    private final CqlSession session;
    private final SsTableStatement stmt;

    public Cqld4SsTableOp(CqlSession session, SsTableStatement stmt, int maxpages, boolean retryreplace) {
        super(session, maxpages,retryreplace, new RSProcessors());
        this.stmt = stmt;
    }

    @Override
    public SsTableStatement getStmt() {
        return stmt;
    }

    @Override
    public String getQueryString() {
        return stmt.getQuery();
    }

}
