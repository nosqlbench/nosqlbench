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

package io.nosqlbench.adapter.qdrant.ops;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.qdrant.client.QdrantClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public abstract class QdrantBaseOp<REQUEST,RESULT> implements CycleOp<RESULT> {

    protected final static Logger logger = LogManager.getLogger(QdrantBaseOp.class);

    protected final QdrantClient client;
    protected final REQUEST request;
    protected final LongFunction<Object> apiCall;

    public QdrantBaseOp(QdrantClient client, REQUEST requestParam) {
        this.client = client;
        this.request = requestParam;
        this.apiCall = this::applyOp;
    }

    public QdrantBaseOp(QdrantClient client, REQUEST requestParam, LongFunction<Object> call) {
        this.client = client;
        this.request = requestParam;
        this.apiCall = call;
    }

    @Override
    public final RESULT apply(long value) {
        logger.trace("applying op: {}", this);

        try {
            RESULT result = applyOp(value);
            return result;
        } catch (Exception e) {
            RuntimeException rte = (RuntimeException) e;
            throw rte;
        }
    }

    public abstract RESULT applyOp(long value);

    @Override
    public String toString() {
        return "QdrantOp(" + this.request.getClass().getSimpleName() + ")";
    }
}
