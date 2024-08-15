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

package io.nosqlbench.adapter.azureaisearch.ops;


import java.util.function.LongFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.azure.search.documents.indexes.SearchIndexClient;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

public abstract class AzureAISearchBaseOp<T> implements CycleOp<Object> {

	protected final static Logger logger = LogManager.getLogger(AzureAISearchBaseOp.class);

	protected final SearchIndexClient searchIndexClient;
	protected final T request;
	protected final LongFunction<Object> apiCall;

	public AzureAISearchBaseOp(SearchIndexClient searchIndexClient, T requestParam) {
		this.searchIndexClient = searchIndexClient;
		this.request = requestParam;
		this.apiCall = this::applyOp;
	}

	public AzureAISearchBaseOp(SearchIndexClient searchIndexClient, T requestParam, LongFunction<Object> call) {
		this.searchIndexClient = searchIndexClient;
		this.request = requestParam;
		this.apiCall = call;
	}

	@Override
	public final Object apply(long value) {
		logger.trace(() -> "applying op: " + this);

		try {
			Object result = applyOp(value);

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
		return "AzureAISearchBaseOp(" + this.request.getClass().getSimpleName() + ")";
	}
}
