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
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.instruments.CqlOpMetrics;

import java.util.ArrayList;
import java.util.List;

public class Cqld4CqlBatchStatement extends Cqld4CqlOp {

    private final BatchStatement stmt;

    public Cqld4CqlBatchStatement(CqlSession session, BatchStatement stmt, int maxPage, int maxLwtRetries, boolean retryReplace, CqlOpMetrics metrics) {
        super(session,maxPage,retryReplace,maxLwtRetries,new RSProcessors(), metrics);
        this.stmt = stmt;
    }

    @Override
    public BatchStatement getStmt() {
        return stmt;
    }

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder();
        stmt.iterator().forEachRemaining(s -> sb.append(s).append("\n"));
        return sb.toString();
    }
}
