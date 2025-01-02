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

package io.nosqlbench.adapter.weaviate;

import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateCreateCollectionOpDispenser;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateCreateObjectsOpDispenser;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateDeleteCollectionOpDispenser;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateGetCollectionSchemaOpDispenser;
import io.nosqlbench.adapter.weaviate.types.WeaviateOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;

import java.util.function.LongFunction;

public class WeaviateOpMapper implements OpMapper<WeaviateBaseOp<?,?>,WeaviateSpace> {
	private static final Logger logger = LogManager.getLogger(WeaviateOpMapper.class);
	private final WeaviateDriverAdapter adapter;

	/**
	 * Create a new WeaviateOpMapper implementing the {@link OpMapper} interface.
	 *
	 * @param adapter The associated {@link WeaviateDriverAdapter}
	 */
	public WeaviateOpMapper(WeaviateDriverAdapter adapter) {
		this.adapter = adapter;
	}

    @Override
    public OpDispenser<? extends WeaviateBaseOp<?,?>> apply(
        NBComponent adapterC,
        ParsedOp pop,
        LongFunction<WeaviateSpace> spaceF
    ) {
        TypeAndTarget<WeaviateOpType, String> typeAndTarget = pop.getTypeAndTarget(
            WeaviateOpType.class,
            String.class,
            "type", "target"
        );
        LongFunction<String> targetF = typeAndTarget.targetFunction;

        logger.info(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + pop.getName() + "'");

        return switch (typeAndTarget.enumId) {
            case delete_collection -> new WeaviateDeleteCollectionOpDispenser(adapter, pop, spaceF, targetF);
            case create_collection -> new WeaviateCreateCollectionOpDispenser(adapter, pop, spaceF, targetF);
            case get_collection_schema ->
                new WeaviateGetCollectionSchemaOpDispenser(adapter, pop, spaceF, targetF);
            case create_objects -> new WeaviateCreateObjectsOpDispenser(adapter, pop, spaceF, targetF);
//            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
//                "mapping parsed op " + op);
        };
    }
}

