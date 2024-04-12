/*
 * Copyright (c) 2022-2023 nosqlbench
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
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.instruments.CqlOpMetrics;

public class Cqld4CqlSimpleStatement extends Cqld4CqlOp {
    private final SimpleStatement stmt;

    public Cqld4CqlSimpleStatement(CqlSession session, SimpleStatement stmt, int maxPages, boolean retryReplace, int maxLwtRetries, CqlOpMetrics metrics) {
        super(session, maxPages,retryReplace, maxLwtRetries, new RSProcessors(), metrics);
        this.stmt = stmt;
    }

    @Override
    public SimpleStatement getStmt() {
        return stmt;
    }

    @Override
    public String getQueryString() {
        return stmt.getQuery();
    }

}
