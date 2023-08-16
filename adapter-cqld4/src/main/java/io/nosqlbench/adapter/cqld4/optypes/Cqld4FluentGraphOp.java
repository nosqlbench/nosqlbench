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

import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpResultSize;

public class Cqld4FluentGraphOp implements CycleOp<GraphResultSet>, OpResultSize {
    private final CqlSession session;
    private final FluentGraphStatement stmt;
    private int resultSize=0;

    public Cqld4FluentGraphOp(CqlSession session, FluentGraphStatement stmt) {
        this.session = session;
        this.stmt = stmt;
    }

    @Override
    public GraphResultSet apply(long value) {
        GraphResultSet result = session.execute(stmt);
        this.resultSize = result.all().size();
        return result;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
