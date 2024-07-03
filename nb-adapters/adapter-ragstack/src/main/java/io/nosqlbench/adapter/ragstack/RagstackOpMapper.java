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

package io.nosqlbench.adapter.ragstack;

import io.nosqlbench.adapter.ragstack.opdispensers.RagstackLoadDatasetOpDispenser;
import io.nosqlbench.adapter.ragstack.ops.RagstackBaseOp;
import io.nosqlbench.adapter.ragstack.ops.RagstackOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RagstackOpMapper implements OpMapper<RagstackBaseOp> {
    private static final Logger logger = LogManager.getLogger(RagstackOpMapper.class);
    private final RagstackDriverAdapter adapter;

    public RagstackOpMapper(RagstackDriverAdapter ragstackDriverAdapter) {
        this.adapter = ragstackDriverAdapter;
    }

    @Override
    public OpDispenser<? extends RagstackBaseOp> apply(ParsedOp op) {
        TypeAndTarget<RagstackOpType, String> typeAndTarget = op.getTypeAndTarget(
            RagstackOpType.class,
            String.class,
            "type",
            "collection"
        );
        logger.debug(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");
        return switch (typeAndTarget.enumId) {
            case load_dataset -> new RagstackLoadDatasetOpDispenser(adapter, op, typeAndTarget.targetFunction);
        };
    }
}
