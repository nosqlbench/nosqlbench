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

package io.nosqlbench.adapter.gcpspanner.ops;

import com.google.cloud.spanner.Spanner;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Abstract base class for GCP Spanner operations.
 * This class implements the CycleOp interface and provides a template for executing operations with a Spanner client.
 *
 * @param <RESULT> the type of the request parameter
 */
public abstract class GCPSpannerBaseOp<REQUEST, RESULT> implements CycleOp<RESULT> {

    protected final static Logger logger = LogManager.getLogger(GCPSpannerBaseOp.class);

    protected final Spanner spannerClient;
    protected final REQUEST request;
    protected final LongFunction<Object> apiCall;

    /**
     * Constructs a new GCPSpannerBaseOp with the specified Spanner client and request parameter.
     *
     * @param spannerClient the Spanner client to use for operations
     * @param requestParam the request parameter for the operation
     */
    public GCPSpannerBaseOp(Spanner spannerClient, REQUEST requestParam) {
        this.spannerClient = spannerClient;
        this.request = requestParam;
        this.apiCall = this::applyOp;
    }

    /**
     * Applies the operation for the given cycle value.
     * This method logs the operation and handles any exceptions by throwing a RuntimeException.
     *
     * @param value the cycle value
     * @return the result of the operation
     */
    @Override
    public final RESULT apply(long value) {
        logger.trace(() -> "applying op: " + this);

        try {
            RESULT result = applyOp(value);
            return result;
        } catch (Exception rte) {
            throw new RuntimeException(rte);
        }
    }

    /**
     * Abstract method to be implemented by subclasses to define the specific operation logic.
     *
     * @param value the cycle value
     * @return the result of the operation
     */
    public abstract RESULT applyOp(long value);

    /**
     * Returns a string representation of the GCPSpannerBaseOp.
     *
     * @return a string representation of the GCPSpannerBaseOp
     */
    @Override
    public String toString() {
        return "GCPSpannerBaseOp(" + this.request.getClass().getSimpleName() + ")";
    }
}
