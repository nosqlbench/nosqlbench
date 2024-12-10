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

package io.nosqlbench.adapter.gcpspanner;

import io.nosqlbench.adapter.gcpspanner.opdispensers.*;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerBaseOp;
import io.nosqlbench.adapter.gcpspanner.types.GCPSpannerOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class GCPSpannerOpMapper implements OpMapper<GCPSpannerBaseOp<?,?>, GCPSpannerSpace> {
    private static final Logger logger = LogManager.getLogger(GCPSpannerOpMapper.class);
    private final GCPSpannerDriverAdapter adapter;

    /**
     * Create a new {@code GCPSpannerOpMapper} implementing the {@link OpMapper}.
     * interface.
     *
     * @param adapter
     *     The associated {@link GCPSpannerDriverAdapter}
     */
    public GCPSpannerOpMapper(GCPSpannerDriverAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Given an instance of a {@link ParsedOp} returns the appropriate
     * {@link GCPSpannerBaseOpDispenser} subclass.
     *
     * @param adapterC
     * @param op
     *     The {@link ParsedOp} to be evaluated
     * @param spaceF
     * @return The correct {@link GCPSpannerBaseOpDispenser} subclass based on
     *     the op type
     */
    @Override
    public OpDispenser<GCPSpannerBaseOp<?,?>> apply(NBComponent adapterC, ParsedOp op, LongFunction<GCPSpannerSpace> spaceF) {
        TypeAndTarget<GCPSpannerOpType, String> typeAndTarget = op.getTypeAndTarget(GCPSpannerOpType.class,
            String.class, "type", "target");
        logger.info(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");

        OpDispenser<GCPSpannerBaseOp<?, ?>> dispenser = switch (typeAndTarget.enumId) {
            case drop_database_ddl ->
                new GCPSpannerDropDatabaseDdlOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_database_ddl ->
                new GCPSpannerCreateDatabaseDdlOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case update_database_ddl ->
                new GCPSpannerUpdateDatabaseDdlOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case insert -> new GCPSpannerInsertOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case execute_dml -> new GCPSpannerExecuteDmlOpDispenser(adapter, op, typeAndTarget.targetFunction);
        };
        return dispenser;
    }
}
