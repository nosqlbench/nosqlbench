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

import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpResultSize;

public class Cqld4ScriptGraphOp implements CycleOp<GraphResultSet>, OpResultSize {
    private final CqlSession session;
    private final ScriptGraphStatement stmt;
    private int resultSize=0;

    public Cqld4ScriptGraphOp(CqlSession session, ScriptGraphStatement stmt) {
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
