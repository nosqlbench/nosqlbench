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
import io.nosqlbench.adapter.qdrant.ops.QdrantSearchPointsOp;
import io.nosqlbench.adapter.qdrant.pojos.SearchPointsHelper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ShardKeySelectorFactory;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class QdrantSearchPointsOpDispenser extends QdrantBaseOpDispenser<SearchPoints,List<ScoredPoint>> {
    public QdrantSearchPointsOpDispenser(QdrantDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public LongFunction<QdrantBaseOp<SearchPoints,List<ScoredPoint>>> createOpFunc(
        LongFunction<SearchPoints> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op, LongFunction<String> targetF) {
        return l -> new QdrantSearchPointsOp(clientF.apply(l), paramF.apply(l));
    }

    @Override
    public LongFunction<SearchPoints> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF) {
        LongFunction<SearchPoints.Builder> ebF =
            l -> SearchPoints.newBuilder().setCollectionName(targetF.apply(l));

        // query params here
        ebF = op.enhanceFuncOptionally(ebF, "timeout", Number.class,
            (SearchPoints.Builder b, Number t) -> b.setTimeout(t.longValue()));
        Optional<LongFunction<Object>> optionalConsistencyF = op.getAsOptionalFunction("consistency", Object.class);
        if (optionalConsistencyF.isPresent()) {
            LongFunction<SearchPoints.Builder> consistencyFunc = ebF;
            LongFunction<ReadConsistency> builtConsistency = buildReadConsistency(optionalConsistencyF.get());
            ebF = l -> consistencyFunc.apply(l).setReadConsistency(builtConsistency.apply(l));
        }

        // body params here
        // - required items
        ebF = op.enhanceFuncOptionally(ebF, "limit", Number.class,
            (SearchPoints.Builder b, Number n) -> b.setLimit(n.longValue()));

        LongFunction<SearchPointsHelper> searchPointsHelperF = buildVectorForSearch(op);
        final LongFunction<SearchPoints.Builder> detailsOfNamedVectorsF = ebF;
        ebF = l -> detailsOfNamedVectorsF.apply(l)
            .setVectorName(searchPointsHelperF.apply(l).getVectorName())
            .addAllVector(searchPointsHelperF.apply(l).getVectorValues());
        //.setSparseIndices(searchPointsHelperF.apply(l).getSparseIndices()); throws NPE at Qdrant driver and hence below
        final LongFunction<SearchPoints.Builder> sparseIndicesF = ebF;
        ebF = l -> {
            SearchPoints.Builder builder = sparseIndicesF.apply(l);
            if (searchPointsHelperF.apply(l).getSparseIndices() != null) {
                builder.setSparseIndices(searchPointsHelperF.apply(l).getSparseIndices());
            }
            return builder;
        };

        // - optional items
        ebF = op.enhanceFuncOptionally(ebF, "shard_key", String.class, (SearchPoints.Builder b, String sk) ->
            b.setShardKeySelector(ShardKeySelectorFactory.shardKeySelector(sk)));
        ebF = op.enhanceFuncOptionally(ebF, "score_threshold", Number.class,
            (SearchPoints.Builder b, Number n) -> b.setScoreThreshold(n.floatValue()));
        ebF = op.enhanceFuncOptionally(ebF, "offset", Number.class,
            (SearchPoints.Builder b, Number n) -> b.setOffset(n.longValue()));

        Optional<LongFunction<Object>> optionalWithPayloadF = op.getAsOptionalFunction("with_payload", Object.class);
        if (optionalWithPayloadF.isPresent()) {
            LongFunction<SearchPoints.Builder> withPayloadFunc = ebF;
            LongFunction<WithPayloadSelector> builtWithPayload = buildWithPayloadSelector(optionalWithPayloadF.get());
            ebF = l -> withPayloadFunc.apply(l).setWithPayload(builtWithPayload.apply(l));
        }

        Optional<LongFunction<Object>> optionalWithVectorF = op.getAsOptionalFunction("with_vector", Object.class);
        if (optionalWithVectorF.isPresent()) {
            LongFunction<SearchPoints.Builder> withVectorFunc = ebF;
            LongFunction<WithVectorsSelector> builtWithVector = buildWithVectorSelector(optionalWithVectorF.get());
            ebF = l -> withVectorFunc.apply(l).setWithVectors(builtWithVector.apply(l));
        }

        Optional<LongFunction<Map>> optionalParams = op.getAsOptionalFunction("params", Map.class);
        if (optionalParams.isPresent()) {
            LongFunction<SearchPoints.Builder> paramsF = ebF;
            LongFunction<SearchParams> params = buildSearchParams(optionalParams.get());
            ebF = l -> paramsF.apply(l).setParams(params.apply(l));
        }

        LongFunction<Filter.Builder> filterBuilder = getFilterFromOp(op);
        if (filterBuilder != null) {
            final LongFunction<SearchPoints.Builder> filterF = ebF;
            ebF = l -> filterF.apply(l).setFilter(filterBuilder.apply(l));
        }

        final LongFunction<SearchPoints.Builder> lastF = ebF;
        return l -> lastF.apply(l).build();
    }

    private LongFunction<SearchPointsHelper> buildVectorForSearch(ParsedOp op) {
        if (!op.isDefined("vector")) {
            throw new OpConfigError("Must provide values for 'vector'");
        }
        Optional<LongFunction<List>> baseFunc = op.getAsOptionalFunction("vector", List.class);
        return baseFunc.<LongFunction<SearchPointsHelper>>map(listLongFunction -> l -> {
            List<Map<String, Object>> vectorPointsList = listLongFunction.apply(l);
            SearchPointsHelper searchPointsHelperBuilder = new SearchPointsHelper();
            vectorPointsList.forEach(point -> {
                if (point.containsKey("name")) {
                    searchPointsHelperBuilder.setVectorName((String) point.get("name"));
                } else {
                    throw new OpConfigError("Must provide values for 'name' within 'vector' field");
                }
                if (point.containsKey("values")) {
                    searchPointsHelperBuilder.setVectorValues((List<Float>) point.get("values"));
                } else {
                    throw new OpConfigError("Must provide values for 'values' within 'vector' field");
                }
                if (point.containsKey("sparse_indices")) {
                    searchPointsHelperBuilder.setSparseIndices(
                        SparseIndices.newBuilder().addAllData((List<Integer>) point.get("sparse_indices")).build());
                }
            });
            return searchPointsHelperBuilder;
        }).orElse(null);
    }

    private LongFunction<WithVectorsSelector> buildWithVectorSelector(LongFunction<Object> objectLongFunction) {
        return l -> {
            Object withVector = objectLongFunction.apply(l);
            switch (withVector) {
                case Boolean b -> {
                    return WithVectorsSelectorFactory.enable(b);
                }
                case List<?> objects when objects.getFirst() instanceof String -> {
                    return WithVectorsSelectorFactory.include((List<String>) withVector);
                }
                case null, default -> {
                    assert withVector != null;
                    throw new OpConfigError("Invalid type for with_vector specified [{}]" +
                        withVector.getClass().getSimpleName());
                }
            }
        };
    }

    private LongFunction<WithPayloadSelector> buildWithPayloadSelector(LongFunction<Object> objectLongFunction) {
        return l -> {
            Object withPayload = objectLongFunction.apply(l);
            switch (withPayload) {
                case Boolean b -> {
                    return WithPayloadSelector.newBuilder().setEnable(b).build();
                }
                case List<?> objects when objects.getFirst() instanceof String -> {
                    return WithPayloadSelectorFactory.include((List<String>) withPayload);
                }
                case Map<?, ?> map -> {
                    WithPayloadSelector.Builder withPayloadSelector = WithPayloadSelector.newBuilder();
                    map.forEach((key, value) -> {
                        if (key.equals("include")) {
                            withPayloadSelector.setInclude(
                                PayloadIncludeSelector.newBuilder().addAllFields((List<String>) value).build());
                        } else if (key.equals("exclude")) {
                            withPayloadSelector.setExclude(
                                PayloadExcludeSelector.newBuilder().addAllFields((List<String>) value).build());
                        } else {
                            throw new OpConfigError("Only 'include' & 'exclude' fields for with_payload map is supported," +
                                " but we got [{}]" + key);
                        }
                    });

                    return withPayloadSelector.build();
                }
                case null, default -> {
                    assert withPayload != null;
                    throw new OpConfigError("Invalid type for with_payload specified [{}]" +
                        withPayload.getClass().getSimpleName());
                }
            }
        };
    }

    /**
     * @param objectLongFunction the {@link LongFunction<Object>} from which the consistency for search will be built.
     * @return a {@link ReadConsistency} function object to be added to a Qdrant {@link UpsertPoints} request.
     * <p>
     * This method interrogates the subsection of the ParsedOp defined for vector parameters and constructs a list of
     * vector (dense plus sparse) points based on the included values, or returns null if this section is not populated.
     * The base function returns either the List of vectors or null, while the interior function builds the vectors
     * with a Builder pattern based on the values contained in the source ParsedOp.
     */
    private LongFunction<ReadConsistency> buildReadConsistency(LongFunction<Object> objectLongFunction) {
        return l -> {
            Object consistency = objectLongFunction.apply(l);
            if (consistency instanceof Number) {
                return ReadConsistency.newBuilder().setTypeValue((Integer) consistency).build();
            } else if (consistency instanceof String) {
                return ReadConsistency.newBuilder().setType(ReadConsistencyType.valueOf((String) consistency)).build();
            } else {
                throw new OpConfigError("Invalid type for read consistency specified");
            }
        };
    }

    private LongFunction<SearchParams> buildSearchParams(LongFunction<Map> mapLongFunction) {
        return l -> {
            SearchParams.Builder searchParamsBuilder = SearchParams.newBuilder();
            mapLongFunction.apply(l).forEach((key, val) -> {
                if ("hnsw_config".equals(key)) {
                    searchParamsBuilder.setHnswEf(((Number) val).longValue());
                }
                if ("exact".equals(key)) {
                    searchParamsBuilder.setExact((Boolean) val);
                }
                if ("indexed_only".equals(key)) {
                    searchParamsBuilder.setIndexedOnly((Boolean) val);
                }
                if ("quantization".equals(key)) {
                    QuantizationSearchParams.Builder qsBuilder = QuantizationSearchParams.newBuilder();
                    ((Map<String, Object>) val).forEach((qKey, qVal) -> {
                        if ("ignore".equals(qKey)) {
                            qsBuilder.setIgnore((Boolean) qVal);
                        }
                        if ("rescore".equals(qKey)) {
                            qsBuilder.setRescore((Boolean) qVal);
                        }
                        if ("oversampling".equals(qKey)) {
                            qsBuilder.setOversampling(((Number) qVal).doubleValue());
                        }
                    });
                    searchParamsBuilder.setQuantization(qsBuilder);
                }
            });
            return searchParamsBuilder.build();
        };
    }
}
