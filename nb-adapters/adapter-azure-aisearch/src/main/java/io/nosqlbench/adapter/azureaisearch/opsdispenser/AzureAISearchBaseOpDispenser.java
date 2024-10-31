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

package io.nosqlbench.adapter.azureaisearch.opsdispenser;

import java.util.function.LongFunction;

import com.azure.search.documents.indexes.SearchIndexClient;

import io.nosqlbench.adapter.azureaisearch.AzureAISearchDriverAdapter;
import io.nosqlbench.adapter.azureaisearch.AzureAISearchSpace;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

public abstract class AzureAISearchBaseOpDispenser<REQUEST,RESULT>
		extends BaseOpDispenser<AzureAISearchBaseOp, AzureAISearchSpace> {

	protected final LongFunction<AzureAISearchSpace> azureAISearchSpaceFunction;
	protected final LongFunction<SearchIndexClient> clientFunction;
	private final LongFunction<? extends AzureAISearchBaseOp<REQUEST,RESULT>> opF;
	private final LongFunction<REQUEST> paramF;

	@SuppressWarnings("rawtypes")
	protected AzureAISearchBaseOpDispenser(AzureAISearchDriverAdapter adapter, ParsedOp op,
			LongFunction<String> targetF) {
		super((DriverAdapter) adapter, op);
		this.azureAISearchSpaceFunction = adapter.getSpaceFunc(op);
		this.clientFunction = (long l) -> {
			try {
				return this.azureAISearchSpaceFunction.apply(l).getSearchIndexClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		};
		this.paramF = getParamFunc(this.clientFunction, op, targetF);
		this.opF = createOpFunc(paramF, this.clientFunction, op, targetF);
	}

	protected AzureAISearchDriverAdapter getDriverAdapter() {
		return (AzureAISearchDriverAdapter) adapter;
	}

	public abstract LongFunction<REQUEST> getParamFunc(LongFunction<SearchIndexClient> clientF, ParsedOp op,
			LongFunction<String> targetF);

	public abstract LongFunction<AzureAISearchBaseOp<REQUEST,RESULT>> createOpFunc(LongFunction<REQUEST> paramF,
			LongFunction<SearchIndexClient> clientF, ParsedOp op, LongFunction<String> targetF);

	@Override
	public AzureAISearchBaseOp<REQUEST,RESULT> getOp(long value) {
		return opF.apply(value);
	}

}
