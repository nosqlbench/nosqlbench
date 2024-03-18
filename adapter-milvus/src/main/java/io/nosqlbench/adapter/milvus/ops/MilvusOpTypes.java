/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapter.milvus.ops;

public enum MilvusOpTypes {
    drop_collection,
    create_index,

    drop_index,
    insert,
//    update,
    search,
    delete,

//    alter_alias,
//    create_alias,
//    drop_alias,

//    create_credential,
//    delete_credential,
//    list_cred_users,
//    update_credential,

//    bulk_insert,
//    get_bulk_insert_state,
//    list_bulk_insert_tasks,

    create_collection,
//    alter_collection,
//    describe_collection,
//    upsert,
//    describeindexstats,
//    fetch
}
