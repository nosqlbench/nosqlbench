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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.VectorQuery;
import com.azure.search.documents.models.VectorSearchOptions;
import com.azure.search.documents.models.VectorizedQuery;

import com.azure.search.documents.util.SearchPagedIterable;
import io.nosqlbench.adapter.azureaisearch.AzureAISearchDriverAdapter;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchBaseOp;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchSearchDocumentsOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

/**
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/rest/api/searchservice/documents/search-get?view=rest-searchservice-2024-07-01&tabs=HTTP#rawvectorquery">
 *      Search GET API<a/>
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/azure/search/vector-search-how-to-query?tabs=query-2024-07-01%2Cfilter-2024-07-01%2Cbuiltin-portal#vector-query-request">How
 *      to query/vector search</a>
 */
public class AzureAISearchSearchDocumentsOpDispenser extends AzureAISearchBaseOpDispenser<SearchOptions, SearchPagedIterable> {
	public AzureAISearchSearchDocumentsOpDispenser(AzureAISearchDriverAdapter adapter, ParsedOp op,
			LongFunction<String> targetF) {
		super(adapter, op, targetF);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public LongFunction<SearchOptions> getParamFunc(LongFunction<SearchIndexClient> clientF, ParsedOp op,
			LongFunction<String> targetF) {
		LongFunction<SearchOptions> ebF = l -> new SearchOptions();

		Optional<LongFunction<Boolean>> countFunc = op.getAsOptionalFunction("count", Boolean.class);
		if (countFunc.isPresent()) {
			final LongFunction<SearchOptions> countLF = ebF;
			ebF = l -> countLF.apply(l).setIncludeTotalCount(countFunc.get().apply(l));
		}

		LongFunction<String> selectFunc = op.getAsRequiredFunction("select", String.class);
		final LongFunction<SearchOptions> selectLF = ebF;
		ebF = l -> selectLF.apply(l).setSelect(selectFunc.apply(l));

		final LongFunction<SearchOptions> vqLF = ebF;
		ebF = l -> vqLF.apply(l).setVectorSearchOptions(buildVectorSearchOptionsStruct(op).apply(l));

		final LongFunction<SearchOptions> lastF = ebF;
		return l -> lastF.apply(l);
	}

	@Override
	public LongFunction<AzureAISearchBaseOp<SearchOptions,SearchPagedIterable>> createOpFunc(LongFunction<SearchOptions> paramF,
			LongFunction<SearchIndexClient> clientF, ParsedOp op, LongFunction<String> targetF) {
		return l -> new AzureAISearchSearchDocumentsOp(clientF.apply(l),
				clientF.apply(l).getSearchClient(targetF.apply(l)), paramF.apply(l));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LongFunction<VectorSearchOptions> buildVectorSearchOptionsStruct(ParsedOp op) {
		if (!op.isDefined("vectorQueries")) {
            throw new OpConfigError("Must provide values for 'vectorQueries' in 'search_documents' op");
        }
        Optional<LongFunction<Map>> baseFunc = op.getAsOptionalFunction("vectorQueries", Map.class);
		return baseFunc.<LongFunction<VectorSearchOptions>>map(mapLongFunc -> l -> {
			Map<String, Object> vsoMap = mapLongFunc.apply(l);
			VectorSearchOptions vsOpts = new VectorSearchOptions();
			if (!vsoMap.containsKey("vector")) {
				throw new OpConfigError(
						"Must provide list of float values for 'vector' field within 'vectorQueries' of 'search_documents' op");
			}
			VectorQuery vectorizableQuery = new VectorizedQuery((List<Float>) vsoMap.get("vector"));
			if (vsoMap.containsKey("exhaustive"))
				vectorizableQuery.setExhaustive((Boolean) vsoMap.get("exhaustive"));
			if (vsoMap.containsKey("fields"))
				vectorizableQuery.setFields(new String[] { (String) vsoMap.get("fields") });
			if (vsoMap.containsKey("weight"))
				vectorizableQuery.setWeight(((Number) vsoMap.get("weight")).floatValue());
			if (vsoMap.containsKey("k"))
				vectorizableQuery.setKNearestNeighborsCount(((Number) vsoMap.get("k")).intValue());
			vsOpts.setQueries(vectorizableQuery);
			return vsOpts;
		}).orElse(null);
	}
}
