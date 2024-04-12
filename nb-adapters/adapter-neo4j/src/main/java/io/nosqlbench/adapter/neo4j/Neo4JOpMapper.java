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

package io.nosqlbench.adapter.neo4j;

import io.nosqlbench.adapter.neo4j.opdispensers.*;
import io.nosqlbench.adapter.neo4j.ops.Neo4JBaseOp;
import io.nosqlbench.adapter.neo4j.types.Neo4JOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;

import java.util.function.LongFunction;


public class Neo4JOpMapper implements OpMapper<Neo4JBaseOp> {
    private final DriverSpaceCache<? extends Neo4JSpace> cache;
    private final Neo4JDriverAdapter adapter;

    public Neo4JOpMapper(Neo4JDriverAdapter adapter, DriverSpaceCache<? extends Neo4JSpace> cache) {
        this.adapter = adapter;
        this.cache = cache;
    }

    @Override
    public OpDispenser<? extends Neo4JBaseOp> apply(ParsedOp op) {
        TypeAndTarget<Neo4JOpType, String> typeAndTarget = op.getTypeAndTarget(Neo4JOpType.class, String.class);
        LongFunction<String> spaceNameFunc = op.getAsFunctionOr("space", "default");
        LongFunction<Neo4JSpace> spaceFunc = l -> cache.get(spaceNameFunc.apply(l));
        return switch (typeAndTarget.enumId) {
            case autocommit -> new Neo4JAutoCommitOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
            case read_transaction -> new Neo4JReadTxnOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
            case write_transaction -> new Neo4JWriteTxnOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
        };
    }
}

