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
 *
 */

package io.nosqlbench.adapter.gcpspanner.opdispensers;

import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Value;
import io.nosqlbench.adapter.gcpspanner.GCPSpannerDriverAdapter;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerBaseOp;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerInsertVectorOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.function.LongFunction;

/**
 * This class is responsible for dispensing GCP Spanner insert vector operations.
 * It extends the GCPSpannerBaseOpDispenser and provides the necessary implementation
 * to create and configure GCPSpannerInsertVectorOp instances.
 */
public class GCPSpannerInsertVectorOpDispenser extends GCPSpannerBaseOpDispenser {
    private static final Logger logger = LogManager.getLogger(GCPSpannerInsertVectorOpDispenser.class);
    private final LongFunction<Map> queryParamsFunction;

    /**
     * Constructs a new GCPSpannerInsertVectorOpDispenser.
     *
     * @param adapter the GCP Spanner driver adapter
     * @param op the parsed operation
     * @param targetFunction a function that provides the target table name based on a long value
     */
    public GCPSpannerInsertVectorOpDispenser(GCPSpannerDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.queryParamsFunction = createParamsFunction(op);
    }

    /**
     * Creates a function that provides query parameters based on a long value.
     *
     * @param op the parsed operation
     * @return a function that provides query parameters
     */
    private LongFunction<Map> createParamsFunction(ParsedOp op) {
        return op.getAsOptionalFunction("query_params", Map.class)
            .orElse(_ -> Collections.emptyMap());
    }

    /**
     * Returns a GCPSpannerInsertVectorOp instance configured with the provided value.
     *
     * @param value the value used to configure the operation
     * @return a configured GCPSpannerInsertVectorOp instance
     */
    @Override
    public GCPSpannerBaseOp<?> getOp(long value) {
        Mutation.WriteBuilder builder = Mutation.newInsertBuilder(targetFunction.apply(value));
        Map<String, Object> params = queryParamsFunction.apply(value);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.set(entry.getKey()).to(convertToValue(entry));
        }
        return new GCPSpannerInsertVectorOp(
            spaceFunction.apply(value).getSpanner(),
            value,
            builder.build(),
            spaceFunction.apply(value).getDbClient()
        );
    }

    private Value convertToValue(Map.Entry<String, Object> entry) {
        return switch(entry.getValue()) {
            case String s -> Value.string(s);
            case Integer i -> Value.int64(i);
            case Long l -> Value.int64(l);
            case Double d -> Value.float64(d);
            case Float f -> Value.float32(f);
            case long[] larr -> Value.int64Array(larr);
            case float[] farr -> Value.float32Array(farr);
            case double[] darr -> Value.float64Array(darr);
            default -> throw new IllegalArgumentException("Unsupported value type: " + entry.getValue().getClass());
        };
    }
}
