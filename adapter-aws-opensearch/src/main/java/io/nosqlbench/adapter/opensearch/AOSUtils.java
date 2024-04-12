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

package io.nosqlbench.adapter.opensearch;

import io.nosqlbench.adapter.opensearch.pojos.Doc;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

public class AOSUtils {

    public static int[] DocHitsToIntIndicesArray(SearchResponse<Doc> response) {
        int[] indices = response.hits().hits()
            .stream()
            .map(Hit::source)
            .mapToInt(doc -> Integer.parseInt(doc.getKey()))
            .toArray();
        return indices;
    }
}
