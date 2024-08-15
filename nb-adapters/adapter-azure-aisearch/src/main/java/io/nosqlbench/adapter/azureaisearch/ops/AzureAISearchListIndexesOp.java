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

import com.azure.core.http.rest.PagedIterable;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.SearchIndex;

public class AzureAISearchListIndexesOp extends AzureAISearchBaseOp<String> {

	public AzureAISearchListIndexesOp(SearchIndexClient client, String request) {
		super(client, request);
	}

	@Override
	public Object applyOp(long value) {
		try {
			PagedIterable<SearchIndex> response = searchIndexClient.listIndexes();
			response.forEach((index) -> {
				logger.info(() -> "Indexes available are: Name: " + index.getName() + ",  ETag: " + index.getETag());
				index.getFields().forEach(field -> {
					logger.info(() -> "Field Name: " + field.getName() + ", Field isKey?: " + field.isKey()
							+ ", Field Dimension: " + field.getVectorSearchDimensions()
							+ ", Field Vector Search Profile: " + field.getVectorSearchProfileName());
				});
			});
		} catch (RuntimeException rte) {
			throw rte;
		}
		return "Listed indexes";
	}

}
