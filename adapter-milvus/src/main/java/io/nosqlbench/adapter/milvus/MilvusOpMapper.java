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

package io.nosqlbench.adapter.milvus;

import io.nosqlbench.adapter.milvus.opdispensers.*;
import io.nosqlbench.adapter.milvus.ops.MilvusOp;
import io.nosqlbench.adapter.milvus.ops.MilvusOpTypes;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MilvusOpMapper implements OpMapper<MilvusOp> {
    private static final Logger logger = LogManager.getLogger(MilvusOpMapper.class);
    private final MilvusDriverAdapter adapter;

    /**
     * Create a new MilvusOpMapper implementing the {@link OpMapper} interface.
     *
     * @param adapter    The associated {@link MilvusDriverAdapter}
     */
    public MilvusOpMapper(MilvusDriverAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Given an instance of a {@link ParsedOp} returns the appropriate {@link MilvusOpDispenser} subclass
     *
     * @param op    The ParsedOp to be evaluated
     * @return      The correct MilvusOpDispenser subclass based on the op type
     */
    @Override
    public OpDispenser<? extends MilvusOp> apply(ParsedOp op) {
        TypeAndTarget<MilvusOpTypes, String> typeAndTarget = op.getTypeAndTarget(
            MilvusOpTypes.class,
            String.class,
            "type",
            "target"
        );
        logger.info(() -> "Using " + typeAndTarget.enumId + " statement form for '" + op.getName());

        return switch (typeAndTarget.enumId) {
            case drop_collection -> new MilvusDropCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_collection ->  new MilvusCreateCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_index -> new MilvusCreateIndexOpDispenser(adapter,op, typeAndTarget.targetFunction);
            case drop_index -> new MilvusDropIndexOpDispenser(adapter,op,typeAndTarget.targetFunction);
            case insert -> new MilvusInsertOpDispenser(adapter,op,typeAndTarget.targetFunction);
            case delete -> new MilvusDeleteOpDispenser(adapter,op,typeAndTarget.targetFunction);
            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
                "mapping parsed op " + op);
        };
    }
}

