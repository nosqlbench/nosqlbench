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

package io.nosqlbench.adapter.qdrant.types;

public enum QdrantOpType {
    create_collection,
    delete_collection,
    create_payload_index,
    // https://qdrant.github.io/qdrant/redoc/index.html#tag/points/operation/search_points
    search_points,
    // https://qdrant.tech/documentation/concepts/points/
    // https://qdrant.github.io/qdrant/redoc/index.html#tag/points/operation/upsert_points
    upsert_points,
    // https://qdrant.github.io/qdrant/redoc/index.html#tag/points/operation/count_points
    // https://qdrant.tech/documentation/concepts/points/#counting-points
    count_points,
    list_collections,
    collection_info,
    collection_exists,
    list_collection_aliases,
    list_snapshots,
}
