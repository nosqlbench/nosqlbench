/*
 * Copyright (c) nosqlbench
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
    // LEGACY OPS
    // admin ops:
    create_database,
    list_databases,
    drop_database,
    get_database_info,
    // db-admin ops:
    create_namespace,
    list_namespaces,
    drop_namespace,
    // in-database ops:
    create_collection,
    create_collection_with_class,
    delete_collection,
    list_collections,
    list_collection_names,
    // in-collection ops:
    insert_many,
    insert_one,
    insert_one_vector,
    find,
    find_distinct,
    find_one,
    find_vector,
    find_vector_filter,
    find_by_id,
    update_one,
    find_one_and_update,
    update_many,
    delete_one,
    find_one_and_delete,
    delete_many,
    delete_all,
    replace_one,
    find_one_and_replace,
    estimated_document_count,
    count_documents,
    // NEW-STYLE OPS
    // admin ops:
    // db-admin ops:
    // in-database ops:
    // in-collection ops:
    collection_find_one_and_update,
}
