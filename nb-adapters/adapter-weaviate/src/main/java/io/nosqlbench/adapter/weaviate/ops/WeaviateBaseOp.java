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

package io.nosqlbench.adapter.weaviate.ops;


import java.util.function.LongFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;

public abstract class WeaviateBaseOp<T> implements CycleOp<Object> {

    protected final static Logger logger = LogManager.getLogger(WeaviateBaseOp.class);

	protected final WeaviateClient client;
    protected final T request;
    protected final LongFunction<Object> apiCall;

	public WeaviateBaseOp(WeaviateClient client, T requestParam) {
        this.client = client;
        this.request = requestParam;
        this.apiCall = this::applyOp;
    }

	public WeaviateBaseOp(WeaviateClient client, T requestParam, LongFunction<Object> call) {
        this.client = client;
        this.request = requestParam;
        this.apiCall = call;
    }

	@SuppressWarnings("unchecked")
	@Override
    public final Object apply(long value) {
        logger.trace("applying op: " + this);

        try {
            Object result = applyOp(value);
			if (result instanceof Result<?>) {

//                Result<Boolean> result = client.misc().readyChecker().run();
//
//                if (result.hasErrors()) {
//                  System.out.println(result.getError());
//                  return;
//                }
//                System.out.println(result.getResult());
				if (((Result<Boolean>) result).hasErrors()) {
					logger.error("Result status: {}", ((Result<Boolean>) result).getError().toString());
				}
            } else {
				logger.warn("Op '" + this.toString() + "' did not return a Result 'Result<Boolean>' type." +
                    " Exception handling will be bypassed"
                );
            }
//            return (Result<Boolean> result).getResult();
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
        return "WeaviateOp(" + this.request.getClass().getSimpleName() + ")";
    }
}
