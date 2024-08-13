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

import java.util.Map;
import java.util.function.LongFunction;

import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.indexes.SearchIndexClient;

import io.nosqlbench.adapter.azureaisearch.AzureAISearchDriverAdapter;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchBaseOp;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchUploadDocumentsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;

/**
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/rest/api/searchservice/documents/?view=rest-searchservice-2024-07-01&tabs=HTTP">API
 *      Reference</a>
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/java/api/com.azure.search.documents?view=azure-java-stable">Index
 *      docs</a>
 */
public class AzureAISearchUploadDocumentsOpDispenser extends AzureAISearchBaseOpDispenser<SearchDocument> {
	public AzureAISearchUploadDocumentsOpDispenser(AzureAISearchDriverAdapter adapter, ParsedOp op,
			LongFunction<String> targetF) {
		super(adapter, op, targetF);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public LongFunction<SearchDocument> getParamFunc(LongFunction<SearchIndexClient> clientF, ParsedOp op,
			LongFunction<String> targetF) {
		LongFunction<SearchDocument> ebF = l -> new SearchDocument();

		LongFunction<Map> fieldsMapF = op.getAsRequiredFunction("fields", Map.class);
		final LongFunction<SearchDocument> fieldF = buildFieldsStruct(fieldsMapF);
		ebF = l -> fieldF.apply(l);

		final LongFunction<SearchDocument> lastF = ebF;
		return l -> lastF.apply(l);
	}

	@Override
	public LongFunction<AzureAISearchBaseOp<SearchDocument>> createOpFunc(LongFunction<SearchDocument> paramF,
			LongFunction<SearchIndexClient> clientF, ParsedOp op, LongFunction<String> targetF) {
		return l -> new AzureAISearchUploadDocumentsOp(clientF.apply(l),
				clientF.apply(l).getSearchClient(targetF.apply(l)), paramF.apply(l));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LongFunction<SearchDocument> buildFieldsStruct(LongFunction<Map> fieldsFunction) {
		return l -> {
			Map<String, Object> fields = fieldsFunction.apply(l);
			var doc = new SearchDocument();
			fields.forEach((key, val) -> {
				doc.put(key, val);
			});
			return doc;
		};
	}

}
