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

import io.nosqlbench.adapter.milvus.opdispensers.*;
import io.nosqlbench.adapter.milvus.ops.MilvusBaseOp;
import io.nosqlbench.adapter.milvus.types.MilvusOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MilvusOpMapper implements OpMapper<MilvusBaseOp<?>> {
    private static final Logger logger = LogManager.getLogger(MilvusOpMapper.class);
    private final MilvusDriverAdapter adapter;

    /**
     * Create a new MilvusOpMapper implementing the {@link OpMapper} interface.
     *
     * @param adapter The associated {@link MilvusDriverAdapter}
     */
    public MilvusOpMapper(MilvusDriverAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Given an instance of a {@link ParsedOp} returns the appropriate {@link MilvusBaseOpDispenser} subclass
     *
     * @param op The {@link ParsedOp} to be evaluated
     * @return The correct {@link MilvusBaseOpDispenser} subclass based on the op type
     */
    @Override
    public OpDispenser<? extends MilvusBaseOp<?>> apply(ParsedOp op) {
        TypeAndTarget<MilvusOpType, String> typeAndTarget = op.getTypeAndTarget(
            MilvusOpType.class,
            String.class,
            "type",
            "target"
        );
        logger.info(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");

        return switch (typeAndTarget.enumId) {
            case drop_collection -> new MilvusDropCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_collection -> new MilvusCreateCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_index -> new MilvusCreateIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case drop_index -> new MilvusDropIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            // Uses the Collection-specific fields (columnar) insert mode
            case insert_rows -> new MilvusInsertRowsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            // Uses the High-Level row-by-row JSONObject (tabular) insert mode
            case insert -> new MilvusInsertOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case delete -> new MilvusDeleteOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case search -> new MilvusSearchOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case alter_alias -> new MilvusAlterAliasOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case alter_collection -> new MilvusAlterCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case flush -> new MilvusFlushOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case bulk_insert -> new MilvusBulkInsertOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_alias -> new MilvusCreateAliasOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case get -> new MilvusGetOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_partition -> new MilvusCreatePartitionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_credential -> new MilvusCreateCredentialOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case create_database -> new MilvusCreateDatabaseOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case query -> new MilvusQueryOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case delete_credential -> new MilvusDeleteCredentialOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case describe_collection ->
                new MilvusDescribeCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case describe_index -> new MilvusDescribeIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case drop_alias -> new MilvusDropAliasOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case get_metrics -> new MilvusGetMetricsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case drop_database -> new MilvusDropDatabaseOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case get_replicas -> new MilvusGetReplicasOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case load_balance -> new MilvusLoadBalanceOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case has_partition -> new MilvusHasPartitionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case drop_partition -> new MilvusDropPartitionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case get_load_state -> new MilvusGetLoadStateOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case list_databases -> new MilvusListDatabasesOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case manual_compact -> new MilvusManualCompactOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case get_index_state -> new MilvusGetIndexStateOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case list_cred_users -> new MilvusListCredUsersOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case load_collection -> new MilvusLoadCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case show_partitions -> new MilvusShowPartitionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case load_partitions -> new MilvusLoadPartitionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case list_collections -> new MilvusListCollectionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case show_collections -> new MilvusShowCollectionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case update_credential -> new MilvusUpdateCredentialOpDispenser(adapter, op, typeAndTarget.targetFunction);
            case release_collection -> new MilvusReleaseCollectionOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_bulk_insert_state -> new MilvusGetBulkInsertStateOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case release_partitions -> new MilvusReleasePartitionsOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_flush_all_state -> new MilvusGetFlushAllStateOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_compaction_state -> new MilvusGetCompactionStateOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_loading_progress -> new MilvusGetLoadingProgressOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_persistent_segment_info -> new MilvusGetPersistentSegmentInfoOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_query_segment_info -> new MilvusGetQuerySegmentInfoOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case list_bulk_insert_tasks -> new MilvusListBulkInsertTasksOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_index_build_progress -> new MilvusGetIndexBuildProgressOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_partition_statistics -> new MilvusGetPartitionStatisticsOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_collection_statistics -> new MilvusGetCollectionStatisticsOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
            case get_compaction_state_with_plans -> new MilvusGetCompactionStateWithPlansOpDispenser(adapter, op,
                typeAndTarget.targetFunction);
//            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
//                "mapping parsed op " + op);
        };
    }
}

