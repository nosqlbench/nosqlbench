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

package io.nosqlbench.adapter.azureaisearch;

import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchBaseOp;
import io.nosqlbench.adapter.azureaisearch.opsdispenser.AzureAISearchBaseOpDispenser;
import io.nosqlbench.adapter.azureaisearch.opsdispenser.AzureAISearchCreateOrUpdateIndexOpDispenser;
import io.nosqlbench.adapter.azureaisearch.opsdispenser.AzureAISearchDeleteIndexOpDispenser;
import io.nosqlbench.adapter.azureaisearch.opsdispenser.AzureAISearchListIndexesOpDispenser;
import io.nosqlbench.adapter.azureaisearch.opsdispenser.AzureAISearchSearchDocumentsOpDispenser;
import io.nosqlbench.adapter.azureaisearch.opsdispenser.AzureAISearchUploadDocumentsOpDispenser;
import io.nosqlbench.adapter.azureaisearch.types.AzureAISearchOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;

import java.util.function.LongFunction;

public class AzureAISearchOpMapper implements OpMapper<AzureAISearchBaseOp<?,?>, AzureAISearchSpace> {
    private static final Logger logger = LogManager.getLogger(AzureAISearchOpMapper.class);
    private final AzureAISearchDriverAdapter adapter;

    /**
     * Create a new {@code AzureAISearchOpMapper} implementing the {@link OpMapper}.
     * interface.
     *
     * @param adapter
     *     The associated {@link AzureAISearchDriverAdapter}
     */
    public AzureAISearchOpMapper(AzureAISearchDriverAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Given an instance of a {@link ParsedOp} returns the appropriate
     * {@link AzureAISearchBaseOpDispenser} subclass.
     *
     * @param adapterC
     * @param op
     *     The {@link ParsedOp} to be evaluated
     * @param spaceInitF
     * @return The correct {@link AzureAISearchBaseOpDispenser} subclass based on
     *     the op type
     */
    @Override
    public OpDispenser<AzureAISearchBaseOp<?,?>> apply(NBComponent adapterC, ParsedOp op, LongFunction<AzureAISearchSpace> spaceInitF) {

        TypeAndTarget<AzureAISearchOpType, String> typeAndTarget = op.getTypeAndTarget(AzureAISearchOpType.class,
            String.class, "type", "target");
        logger.info(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");

        return switch (typeAndTarget.enumId) {
            case delete_index -> new AzureAISearchDeleteIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_or_update_index ->
                new AzureAISearchCreateOrUpdateIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case list_indexes -> new AzureAISearchListIndexesOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case upload_documents ->
                new AzureAISearchUploadDocumentsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case search_documents ->
                new AzureAISearchSearchDocumentsOpDispenser(adapter, op, typeAndTarget.targetFunction);

//		default -> throw new RuntimeException(
//				"Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " + "mapping parsed op " + op);
        };
    }

}
