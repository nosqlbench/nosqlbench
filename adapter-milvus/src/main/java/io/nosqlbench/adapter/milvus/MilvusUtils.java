/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.milvus;

import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.response.SearchResultsWrapper;

import java.util.List;

public class MilvusUtils {

    public static int[] intArrayFromMilvusSearchResults(String fieldname, R<SearchResults> result) {

        SearchResultsWrapper wrapper = new SearchResultsWrapper(result.getData().getResults());
        List<String> fieldData = (List<String>) wrapper.getFieldData(fieldname, 0);
        int[] indices = new int[fieldData.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i]=Integer.parseInt(fieldData.get(i));
        }
        return indices;

    }
}
