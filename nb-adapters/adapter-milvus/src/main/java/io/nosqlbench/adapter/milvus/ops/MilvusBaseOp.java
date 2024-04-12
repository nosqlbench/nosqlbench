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

package io.nosqlbench.adapter.milvus.ops;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.R;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;
import java.util.function.LongFunction;

public abstract class MilvusBaseOp<T> implements CycleOp<Object> {

    protected final static Logger logger = LogManager.getLogger(MilvusBaseOp.class);

    protected final MilvusServiceClient client;
    protected final T request;
    protected final LongFunction<Object> apiCall;

    public MilvusBaseOp(MilvusServiceClient client, T requestParam) {
        this.client = client;
        this.request = requestParam;
        this.apiCall = this::applyOp;
    }

    public MilvusBaseOp(MilvusServiceClient client, T requestParam, LongFunction<Object> call) {
        this.client = client;
        this.request = requestParam;
        this.apiCall = call;
    }

    @Override
    public final Object apply(long value) {
        logger.trace("applying op: " + this);

        try {
            Object result = applyOp(value);
            if (result instanceof R<?> r) {
                var error = r.getException();
                if (error!=null) {
                    throw error;
                }
            } else {
                logger.warn("Op '" + this.toString() + "' did not return a Result 'R' type." +
                    " Exception handling will be bypassed"
                );
            }
            return result;
        } catch (Exception e) {
            if (e instanceof RuntimeException rte) {
                throw rte;
            } else {
                throw new RuntimeException(e);
            }
        }
    };

    public abstract Object applyOp(long value);

    @Override
    public String toString() {
        return "MilvusOp(" + this.request.getClass().getSimpleName() + ")";
    }
}
