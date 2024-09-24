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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public abstract class GCPSpannerBaseOp<T> implements CycleOp<Object> {

    protected final static Logger logger = LogManager.getLogger(GCPSpannerBaseOp.class);

    protected final Spanner spannerClient;
    protected final T request;
    protected final LongFunction<Object> apiCall;

	public GCPSpannerBaseOp(Spanner searchIndexClient, T requestParam) {
        this.spannerClient = searchIndexClient;
        this.request = requestParam;
        this.apiCall = this::applyOp;
    }

    @Override
    public final Object apply(long value) {
        logger.trace(() -> "applying op: " + this);

        try {
            Object result = applyOp(value);

            return result;
        } catch (Exception rte) {
            throw new RuntimeException(rte);
        }
    }

    public abstract Object applyOp(long value);

    @Override
    public String toString() {
        return "GCPSpannerBaseOp(" + this.request.getClass().getSimpleName() + ")";
    }
}
