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

import java.util.List;

import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.IndexDocumentsResult;

public class AzureAISearchUploadDocumentsOp extends AzureAISearchClientBaseOp<SearchDocument> {

	public AzureAISearchUploadDocumentsOp(SearchIndexClient searchIndexClient, SearchClient searchClient,
			SearchDocument request) {
		super(searchIndexClient, searchClient, request);
	}

	@Override
	public Object applyOp(long value) {
		IndexDocumentsResult uploadDocsResponse = null;
		try {
			uploadDocsResponse = searchClient.uploadDocuments(List.of(request));
			if (logger.isDebugEnabled()) {
				uploadDocsResponse.getResults().forEach((r) -> {
					logger.debug(
							"Successfully created the collection with return status code: {}, key: {}, succeeded?: {}, error message: {}",
							r.getStatusCode(), r.getKey(), r.isSucceeded(), r.getErrorMessage());
				});
			}
		} catch (RuntimeException rte) {
			throw rte;
		}
		return uploadDocsResponse;
	}

}
