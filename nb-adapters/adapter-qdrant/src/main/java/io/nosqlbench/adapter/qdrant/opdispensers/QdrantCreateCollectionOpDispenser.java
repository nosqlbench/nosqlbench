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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

import io.qdrant.client.grpc.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.nosqlbench.adapter.qdrant.QdrantDriverAdapter;
import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapter.qdrant.ops.QdrantCreateCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.BinaryQuantization;
import io.qdrant.client.grpc.Collections.CompressionRatio;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.HnswConfigDiff;
import io.qdrant.client.grpc.Collections.OptimizersConfigDiff;
import io.qdrant.client.grpc.Collections.ProductQuantization;
import io.qdrant.client.grpc.Collections.QuantizationConfig;
import io.qdrant.client.grpc.Collections.QuantizationType;
import io.qdrant.client.grpc.Collections.ScalarQuantization;
import io.qdrant.client.grpc.Collections.ShardingMethod;
import io.qdrant.client.grpc.Collections.SparseIndexConfig;
import io.qdrant.client.grpc.Collections.SparseVectorConfig;
import io.qdrant.client.grpc.Collections.SparseVectorParams;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorParamsMap;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.Collections.WalConfigDiff;

public class QdrantCreateCollectionOpDispenser extends QdrantBaseOpDispenser<CreateCollection, Collections.CollectionOperationResponse> {
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

        LongFunction<Map<String, VectorParams>> namedVectorParamsMap = buildNamedVectorsStruct(op);
        final LongFunction<CreateCollection.Builder> namedVectorsF = ebF;
        ebF = l -> namedVectorsF.apply(l).setVectorsConfig(VectorsConfig.newBuilder().setParamsMap(
            VectorParamsMap.newBuilder().putAllMap(namedVectorParamsMap.apply(l)).build()));

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

        Optional<LongFunction<Map>> walF = op.getAsOptionalFunction("wal_config", Map.class);
        if (walF.isPresent()) {
            final LongFunction<CreateCollection.Builder> wallFunc = ebF;
            LongFunction<WalConfigDiff> wcdF = buildWalConfigDiff(walF.get());
            ebF = l -> wallFunc.apply(l).setWalConfig(wcdF.apply(l));
        }

        Optional<LongFunction<Map>> optConDifF = op.getAsOptionalFunction("optimizers_config", Map.class);
        if (optConDifF.isPresent()) {
            final LongFunction<CreateCollection.Builder> wallFunc = ebF;
            LongFunction<OptimizersConfigDiff> ocdF = buildOptimizerConfigDiff(optConDifF.get());
            ebF = l -> wallFunc.apply(l).setOptimizersConfig(ocdF.apply(l));
        }

        Optional<LongFunction<Map>> hnswConfigDiffF = op.getAsOptionalFunction("hnsw_config", Map.class);
        if (hnswConfigDiffF.isPresent()) {
            final LongFunction<CreateCollection.Builder> hnswConfigF = ebF;
            LongFunction<HnswConfigDiff> hcdF = buildHnswConfigDiff(hnswConfigDiffF.get());
            ebF = l -> hnswConfigF.apply(l).setHnswConfig(hcdF.apply(l));
        }

        Optional<LongFunction<Map>> quantConfigF = op.getAsOptionalFunction("quantization_config", Map.class);
        if (quantConfigF.isPresent()) {
            final LongFunction<CreateCollection.Builder> qConF = ebF;
            LongFunction<QuantizationConfig> qcDiffF = buildQuantizationConfig(quantConfigF.get());
            ebF = l -> qConF.apply(l).setQuantizationConfig(qcDiffF.apply(l));
        }

        Optional<LongFunction<Map>> sparseVectorsF = op.getAsOptionalFunction("sparse_vectors", Map.class);
        if (sparseVectorsF.isPresent()) {
            final LongFunction<CreateCollection.Builder> sparseVecF = ebF;
            LongFunction<SparseVectorConfig> sparseVectorsMap = buildSparseVectorsStruct(sparseVectorsF.get());
            ebF = l -> sparseVecF.apply(l).setSparseVectorsConfig(sparseVectorsMap.apply(l));
        }

        final LongFunction<CreateCollection.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    /**
     * Build the {@link OptimizersConfigDiff} from the provided {@link ParsedOp}.
     *
     * @param ocdMapLongFunc {@link LongFunction<Map>} containing the optimizer config data.
     * @return {@link OptimizersConfigDiff} containing the optimizer config data
     */
    private LongFunction<OptimizersConfigDiff> buildOptimizerConfigDiff(LongFunction<Map> ocdMapLongFunc) {
        return l -> {
            OptimizersConfigDiff.Builder ocDiffBuilder = OptimizersConfigDiff.newBuilder();
            ocdMapLongFunc.apply(l).forEach((key, value) -> {
                    if (key.equals("deleted_threshold")) {
                        ocDiffBuilder.setDeletedThreshold(((Number) value).doubleValue());
                    }
                    if (key.equals("vacuum_min_vector_number")) {
                        ocDiffBuilder.setVacuumMinVectorNumber(((Number) value).longValue());
                    }
                    if (key.equals("default_segment_number")) {
                        ocDiffBuilder.setDefaultSegmentNumber(((Number) value).longValue());
                    }
                    if (key.equals("max_segment_size")) {
                        ocDiffBuilder.setMaxSegmentSize(((Number) value).longValue());
                    }
                    if (key.equals("memmap_threshold")) {
                        ocDiffBuilder.setMemmapThreshold(((Number) value).longValue());
                    }
                    if (key.equals("indexing_threshold")) {
                        ocDiffBuilder.setIndexingThreshold(((Number) value).longValue());
                    }
                    if (key.equals(("flush_interval_sec"))) {
                        ocDiffBuilder.setFlushIntervalSec(((Number) value).longValue());
                    }
                    if (key.equals("max_optimization_threads")) {
                        ocDiffBuilder.setMaxOptimizationThreads(((Number) value).intValue());
                    }
                }
            );
            return ocDiffBuilder.build();
        };
    }

    /**
     * Build the {@link WalConfigDiff} from the provided {@link ParsedOp}.
     *
     * @param mapLongFunction {@link LongFunction<Map>} containing the WAL config data.
     * @return {@link LongFunction<WalConfigDiff>} containing the WAL config data
     */
    private LongFunction<WalConfigDiff> buildWalConfigDiff(LongFunction<Map> mapLongFunction) {
        return l -> {
            WalConfigDiff.Builder walConfigDiffBuilder = WalConfigDiff.newBuilder();
            mapLongFunction.apply(l).forEach((key, value) -> {
                    if (key.equals("wal_capacity_mb")) {
                        walConfigDiffBuilder.setWalCapacityMb(((Number) value).longValue());
                    }
                    if (key.equals("wal_segments_ahead")) {
                        walConfigDiffBuilder.setWalSegmentsAhead(((Number) value).longValue());
                    }
                }
            );
            return walConfigDiffBuilder.build();
        };
    }

    /**
     * Only named vectors are supported at this time in this driver.
     *
     * @param {@link ParsedOp} op
     * @return {@link LongFunction<Map<String, VectorParams>>} containing the named vectors
     */
    private LongFunction<Map<String, VectorParams>> buildNamedVectorsStruct(ParsedOp op) {
        if (!op.isDefined("vectors")) {
            throw new OpConfigError("Must provide values for 'vectors' in 'create_collection' op");
        }
        Optional<LongFunction<Map>> baseFunc = op.getAsOptionalFunction("vectors", Map.class);
        return baseFunc.<LongFunction<Map<String, VectorParams>>>map(mapLongFunc -> l -> {
            Map<String, Object> nvMap = mapLongFunc.apply(l);
            Map<String, VectorParams> namedVectors = new HashMap<>();
            nvMap.forEach((name, value) -> {
                VectorParams.Builder builder = VectorParams.newBuilder();
                if (value instanceof Map) {
                    ((Map<String, Object>) value).forEach((innerKey, innerValue) -> {
                        if (innerKey.equals("distance_value")) {
                            builder.setDistanceValue(((Number) innerValue).intValue());
                        }
                        if (innerKey.equals("size")) {
                            builder.setSize(((Number) innerValue).longValue());
                        }
                        if (innerKey.equals("on_disk")) {
                            builder.setOnDisk((Boolean) innerValue);
                        }
                        if (innerKey.equals("datatype_value")) {
                            builder.setDatatypeValue(((Number) innerValue).intValue());
                        }
                        if (innerKey.equals("hnsw_config")) {
                            builder.setHnswConfig(buildHnswConfigDiff((Map<String, Object>) innerValue));
                        }
                        if (innerKey.equals("quantization_config")) {
                            builder.setQuantizationConfig(buildQuantizationConfig((Map<String, Object>) innerValue));
                        }
                    });
                } else {
                    throw new OpConfigError("Named vectors must be a Map<String, Map<String, Object>>, but got "
                        + value.getClass().getSimpleName() + " instead for the inner value");
                }
                namedVectors.put(name, builder.build());
            });
            return namedVectors;
        }).orElse(null);
    }

    private LongFunction<QuantizationConfig> buildQuantizationConfig(LongFunction<Map> quantConfMapLongFunc) {
        return l -> this.buildQuantizationConfig(quantConfMapLongFunc.apply(l));
    }

    private QuantizationConfig buildQuantizationConfig(Map<String, Object> quantConfMap) {
        QuantizationConfig.Builder qcBuilder = QuantizationConfig.newBuilder();
        quantConfMap.forEach((key, value) -> {
            switch (key) {
                case "binary" -> {
                    BinaryQuantization.Builder binaryBuilder = BinaryQuantization.newBuilder();
                    Map<?, ?> binaryQCData = (Map<?, ?>) value;
                    if (null != binaryQCData && !binaryQCData.isEmpty()) {
                        if (binaryQCData.containsKey("always_ram")) {
                            binaryBuilder.setAlwaysRam((Boolean) binaryQCData.get("always_ram"));
                        }
                        qcBuilder.setBinary(binaryBuilder);
                    }
                }
                case "product" -> {
                    ProductQuantization.Builder productBuilder = ProductQuantization.newBuilder();
                    Map<?, ?> productQCData = (Map<?, ?>) value;
                    if (null != productQCData && !productQCData.isEmpty()) {
                        // Mandatory field
                        productBuilder.setAlwaysRam((Boolean) productQCData.get("always_ram"));
                        // Optional field(s) below
                        if (productQCData.containsKey("compression")) {
                            productBuilder.setCompression(CompressionRatio.valueOf((String) productQCData.get("compression")));
                        }
                        qcBuilder.setProduct(productBuilder);
                    }
                }
                case "scalar" -> {
                    ScalarQuantization.Builder scalarBuilder = ScalarQuantization.newBuilder();
                    Map<?, ?> scalarQCData = (Map<?, ?>) value;
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
                        qcBuilder.setScalar(scalarBuilder);
                    }
                }
            }
        });
        return qcBuilder.build();
    }

    private LongFunction<HnswConfigDiff> buildHnswConfigDiff(LongFunction<Map> hnswConfigDiffMapLongFunc) {
        return l -> this.buildHnswConfigDiff(hnswConfigDiffMapLongFunc.apply(l));
    }

    /**
     * Build the {@link HnswConfigDiff} from the provided {@link ParsedOp}.
     *
     * @param hnswConfigDiffMap The {@link Map<String, Object>} containing the hnsw config data
     * @return The {@link LongFunction<HnswConfigDiff>} built from the provided {@link ParsedOp}
     * @see <a href="https://qdrant.tech/documentation/concepts/indexing/#vector-index">HNSW Config</a>
     */
    private HnswConfigDiff buildHnswConfigDiff(Map<String, Object> hnswConfigDiffMap) {
        HnswConfigDiff.Builder hnswConfigBuilder = HnswConfigDiff.newBuilder();
        hnswConfigDiffMap.forEach((key, value) -> {
                if (key.equals("ef_construct")) {
                    hnswConfigBuilder.setEfConstruct(((Number) value).longValue());
                }
                if (key.equals("m")) {
                    hnswConfigBuilder.setM(((Number) value).intValue());
                }
                if (key.equals("full_scan_threshold")) {
                    hnswConfigBuilder.setFullScanThreshold(((Number) value).intValue());
                }
                if (key.equals("max_indexing_threads")) {
                    hnswConfigBuilder.setMaxIndexingThreads(((Number) value).intValue());
                }
                if (key.equals("on_disk")) {
                    hnswConfigBuilder.setOnDisk((Boolean) value);
                }
                if (key.equals("payload_m")) {
                    hnswConfigBuilder.setPayloadM(((Number) value).intValue());
                }
            }
        );
        return hnswConfigBuilder.build();
    }

    /**
     * Build the {@link SparseVectorConfig} from the provided {@link ParsedOp}.
     *
     * @param sparseVectorsMapLongFunc The {@link LongFunction<Map>} containing the sparse vectors data
     * @return The {@link LongFunction<SparseVectorConfig>} built from the provided {@link ParsedOp}'s data
     */
    private LongFunction<SparseVectorConfig> buildSparseVectorsStruct(LongFunction<Map> sparseVectorsMapLongFunc) {
        return l -> {
            SparseVectorConfig.Builder builder = SparseVectorConfig.newBuilder();
            sparseVectorsMapLongFunc.apply(l).forEach((key, value) -> {
                SparseVectorParams.Builder svpBuilder = SparseVectorParams.newBuilder();
                SparseIndexConfig.Builder sicBuilder = SparseIndexConfig.newBuilder();
                if (value instanceof Map) {
                    ((Map<String, Object>) value).forEach((innerKey, innerValue) -> {
                            if (innerKey.equals("full_scan_threshold")) {
                                sicBuilder.setFullScanThreshold(((Number) innerValue).intValue());
                            }
                            if (innerKey.equals("on_disk")) {
                                sicBuilder.setOnDisk((Boolean) innerValue);
                            }
                            svpBuilder.setIndex(sicBuilder);
                            builder.putMap((String) key, svpBuilder.build());
                        }
                    );
                } else {
                    throw new OpConfigError("Sparse Vectors must be a Map<String, Map<String, Object>>, but got "
                        + value.getClass().getSimpleName() + " instead for the inner value");
                }
            });
            return builder.build();
        };
    }

    // https://qdrant.tech/documentation/concepts/collections/#create-a-collection
    @Override
    public LongFunction<QdrantBaseOp<CreateCollection, Collections.CollectionOperationResponse>> createOpFunc(
        LongFunction<CreateCollection> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    ) {
        return l -> new QdrantCreateCollectionOp(clientF.apply(l), paramF.apply(l));
    }
}
