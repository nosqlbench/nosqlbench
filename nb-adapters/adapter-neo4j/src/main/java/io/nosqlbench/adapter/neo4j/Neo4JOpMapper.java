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

package io.nosqlbench.adapter.neo4j;

import io.nosqlbench.adapter.neo4j.opdispensers.*;
import io.nosqlbench.adapter.neo4j.ops.Neo4JBaseOp;
import io.nosqlbench.adapter.neo4j.types.Neo4JOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.ConcurrentSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;

import java.util.function.IntFunction;
import java.util.function.LongFunction;


public class Neo4JOpMapper implements OpMapper<Neo4JBaseOp,Neo4JSpace> {
    private final Neo4JDriverAdapter adapter;

    public Neo4JOpMapper(Neo4JDriverAdapter adapter, ConcurrentSpaceCache<Neo4JSpace> cache) {
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<Neo4JBaseOp> apply(ParsedOp op, LongFunction<Neo4JSpace> spaceInitF) {
        TypeAndTarget<Neo4JOpType, String> typeAndTarget = op.getTypeAndTarget(Neo4JOpType.class, String.class);
        LongFunction<Neo4JSpace> spaceFunc = adapter.getSpaceFunc(op);
        return switch (typeAndTarget.enumId) {
            case sync_autocommit -> new Neo4JSyncAutoCommitOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
            case async_autocommit -> new Neo4JAsyncAutoCommitOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
            case sync_read_transaction -> new Neo4JSyncReadTxnOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
            case async_read_transaction -> new Neo4JAsyncReadTxnOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
            case sync_write_transaction -> new Neo4JSyncWriteTxnOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
            case async_write_transaction -> new Neo4JAsyncWriteTxnOpDispenser(
                adapter, op, spaceFunc, typeAndTarget.enumId.getValue()
            );
        };
    }
}

