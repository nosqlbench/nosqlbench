/*
 * Copyright (c) 2023-2024 nosqlbench
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

package io.nosqlbench.adapter.milvus.types;

import static io.nosqlbench.adapter.milvus.types.MilvusOpGroup.*;

public enum MilvusOpType {

    alter_alias(Alias),
    create_alias(Alias),
    drop_alias(Alias),

    create_credential(Authentication),
    delete_credential(Authentication),
    list_cred_users(Authentication),
    update_credential(Authentication),
    bulk_insert(BulkInsert),
    get_bulk_insert_state(BulkInsert),
    list_bulk_insert_tasks(BulkInsert),


    alter_collection(Collection),
    create_collection(Collection),
    delete(Collection),
    describe_collection(Collection),
    drop_collection(Collection),
    flush(Collection),
    get_collection_statistics(Collection),
    get_load_state(Collection),
    get_loading_progress(Collection),
    get_persistent_segment_info(Collection),
    get_query_segment_info(Collection),
    get_replicas(Collection),
    insert_rows(Collection),  // Added because of generic signature causing ambiguous patterns on insert(...)
    insert(Collection),
    load_collection(Collection),
    release_collection(Collection),
    show_collections(Collection),
    create_database(Database),
    drop_database(Database),
    list_databases(Database),
    get(HighLevel),
    list_collections(HighLevel),
    create_index(Index),
    describe_index(Index),
    drop_index(Index),
    get_index_build_progress(Index),
    get_index_state(Index),
    get_compaction_state(Management),
    get_compaction_state_with_plans(Management),
    get_flush_all_state(Management),
    get_metrics(Management),
    load_balance(Management),
    manual_compact(Management),
    create_partition(Partition),
    drop_partition(Partition),
    get_partition_statistics(Partition),
    has_partition(Partition),
    load_partitions(Partition),
    release_partitions(Partition),
    show_partitions(Partition),
    query(QueryAndSearch),
    search(QueryAndSearch),
//    add_user_to_role(RBAC),
//    create_role(RBAC),
//    drop_role(RBAC),
//    grant_role_privilege(RBAC),
//    remove_user_from_role(RBAC),
//    revoke_role_privilege(RBAC),
//    select_grant_for_role(RBAC),
//    select_grant_for_role_and_object(RBAC),
//    select_role(RBAC),
//    select_user(RBAC),
    ;
    private final MilvusOpGroup group;

    MilvusOpType(MilvusOpGroup group) {
        this.group = group;
    }

}
