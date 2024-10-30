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
import io.nosqlbench.adapter.neo4j.ops.Neo4JSyncAutoCommitOp;
import io.nosqlbench.adapter.neo4j.Neo4JSpace;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import org.neo4j.driver.Session;

import java.util.function.LongFunction;


public class Neo4JSyncAutoCommitOpDispenser extends Neo4JBaseOpDispenser {

    public Neo4JSyncAutoCommitOpDispenser(Neo4JDriverAdapter adapter, ParsedOp op, LongFunction<Neo4JSpace> spaceFunc, String requiredTemplateKey) {
        super(adapter, op, spaceFunc, requiredTemplateKey);
    }

    @Override
    public LongFunction<Neo4JSyncAutoCommitOp> createOpFunc() {
        return l -> new Neo4JSyncAutoCommitOp(
            sessionFunc.apply(l),
            queryFunc.apply(l)
        );
    }
}
