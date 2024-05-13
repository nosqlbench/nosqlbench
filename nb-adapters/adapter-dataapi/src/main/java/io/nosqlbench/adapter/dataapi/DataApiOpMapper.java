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

package io.nosqlbench.adapter.dataapi;

import io.nosqlbench.adapter.dataapi.opdispensers.*;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapter.dataapi.ops.DataApiOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataApiOpMapper implements OpMapper<DataApiBaseOp> {
    private static final Logger logger = LogManager.getLogger(DataApiOpMapper.class);
    private final DataApiDriverAdapter adapter;

    public DataApiOpMapper(DataApiDriverAdapter dataApiDriverAdapter) {
        this.adapter = dataApiDriverAdapter;
    }

    @Override
    public OpDispenser<? extends DataApiBaseOp> apply(ParsedOp op) {
        TypeAndTarget<DataApiOpType, String> typeAndTarget = op.getTypeAndTarget(
            DataApiOpType.class,
            String.class,
            "type",
            "collection"
        );
        logger.debug(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");
        return switch (typeAndTarget.enumId) {
            case create_collection -> new DataApiCreateCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case insert_many -> new DataApiInsertManyOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case insert_one -> new DataApiInsertOneOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case insert_one_vector -> new DataApiInsertOneVectorOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case find -> new DataApiFindOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case find_one -> new DataApiFindOneOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case find_one_and_delete -> new DataApiFindOneAndDeleteOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case find_one_and_update -> new DataApiFindOneAndUpdateOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case find_vector -> new DataApiFindVectorOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case find_vector_filter -> new DataApiFindVectorFilterOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case update_one -> new DataApiUpdateOneOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case update_many -> new DataApiUpdateManyOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case delete_one -> new DataApiDeleteOneOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case delete_many -> new DataApiDeleteManyOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case delete_collection -> new DataApiDropCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
        };
    }
}
