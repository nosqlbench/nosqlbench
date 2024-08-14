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

import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;

public class AzureAISearchSearchDocumentsOp extends AzureAISearchClientBaseOp<SearchOptions> {

	public AzureAISearchSearchDocumentsOp(SearchIndexClient searchIndexClient, SearchClient searchClient,
			SearchOptions request) {
		super(searchIndexClient, searchClient, request);
	}

	@Override
	public Object applyOp(long value) {
		SearchPagedIterable searchDocsResponse = null;
		try {
			searchDocsResponse = searchClient.search(null, // we've not implemented other complex searches yet here.
					request,
					Context.NONE);
			if (logger.isInfoEnabled()) {
				searchDocsResponse.forEach((r) -> {
					SearchDocument doc = r.getDocument(SearchDocument.class);
					logger.debug(
							"Successfully searched the index and returned id: {}, score: {}, vector embedding: {}",
							doc.get("id"), r.getScore(), doc.get("value"));
				});
			}
		} catch (RuntimeException rte) {
			throw rte;
		}
		return searchDocsResponse;
	}

}
