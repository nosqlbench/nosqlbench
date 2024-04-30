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

package io.nosqlbench.adapter.qdrant.opdispensers;

import io.nosqlbench.adapter.qdrant.QdrantDriverAdapter;
import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapter.qdrant.ops.QdrantCreateCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class QdrantCreateCollectionOpDispenser extends QdrantBaseOpDispenser<CreateCollection> {
    private static final Logger logger = LogManager.getLogger(QdrantCreateCollectionOpDispenser.class);

    /**
     * Create a new QdrantCreateCollectionOpDispenser subclassed from {@link QdrantBaseOpDispenser}.
     *
     * @param adapter        The associated {@link QdrantDriverAdapter}
     * @param op             The {@link ParsedOp} encapsulating the activity for this cycle
     * @param targetFunction A LongFunction that returns the specified Qdrant Index for this Op
     * @see <a href="https://qdrant.github.io/qdrant/redoc/index.html#tag/collections/operation/create_collection">Qdrant Create Collection</a>.
     */
    public QdrantCreateCollectionOpDispenser(QdrantDriverAdapter adapter,
                                             ParsedOp op,
                                             LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<CreateCollection> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        LongFunction<CreateCollection.Builder> ebF =
            l -> CreateCollection.newBuilder().setCollectionName(targetF.apply(l));

//        VectorParams namedVectorsMap = buildNamedVectorsStruct(
//            op.getAsSubOps("vectors", ParsedOp.SubOpNaming.SubKey)
//        );
//        final LongFunction<CreateCollection.Builder> namedVectorsF = ebF;
//        ebF = l -> namedVectorsF.apply(l).setVectorsConfig(VectorsConfig.newBuilder().setParams(namedVectorsMap));

        Map<String, VectorParams> namedVectorParamsMap = buildNamedVectorsStruct(
            op.getAsSubOps("vectors", ParsedOp.SubOpNaming.SubKey)
        );
        final LongFunction<CreateCollection.Builder> namedVectorsF = ebF;
        ebF = l -> namedVectorsF.apply(l).setVectorsConfig(VectorsConfig.newBuilder().setParamsMap(
            VectorParamsMap.newBuilder().putAllMap(namedVectorParamsMap).build()));

        ebF = op.enhanceFuncOptionally(ebF, "on_disk_payload", Boolean.class,
            CreateCollection.Builder::setOnDiskPayload);
        ebF = op.enhanceFuncOptionally(ebF, "shard_number", Number.class,
            (CreateCollection.Builder b, Number n) -> b.setShardNumber(n.intValue()));
        ebF = op.enhanceFuncOptionally(ebF, "replication_factor", Number.class,
            (CreateCollection.Builder b, Number n) -> b.setReplicationFactor(n.intValue()));
        ebF = op.enhanceFuncOptionally(ebF, "write_consistency_factor", Number.class,
            (CreateCollection.Builder b, Number n) -> b.setWriteConsistencyFactor(n.intValue()));
        ebF = op.enhanceFuncOptionally(ebF, "init_from", String.class,
            CreateCollection.Builder::setInitFromCollection);
        ebF = op.enhanceFuncOptionally(ebF, "sharding_method", String.class,
            (CreateCollection.Builder b, String s) -> b.setShardingMethod(ShardingMethod.valueOf(s)));

        WalConfigDiff walConfig = buildWalConfigDiff(op);
        final LongFunction<CreateCollection.Builder> walConfigF = ebF;
        ebF = l -> walConfigF.apply(l).setWalConfig(walConfig);

        OptimizersConfigDiff ocDiff = buildOptimizerConfigDiff(op);
        final LongFunction<CreateCollection.Builder> ocF = ebF;
        ebF = l -> ocF.apply(l).setOptimizersConfig(ocDiff);

        HnswConfigDiff hnswConfigDiff = buildHnswConfigDiff(op);
        final LongFunction<CreateCollection.Builder> hnswConfigF = ebF;
        ebF = l -> hnswConfigF.apply(l).setHnswConfig(hnswConfigDiff);

        QuantizationConfig qcDiff = buildQuantizationConfig(op);
        if (qcDiff != null) {
            final LongFunction<CreateCollection.Builder> qcConfigF = ebF;
            ebF = l -> qcConfigF.apply(l).setQuantizationConfig(qcDiff);
        }

        SparseVectorConfig sparseVectorsMap = buildSparseVectorsStruct(
            op.getAsSubOps("sparse_vectors", ParsedOp.SubOpNaming.SubKey)
        );
        final LongFunction<CreateCollection.Builder> sparseVectorsF = ebF;
        ebF = l -> sparseVectorsF.apply(l).setSparseVectorsConfig(sparseVectorsMap);

        final LongFunction<CreateCollection.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    /**
     * Build the {@link OptimizersConfigDiff} from the provided {@link ParsedOp}.
     *
     * @param op {@link ParsedOp} containing the optimizer config data.
     * @return {@link OptimizersConfigDiff} containing the optimizer config data
     */
    private OptimizersConfigDiff buildOptimizerConfigDiff(ParsedOp op) {
        OptimizersConfigDiff.Builder ocDiffBuilder = OptimizersConfigDiff.newBuilder();
        op.getOptionalStaticValue("optimizers_config", Map.class).ifPresent(ocData -> {
            if (ocData.isEmpty()) {
                return;
            } else {
                if (ocData.containsKey("deleted_threshold")) {
                    ocDiffBuilder.setDeletedThreshold(((Number) ocData.get("deleted_threshold")).doubleValue());
                }
                if (ocData.containsKey("vacuum_min_vector_number")) {
                    ocDiffBuilder.setVacuumMinVectorNumber(((Number) ocData.get("vacuum_min_vector_number")).longValue());
                }
                if (ocData.containsKey("default_segment_number")) {
                    ocDiffBuilder.setDefaultSegmentNumber(((Number) ocData.get("default_segment_number")).longValue());
                }
                if (ocData.containsKey("max_segment_size")) {
                    ocDiffBuilder.setMaxSegmentSize(((Number) ocData.get("max_segment_size")).longValue());
                }
                if (ocData.containsKey("memmap_threshold")) {
                    ocDiffBuilder.setMemmapThreshold(((Number) ocData.get("memmap_threshold")).longValue());
                }
                if (ocData.containsKey("indexing_threshold")) {
                    ocDiffBuilder.setIndexingThreshold(((Number) ocData.get("indexing_threshold")).longValue());
                }
                if (ocData.containsKey(("flush_interval_sec"))) {
                    ocDiffBuilder.setFlushIntervalSec(((Number) ocData.get("flush_interval_sec")).longValue());
                }
                if (ocData.containsKey("max_optimization_threads")) {
                    ocDiffBuilder.setMaxOptimizationThreads(((Number) ocData.get("max_optimization_threads")).intValue());
                }
            }
        });
        return ocDiffBuilder.build();
    }

    /**
     * Build the {@link WalConfigDiff} from the provided {@link ParsedOp}.
     *
     * @param op {@link ParsedOp} containing the WAL config data.
     * @return {@link WalConfigDiff} containing the WAL config data
     */
    private WalConfigDiff buildWalConfigDiff(ParsedOp op) {
        WalConfigDiff.Builder walConfigDiffBuilder = WalConfigDiff.newBuilder();
        op.getOptionalStaticValue("wal_config", Map.class).ifPresent(walConfigData -> {
            if (walConfigData.isEmpty()) {
                return;
            } else {
                if (walConfigData.containsKey("wal_capacity_mb")) {
                    walConfigDiffBuilder.setWalCapacityMb(((Number) walConfigData.get("wal_capacity_mb")).longValue());
                }
                if (walConfigData.containsKey("wal_segments_ahead")) {
                    walConfigDiffBuilder.setWalSegmentsAhead(((Number) walConfigData.get("wal_segments_ahead")).longValue());
                }
            }
        });
        return walConfigDiffBuilder.build();
    }

    /**
     * Only named vectors are supported at this time in this driver.
     *
     * @param {@link Map<String, ParsedOp>} namedVectorsData
     * @return {@link VectorParams} containing the named vectors
     */
    private Map<String, VectorParams>/*VectorParams*/ buildNamedVectorsStruct(Map<String, ParsedOp> namedVectorsData) {
//        if (namedVectorsData.size() != 1) {
//            // TODO - we need this form somehow to support the mapped version
//            // https://github.com/qdrant/java-client/blob/v1.9.0/src/main/java/io/qdrant/client/QdrantClient.java#L232-L243
//            throw new UnsupportedOperationException("Empty or more than one named vectors are not supported at this time");
//        }
        Map<String, VectorParams> namedVectors = new HashMap<>();
        VectorParams.Builder builder = VectorParams.newBuilder();
        namedVectorsData.forEach((name, fieldSpec) -> {
            builder.setDistanceValue(fieldSpec.getStaticValue("distance_value", Number.class).intValue());
            builder.setSize(fieldSpec.getStaticValue("size", Number.class).longValue());
            fieldSpec.getOptionalStaticValue("on_disk", Boolean.class)
                .ifPresent(builder::setOnDisk);
            fieldSpec.getOptionalStaticValue("datatype_value", Number.class)
                .ifPresent((Number value) -> builder.setDatatypeValue(value.intValue()));

            builder.setHnswConfig(buildHnswConfigDiff(fieldSpec));
            builder.setQuantizationConfig(buildQuantizationConfig(fieldSpec));

            namedVectors.put(name, builder.build());
        });
        //return builder.build();
        return namedVectors;
    }

    /**
     * Build the {@link QuantizationConfig} from the provided {@link ParsedOp}.
     *
     * @param fieldSpec The {@link ParsedOp} containing the quantization config data
     * @return The {@link QuantizationConfig} built from the provided {@link ParsedOp}
     * @see <a href="https://qdrant.tech/documentation/guides/quantization/#setting-up-quantization-in-qdrant">Quantization Config</a>
     */
    private QuantizationConfig buildQuantizationConfig(ParsedOp fieldSpec) {
        QuantizationConfig.Builder qcBuilder = QuantizationConfig.newBuilder();
        fieldSpec.getOptionalStaticValue("quantization_config", Map.class).ifPresent(qcData -> {
            if (qcData.isEmpty()) {
                return;
            } else {
                // TODO - Approach #1 - feels ugly
                Arrays.asList("binary", "product", "scalar")
                    .forEach(key -> {
                        if (qcData.containsKey(key)) {
                            switch (key) {
                                case "binary":
                                    BinaryQuantization.Builder binaryBuilder = BinaryQuantization.newBuilder();
                                    Map<?, ?> binaryQCData = (Map<?, ?>) qcData.get("binary");
                                    if (null != binaryQCData && !binaryQCData.isEmpty()) {
                                        if (binaryQCData.containsKey("always_ram")) {
                                            binaryBuilder.setAlwaysRam((Boolean) binaryQCData.get("always_ram"));
                                        }
                                    }
                                    qcBuilder.setBinary(binaryBuilder);
                                    break;
                                case "product":
                                    ProductQuantization.Builder productBuilder = ProductQuantization.newBuilder();
                                    Map<?, ?> productQCData = (Map<?, ?>) qcData.get("product");
                                    if (null != productQCData && !productQCData.isEmpty()) {
                                        // Mandatory field
                                        productBuilder.setAlwaysRam((Boolean) productQCData.get("always_ram"));
                                        // Optional field(s) below
                                        if (productQCData.containsKey("compression")) {
                                            productBuilder.setCompression(CompressionRatio.valueOf((String) productQCData.get("compression")));
                                        }
                                    }
                                    qcBuilder.setProduct(productBuilder);
                                    break;
                                case "scalar":
                                    ScalarQuantization.Builder scalarBuilder = ScalarQuantization.newBuilder();
                                    Map<?, ?> scalarQCData = (Map<?, ?>) qcData.get("scalar");
                                    if (null != scalarQCData && !scalarQCData.isEmpty()) {
                                        // Mandatory field
                                        scalarBuilder.setType(QuantizationType.forNumber(((Number) scalarQCData.get("type")).intValue()));
                                        // Optional field(s) below
                                        if (scalarQCData.containsKey("always_ram")) {
                                            scalarBuilder.setAlwaysRam((Boolean) scalarQCData.get("always_ram"));
                                        }
                                        if (scalarQCData.containsKey("quantile")) {
                                            scalarBuilder.setQuantile(((Number) scalarQCData.get("quantile")).floatValue());
                                        }
                                    }
                                    qcBuilder.setScalar(scalarBuilder);
                                    break;
                            }
                        }
                    });
                // TODO - Approach #2 - equally feels ugly too.
//                if (qcData.containsKey("binary")) {
//                    if (qcData.containsKey("scalar") || qcData.containsKey("product")) {
//                        throw new UnsupportedOperationException("Only one of binary, scalar, or product can be specified for quantization config");
//                    }
//                    BinaryQuantization.Builder binaryBuilder = BinaryQuantization.newBuilder();
//                    Map<?, ?> binaryQCData = (Map<?, ?>) qcData.get("binary");
//                    if (null != binaryQCData && !binaryQCData.isEmpty()) {
//                        if (binaryQCData.containsKey("always_ram")) {
//                            binaryBuilder.setAlwaysRam((Boolean) binaryQCData.get("always_ram"));
//                        }
//                    }
//                    qcBuilder.setBinary(binaryBuilder);
//                } else if (qcData.containsKey("product")) {
//                    if (qcData.containsKey("binary") || qcData.containsKey("scalar")) {
//                        throw new UnsupportedOperationException("Only one of binary, scalar, or product can be specified for quantization config");
//                    }
//                    ProductQuantization.Builder productBuilder = ProductQuantization.newBuilder();
//                    Map<?, ?> productQCData = (Map<?, ?>) qcData.get("product");
//                    if (null != productQCData && !productQCData.isEmpty()) {
//                        // Mandatory field
//                        productBuilder.setAlwaysRam((Boolean) productQCData.get("always_ram"));
//                        // Optional field(s) below
//                        if (productQCData.containsKey("compression")) {
//                            productBuilder.setCompression(CompressionRatio.valueOf((String) productQCData.get("compression")));
//                        }
//                    }
//                    qcBuilder.setProduct(productBuilder);
//                } else if (qcData.containsKey("scalar")) {
//                    if (qcData.containsKey("binary") || qcData.containsKey("product")) {
//                        throw new UnsupportedOperationException("Only one of binary, scalar, or product can be specified for quantization config");
//                    }
//                    ScalarQuantization.Builder scalarBuilder = ScalarQuantization.newBuilder();
//                    Map<?, ?> scalarQCData = (Map<?, ?>) qcData.get("scalar");
//                    if (null != scalarQCData && !scalarQCData.isEmpty()) {
//                        // Mandatory field
//                        scalarBuilder.setType(QuantizationType.valueOf((String) scalarQCData.get("type")));
//                        // Optional field(s) below
//                        if (scalarQCData.containsKey("always_ram")) {
//                            scalarBuilder.setAlwaysRam((Boolean) scalarQCData.get("always_ram"));
//                        }
//                        if (scalarQCData.containsKey("quantile")) {
//                            scalarBuilder.setQuantile((Float) scalarQCData.get("quantile"));
//                        }
//                    }
//                    qcBuilder.setScalar(scalarBuilder);
//                }
            }
        });

        // The below check is required to avoid INVALID_ARGUMENT: Unable to convert quantization config
        if (qcBuilder.hasBinary() || qcBuilder.hasProduct() || qcBuilder.hasScalar()) {
            return qcBuilder.build();
        }
        return null;
    }

    /**
     * Build the {@link HnswConfigDiff} from the provided {@link ParsedOp}.
     *
     * @param fieldSpec The {@link ParsedOp} containing the hnsw config data
     * @return The {@link HnswConfigDiff} built from the provided {@link ParsedOp}
     * @see <a href="https://qdrant.tech/documentation/concepts/indexing/#vector-index">HNSW Config</a>
     */
    private HnswConfigDiff buildHnswConfigDiff(ParsedOp fieldSpec) {
        HnswConfigDiff.Builder hnswConfigBuilder = HnswConfigDiff.newBuilder();
        fieldSpec.getOptionalStaticValue("hnsw_config", Map.class).ifPresent(hnswConfigData -> {
            if (hnswConfigData.isEmpty()) {
                return;
            } else {
                if (hnswConfigData.containsKey("ef_construct")) {
                    hnswConfigBuilder.setEfConstruct(((Number) hnswConfigData.get("ef_construct")).longValue());
                }
                if (hnswConfigData.containsKey("m")) {
                    hnswConfigBuilder.setM(((Number) hnswConfigData.get("m")).intValue());
                }
                if (hnswConfigData.containsKey("full_scan_threshold")) {
                    hnswConfigBuilder.setFullScanThreshold(((Number) hnswConfigData.get("full_scan_threshold")).intValue());
                }
                if (hnswConfigData.containsKey("max_indexing_threads")) {
                    hnswConfigBuilder.setMaxIndexingThreads(((Number) hnswConfigData.get("max_indexing_threads")).intValue());
                }
                if (hnswConfigData.containsKey("on_disk")) {
                    hnswConfigBuilder.setOnDisk((Boolean) hnswConfigData.get("on_disk"));
                }
                if (hnswConfigData.containsKey("payload_m")) {
                    hnswConfigBuilder.setPayloadM(((Number) hnswConfigData.get("payload_m")).intValue());
                }
            }
        });
//        if (hnswConfigBuilder.hasM() || hnswConfigBuilder.hasEfConstruct() || hnswConfigBuilder.hasOnDisk()
//            || hnswConfigBuilder.hasPayloadM() || hnswConfigBuilder.hasFullScanThreshold()
//            || hnswConfigBuilder.hasMaxIndexingThreads()) {
        return hnswConfigBuilder.build();
//        }
//        return null;
    }

    /**
     * Build the {@link SparseVectorConfig} from the provided {@link ParsedOp}.
     *
     * @param sparseVectorsData The {@link ParsedOp} containing the sparse vectors data
     * @return The {@link SparseVectorConfig} built from the provided {@link ParsedOp}
     */
    private SparseVectorConfig buildSparseVectorsStruct(Map<String, ParsedOp> sparseVectorsData) {
        SparseVectorConfig.Builder builder = SparseVectorConfig.newBuilder();
        sparseVectorsData.forEach((name, fieldSpec) -> {
            SparseVectorParams.Builder svpBuilder = SparseVectorParams.newBuilder();
            SparseIndexConfig.Builder sicBuilder = SparseIndexConfig.newBuilder();

            fieldSpec.getOptionalStaticValue("full_scan_threshold", Number.class)
                .ifPresent((Number value) -> sicBuilder.setFullScanThreshold(value.intValue()));
            fieldSpec.getOptionalStaticValue("on_disk", Boolean.class)
                .ifPresent(sicBuilder::setOnDisk);

            svpBuilder.setIndex(sicBuilder);
            builder.putMap(name, svpBuilder.build());
        });
        return builder.build();
    }

    // https://qdrant.tech/documentation/concepts/collections/#create-a-collection
    @Override
    public LongFunction<QdrantBaseOp<CreateCollection>> createOpFunc(
        LongFunction<CreateCollection> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new QdrantCreateCollectionOp(clientF.apply(l), paramF.apply(l));
    }
}
