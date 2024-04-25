/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.qdrant;

import io.nosqlbench.adapter.qdrant.opdispensers.QdrantBaseOpDispenser;
import io.nosqlbench.adapter.qdrant.opdispensers.QdrantCreateCollectionOpDispenser;
import io.nosqlbench.adapter.qdrant.opdispensers.QdrantDeleteCollectionOpDispenser;
import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapter.qdrant.types.QdrantOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QdrantOpMapper implements OpMapper<QdrantBaseOp<?>> {
    private static final Logger logger = LogManager.getLogger(QdrantOpMapper.class);
    private final QdrantDriverAdapter adapter;

    /**
     * Create a new QdrantOpMapper implementing the {@link OpMapper} interface.
     *
     * @param adapter The associated {@link QdrantDriverAdapter}
     */
    public QdrantOpMapper(QdrantDriverAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Given an instance of a {@link ParsedOp} returns the appropriate {@link QdrantBaseOpDispenser} subclass
     *
     * @param op The {@link ParsedOp} to be evaluated
     * @return The correct {@link QdrantBaseOpDispenser} subclass based on the op type
     */
    @Override
    public OpDispenser<? extends QdrantBaseOp<?>> apply(ParsedOp op) {
        TypeAndTarget<QdrantOpType, String> typeAndTarget = op.getTypeAndTarget(
            QdrantOpType.class,
            String.class,
            "type",
            "target"
        );
        logger.info(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");

        return switch (typeAndTarget.enumId) {
            case delete_collection -> new QdrantDeleteCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_collection -> new QdrantCreateCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
//                "mapping parsed op " + op);
        };
    }
}
