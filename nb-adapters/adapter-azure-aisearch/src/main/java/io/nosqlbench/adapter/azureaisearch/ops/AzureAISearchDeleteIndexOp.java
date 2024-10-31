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

import com.azure.search.documents.indexes.SearchIndexClient;

public class AzureAISearchDeleteIndexOp extends AzureAISearchBaseOp<String,String> {

	public AzureAISearchDeleteIndexOp(SearchIndexClient client, String request) {
		super(client, request);
	}

	@Override
	public String applyOp(long value) {
		try {
			searchIndexClient.deleteIndex(request);
			logger.debug("Successfully deleted the index: {}", request);
		} catch (RuntimeException rte) {
			throw rte;
		}
		return "Deleted";
	}

}
