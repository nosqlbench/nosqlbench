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
 *
 */

package io.nosqlbench.adapter.gcpspanner.opdispensers;

import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import io.nosqlbench.adapter.gcpspanner.GCPSpannerDriverAdapter;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerBaseOp;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerExecuteDmlOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser class for creating GCP Spanner Execute DML operations.
 * This class extends the GCPSpannerBaseOpDispenser and provides functionality
 * to create and configure GCPSpannerExecuteDmlOp instances.
 */
public class GCPSpannerExecuteDmlOpDispenser extends GCPSpannerBaseOpDispenser<GCPSpannerExecuteDmlOp, ResultSet> {
    private static final Logger logger = LogManager.getLogger(GCPSpannerExecuteDmlOpDispenser.class);
    private final LongFunction<GCPSpannerExecuteDmlOp> opFunction;

    /**
     * Constructs a new GCPSpannerExecuteDmlOpDispenser.
     *
     * @param adapter the driver adapter for GCP Spanner operations
     * @param op the parsed operation
     * @param targetFunction a function that provides the target string
     */
    public GCPSpannerExecuteDmlOpDispenser(GCPSpannerDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    /**
     * Creates a function that generates GCPSpannerExecuteDmlOp instances.
     *
     * @param op the parsed operation
     * @return a function that generates GCPSpannerExecuteDmlOp instances
     */
    private LongFunction<GCPSpannerExecuteDmlOp> createOpFunction(ParsedOp op) {
        return (l) -> new GCPSpannerExecuteDmlOp(
            spaceFunction.apply(l).getSpanner(),
            l,
            generateStatement(targetFunction.apply(l)),
            spaceFunction.apply(l).getDbClient()
        );
    }

    /**
     * Generates a Spanner Statement from a DML string.
     *
     * @param dml the DML string
     * @return the generated Statement
     */
    private Statement generateStatement(String dml) {
        return Statement.of(dml);
    }

    /**
     * Retrieves the GCP Spanner operation for the given value.
     *
     * @param cycle the input value
     * @return the GCP Spanner operation
     */
    @Override
    public GCPSpannerExecuteDmlOp getOp(long cycle) {
        return opFunction.apply(cycle);
    }
}
