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
package io.nosqlbench.adapter.azureaisearch.opsdispenser;

import java.util.function.LongFunction;

import com.azure.search.documents.indexes.SearchIndexClient;

import io.nosqlbench.adapter.azureaisearch.AzureAISearchDriverAdapter;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchBaseOp;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchListIndexesOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

/**
 * Lists the indexes available.
 */
public class AzureAISearchListIndexesOpDispenser extends AzureAISearchBaseOpDispenser<String> {
	public AzureAISearchListIndexesOpDispenser(AzureAISearchDriverAdapter adapter, ParsedOp op,
			LongFunction<String> targetF) {
		super(adapter, op, targetF);
	}

	@Override
	public LongFunction<String> getParamFunc(LongFunction<SearchIndexClient> clientF, ParsedOp op,
			LongFunction<String> targetF) {
		return l -> targetF.apply(l);
	}

	@Override
	public LongFunction<AzureAISearchBaseOp<String>> createOpFunc(LongFunction<String> paramF,
			LongFunction<SearchIndexClient> clientF, ParsedOp op, LongFunction<String> targetF) {
		return l -> new AzureAISearchListIndexesOp(clientF.apply(l), paramF.apply(l));
	}
}