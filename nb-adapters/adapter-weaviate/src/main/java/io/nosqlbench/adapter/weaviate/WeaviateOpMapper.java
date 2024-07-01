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

package io.nosqlbench.adapter.weaviate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateBaseOpDispenser;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateCreateCollectionOpDispenser;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateCreateObjectsOpDispenser;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateDeleteCollectionOpDispenser;
import io.nosqlbench.adapter.weaviate.opsdispensers.WeaviateGetCollectionSchemaOpDispenser;
import io.nosqlbench.adapter.weaviate.types.WeaviateOpType;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;

public class WeaviateOpMapper implements OpMapper<WeaviateBaseOp<?>> {
	private static final Logger logger = LogManager.getLogger(WeaviateOpMapper.class);
	private final WeaviateDriverAdapter adapter;

	/**
	 * Create a new WeaviateOpMapper implementing the {@link OpMapper} interface.
	 *
	 * @param adapter The associated {@link WeaviateDriverAdapter}
	 */
	public WeaviateOpMapper(WeaviateDriverAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * Given an instance of a {@link ParsedOp} returns the appropriate
	 * {@link WeaviateBaseOpDispenser} subclass
	 *
	 * @param op The {@link ParsedOp} to be evaluated
	 * @return The correct {@link WeaviateBaseOpDispenser} subclass based on the op
	 *         type
	 */
	@Override
	public OpDispenser<? extends WeaviateBaseOp<?>> apply(ParsedOp op) {
		TypeAndTarget<WeaviateOpType, String> typeAndTarget = op.getTypeAndTarget(WeaviateOpType.class, String.class,
				"type", "target");
		logger.info(() -> "Using '" + typeAndTarget.enumId + "' op type for op template '" + op.getName() + "'");

		return switch (typeAndTarget.enumId) {
		case delete_collection -> new WeaviateDeleteCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
		case create_collection -> new WeaviateCreateCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
		case get_collection_schema ->
			new WeaviateGetCollectionSchemaOpDispenser(adapter, op, typeAndTarget.targetFunction);
		case create_objects -> new WeaviateCreateObjectsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case create_index -> new WeaviateCreateIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case drop_index -> new WeaviateDropIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            // Uses the Collection-specific fields (columnar) insert mode
//            case insert_rows -> new WeaviateInsertRowsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            // Uses the High-Level row-by-row JSONObject (tabular) insert mode
//            case insert -> new WeaviateInsertOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case delete -> new WeaviateDeleteOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case search -> new WeaviateSearchOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case alter_alias -> new WeaviateAlterAliasOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case alter_collection -> new WeaviateAlterCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case flush -> new WeaviateFlushOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case bulk_insert -> new WeaviateBulkInsertOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case create_alias -> new WeaviateCreateAliasOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case get -> new WeaviateGetOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case create_partition -> new WeaviateCreatePartitionOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case create_credential -> new WeaviateCreateCredentialOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case create_database -> new WeaviateCreateDatabaseOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case query -> new WeaviateQueryOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case delete_credential -> new WeaviateDeleteCredentialOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case describe_collection ->
//                new WeaviateDescribeCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case describe_index -> new WeaviateDescribeIndexOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case drop_alias -> new WeaviateDropAliasOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case get_metrics -> new WeaviateGetMetricsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case drop_database -> new WeaviateDropDatabaseOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case get_replicas -> new WeaviateGetReplicasOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case load_balance -> new WeaviateLoadBalanceOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case has_partition -> new WeaviateHasPartitionOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case drop_partition -> new WeaviateDropPartitionOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case get_load_state -> new WeaviateGetLoadStateOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case list_databases -> new WeaviateListDatabasesOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case manual_compact -> new WeaviateManualCompactOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case get_index_state -> new WeaviateGetIndexStateOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case list_cred_users -> new WeaviateListCredUsersOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case load_collection -> new WeaviateLoadCollectionOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case show_partitions -> new WeaviateShowPartitionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case load_partitions -> new WeaviateLoadPartitionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case list_collections -> new WeaviateListCollectionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case show_collections -> new WeaviateShowCollectionsOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case update_credential -> new WeaviateUpdateCredentialOpDispenser(adapter, op, typeAndTarget.targetFunction);
//            case release_collection -> new WeaviateReleaseCollectionOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_bulk_insert_state -> new WeaviateGetBulkInsertStateOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case release_partitions -> new WeaviateReleasePartitionsOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_flush_all_state -> new WeaviateGetFlushAllStateOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_compaction_state -> new WeaviateGetCompactionStateOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_loading_progress -> new WeaviateGetLoadingProgressOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_persistent_segment_info -> new WeaviateGetPersistentSegmentInfoOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_query_segment_info -> new WeaviateGetQuerySegmentInfoOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case list_bulk_insert_tasks -> new WeaviateListBulkInsertTasksOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_index_build_progress -> new WeaviateGetIndexBuildProgressOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_partition_statistics -> new WeaviateGetPartitionStatisticsOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_collection_statistics -> new WeaviateGetCollectionStatisticsOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            case get_compaction_state_with_plans -> new WeaviateGetCompactionStateWithPlansOpDispenser(adapter, op,
//                typeAndTarget.targetFunction);
//            default -> throw new RuntimeException("Unrecognized op type '" + typeAndTarget.enumId.name() + "' while " +
//                "mapping parsed op " + op);
		};
	}
}

