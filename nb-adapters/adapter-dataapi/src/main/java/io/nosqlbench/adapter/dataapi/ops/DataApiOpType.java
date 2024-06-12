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

package io.nosqlbench.adapter.dataapi.ops;

public enum DataApiOpType {
    create_collection,
    insert_many,
    insert_one,
    insert_one_vector,
    find,
    find_one,
    find_one_and_delete,
    find_one_and_update,
    find_vector,
    find_vector_filter,
    update_one,
    update_many,
    delete_one,
    delete_many,
    delete_collection,
    list_collections,
    list_collection_names,
    estimated_document_count,
    find_by_id,
    find_distinct,
    count_documents,
    replace_one,
    find_one_and_replace,
}
