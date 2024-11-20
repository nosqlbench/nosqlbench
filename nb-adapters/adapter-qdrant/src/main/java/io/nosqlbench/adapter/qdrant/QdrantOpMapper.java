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

import io.nosqlbench.adapter.qdrant.opdispensers.*;
import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapter.qdrant.types.QdrantOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class QdrantOpMapper implements OpMapper<QdrantBaseOp<?,?>,QdrantSpace> {
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
     * @param adapterC
     * @param op
     *     The {@link ParsedOp} to be evaluated
     * @param spaceInitF
     * @return The correct {@link QdrantBaseOpDispenser} subclass based on the op type
     */
    @Override
    public OpDispenser<QdrantBaseOp<?,?>> apply(NBComponent adapterC, ParsedOp op, LongFunction<QdrantSpace> spaceInitF) {
        TypeAndTarget<QdrantOpType, String> typeAndTarget = op.getTypeAndTarget(
            QdrantOpType.class,
            String.class,
            "type",
            "target"
        );
        logger.info(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");

        OpDispenser<QdrantBaseOp<?,?>> dispenser =  switch (typeAndTarget.enumId) {
            case delete_collection -> new QdrantDeleteCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_collection -> new QdrantCreateCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_payload_index ->
                new QdrantCreatePayloadIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case search_points -> new QdrantSearchPointsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case upsert_points -> new QdrantUpsertPointsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case count_points -> new QdrantCountPointsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case list_collections -> new QdrantListCollectionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case collection_info -> new QdrantCollectionInfoOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case collection_exists -> new QdrantCollectionExistsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case list_collection_aliases ->
                new QdrantListCollectionAliasesOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case list_snapshots -> new QdrantListSnapshotsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
//                "mapping parsed op " + op);
        };
        return dispenser;
    }

}
