/*
 * Copyright (c) 2024 nosqlbench
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

import java.util.Collections;
import java.util.function.LongFunction;
import java.util.Map;


public abstract class Neo4JBaseOpDispenser extends BaseOpDispenser<Neo4JBaseOp, Neo4JSpace> {
    protected final LongFunction<Neo4JSpace> spaceFunc;
    protected final LongFunction<String> cypherFunc;
    protected final LongFunction<Query> queryFunc;
    protected final LongFunction<Map> paramFunc;
    protected final LongFunction<Neo4JBaseOp> opFunc;

    public Neo4JBaseOpDispenser(Neo4JDriverAdapter adapter, ParsedOp op, LongFunction<Neo4JSpace> spaceFunc, String requiredTemplateKey) {
        super(adapter, op);
        this.spaceFunc = spaceFunc;
        this.cypherFunc = op.getAsRequiredFunction(requiredTemplateKey);
        this.paramFunc = createParamFunc(op);
        this.queryFunc = createQueryFunc();
        this.opFunc = (LongFunction<Neo4JBaseOp>) createOpFunc();
    }

    private LongFunction<Map> createParamFunc(ParsedOp op) {
        return op.getAsOptionalFunction("query_params", Map.class)
            .orElse(l -> Collections.emptyMap());
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
    public Neo4JBaseOp apply(long cycle) {
        return opFunc.apply(cycle);
    }
}
