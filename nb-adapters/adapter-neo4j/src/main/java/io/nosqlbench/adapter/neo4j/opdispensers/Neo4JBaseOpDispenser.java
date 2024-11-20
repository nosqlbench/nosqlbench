/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.adapter.neo4j.opdispensers;

import io.nosqlbench.adapter.neo4j.Neo4JDriverAdapter;
import io.nosqlbench.adapter.neo4j.ops.Neo4JBaseOp;
import io.nosqlbench.adapter.neo4j.Neo4JSpace;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.async.AsyncSession;

import java.util.Collections;
import java.util.function.LongFunction;
import java.util.Map;


public abstract class Neo4JBaseOpDispenser extends BaseOpDispenser<Neo4JBaseOp, Neo4JSpace> {
    protected final LongFunction<Neo4JSpace> spaceFunc;
    protected final LongFunction<String> cypherFunc;
    protected final LongFunction<Query> queryFunc;
    protected final LongFunction<Map> paramFunc;
    protected final LongFunction<Neo4JBaseOp> opFunc;
    protected final LongFunction<Session> sessionFunc;
    protected final LongFunction<AsyncSession> asyncSessionFunc;

    public Neo4JBaseOpDispenser(Neo4JDriverAdapter adapter, ParsedOp op, LongFunction<Neo4JSpace> spaceFunc, String requiredTemplateKey) {
        super(adapter, op, adapter.getSpaceFunc(op));
        this.spaceFunc = spaceFunc;
        this.cypherFunc = op.getAsRequiredFunction(requiredTemplateKey);
        this.paramFunc = createParamFunc(op);
        this.queryFunc = createQueryFunc();
        this.opFunc = (LongFunction<Neo4JBaseOp>) createOpFunc();
        this.sessionFunc = getSessionFunction(spaceFunc,op);
        this.asyncSessionFunc = getAsyncSessionFunction(spaceFunc,op);
    }

    private LongFunction<Map> createParamFunc(ParsedOp op) {
        return op.getAsOptionalFunction("query_params", Map.class)
            .orElse(l -> Collections.emptyMap());
    }

    private LongFunction<Session> getSessionFunction(LongFunction<Neo4JSpace> spaceFunc, ParsedOp op) {
        LongFunction<SessionConfig.Builder> scbF = (long l) -> SessionConfig.builder();
        scbF = op.enhanceFuncOptionally(scbF,"database",String.class,SessionConfig.Builder::withDatabase);
        LongFunction<SessionConfig.Builder> finalScbF = scbF;
        LongFunction<SessionConfig> scF = (long l) -> finalScbF.apply(l).build();
        return (long l) -> spaceFunc.apply(l).getDriver().session(Session.class,scF.apply(l));

    }

    private LongFunction<AsyncSession> getAsyncSessionFunction(LongFunction<Neo4JSpace> spaceFunc, ParsedOp op) {
        LongFunction<SessionConfig.Builder> scbF = (long l) -> SessionConfig.builder();
        scbF = op.enhanceFuncOptionally(scbF,"database",String.class,SessionConfig.Builder::withDatabase);
        LongFunction<SessionConfig.Builder> finalScbF = scbF;
        LongFunction<SessionConfig> scF = (long l) -> finalScbF.apply(l).build();
        return (long l) -> spaceFunc.apply(l).getDriver().session(AsyncSession.class,scF.apply(l));

    }

    /**
     * Reference:
     * - https://neo4j.com/docs/api/java-driver/current/org.neo4j.driver/org/neo4j/driver/Query.html#withParameters(java.util.Map)
     */
    private LongFunction<Query> createQueryFunc() {
        return l -> new Query(cypherFunc.apply(l)).withParameters(paramFunc.apply(l));
    }

    public abstract LongFunction<? extends Neo4JBaseOp> createOpFunc();

    @Override
    public Neo4JBaseOp getOp(long cycle) {
        return opFunc.apply(cycle);
    }
}
