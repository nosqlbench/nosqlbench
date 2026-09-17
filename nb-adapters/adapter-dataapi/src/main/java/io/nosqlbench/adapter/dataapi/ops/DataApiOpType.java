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

    // TO-DO OPS
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
    delete_collection,
    list_collections,
    list_collection_names,
    // in-collection ops:
    insert_many,
    
    // LEGACY OPS
    // admin ops:
    // db-admin ops:
    // in-database ops:
    create_collection_with_class,
    // in-collection ops:
    insert_one,
    insert_one_vector,
    delete_one,
    delete_many,
    delete_all,
    find_one_and_delete,
    find,
    find_vector,
    find_vector_filter,
    find_by_id,
    find_distinct,
    find_one,
    update_one,
    find_one_and_update,
    replace_one,
    find_one_and_replace,
    update_many,
    estimated_document_count,
    count_documents,

    // NEW-STYLE OPS
    // admin ops:
    // db-admin ops:
    // in-database ops:
    // in-collection ops:
    collection_insert_one,
    collection_delete_many,
    collection_delete_one,
    collection_find_one_and_delete,
    collection_find,
    collection_find_one,
    collection_update_one,
    collection_find_one_and_update,
    collection_find_one_and_replace,
    collection_update_many,
    collection_estimated_document_count,
    collection_count_documents,
}
