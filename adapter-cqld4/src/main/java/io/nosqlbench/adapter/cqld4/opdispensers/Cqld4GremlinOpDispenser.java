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

package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatementBuilder;
import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4ScriptGraphOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.Optional;
import java.util.function.LongFunction;

public class Cqld4GremlinOpDispenser extends BaseOpDispenser<Cqld4ScriptGraphOp, Cqld4Space> {

    private final LongFunction<? extends ScriptGraphStatement> stmtFunc;
    private final LongFunction<CqlSession> sessionFunc;
    private final LongFunction<Long> diagFunc;

    public Cqld4GremlinOpDispenser(DriverAdapter adapter, LongFunction<CqlSession> sessionFunc, LongFunction<String> targetFunction, ParsedOp cmd) {
        super(adapter,cmd);
        this.sessionFunc = sessionFunc;
        this.diagFunc = cmd.getAsFunctionOr("diag", 0L);

        LongFunction<ScriptGraphStatementBuilder> func = l -> new ScriptGraphStatementBuilder();

        // graphname
        Optional<LongFunction<String>> graphnameFunc = cmd.getAsOptionalFunction("graphname");
        if (graphnameFunc.isPresent()) {
            LongFunction<ScriptGraphStatementBuilder> finalFunc = func;
            LongFunction<? extends String> stringLongFunction = graphnameFunc.get();
            func = l -> finalFunc.apply(l).setGraphName(stringLongFunction.apply(l));
        }

        LongFunction<ScriptGraphStatementBuilder> finalFunc = func;
        this.stmtFunc = l -> finalFunc.apply(l).setScript(targetFunction.apply(l)).build();

    }

    @Override
    public Cqld4ScriptGraphOp apply(long value) {
        ScriptGraphStatement stmt = stmtFunc.apply(value);
        if (diagFunc.apply(value)>0L) {
            System.out.println("## GREMLIN DIAG: ScriptGraphStatement on graphname(" + stmt.getGraphName() + "):\n" + stmt.getScript());
        }
        return new Cqld4ScriptGraphOp(sessionFunc.apply(value), stmt);
    }

}
