/*
 * Copyright (c) 2020-2024 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.nosqlbench.adapter.weaviate.opsdispensers;

import java.util.function.LongFunction;

import io.nosqlbench.adapter.weaviate.WeaviateDriverAdapter;
import io.nosqlbench.adapter.weaviate.WeaviateSpace;
import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.auth.exception.AuthException;

public abstract class WeaviateBaseOpDispenser<T> extends BaseOpDispenser<WeaviateBaseOp<T>, WeaviateSpace> {

	protected final LongFunction<WeaviateSpace> weaviateSpaceFunction;
	protected final LongFunction<WeaviateClient> clientFunction;
	private final LongFunction<? extends WeaviateBaseOp<T>> opF;
	private final LongFunction<T> paramF;

	protected WeaviateBaseOpDispenser(WeaviateDriverAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
		super((DriverAdapter) adapter, op);
		this.weaviateSpaceFunction = adapter.getSpaceFunc(op);
		this.clientFunction = (long l) -> {
			try {
				return this.weaviateSpaceFunction.apply(l).getClient();
			} catch (AuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		};
		this.paramF = getParamFunc(this.clientFunction, op, targetF);
		this.opF = createOpFunc(paramF, this.clientFunction, op, targetF);
	}

	protected WeaviateDriverAdapter getDriverAdapter() {
		return (WeaviateDriverAdapter) adapter;
	}

	public abstract LongFunction<T> getParamFunc(LongFunction<WeaviateClient> clientF, ParsedOp op,
			LongFunction<String> targetF);

	public abstract LongFunction<WeaviateBaseOp<T>> createOpFunc(LongFunction<T> paramF,
			LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF);

	@Override
	public WeaviateBaseOp<T> getOp(long value) {
		return opF.apply(value);
	}

}
