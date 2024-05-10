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
import io.nosqlbench.adapter.neo4j.Neo4JSpace;
import io.nosqlbench.adapter.neo4j.ops.Neo4JAsyncWriteTxnOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import org.neo4j.driver.async.AsyncSession;

import java.util.function.LongFunction;


public class Neo4JAsyncWriteTxnOpDispenser extends Neo4JBaseOpDispenser {

    public Neo4JAsyncWriteTxnOpDispenser(Neo4JDriverAdapter adapter, ParsedOp op, LongFunction<Neo4JSpace> spaceFunc, String requiredTemplateKey) {
        super(adapter, op, spaceFunc, requiredTemplateKey);
    }

    @Override
    public LongFunction<Neo4JAsyncWriteTxnOp> createOpFunc() {
        return l -> new Neo4JAsyncWriteTxnOp(
            spaceFunc.apply(l).getDriver().session(AsyncSession.class),
            queryFunc.apply(l)
        );
    }
}
