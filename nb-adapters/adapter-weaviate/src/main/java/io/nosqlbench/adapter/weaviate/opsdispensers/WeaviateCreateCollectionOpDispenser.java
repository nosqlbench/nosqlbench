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
package io.nosqlbench.adapter.weaviate.opsdispensers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

import io.nosqlbench.adapter.weaviate.WeaviateDriverAdapter;
import io.nosqlbench.adapter.weaviate.ops.WeaviateBaseOp;
import io.nosqlbench.adapter.weaviate.ops.WeaviateCreateCollectionOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.misc.model.BQConfig;
import io.weaviate.client.v1.misc.model.BQConfig.BQConfigBuilder;
import io.weaviate.client.v1.misc.model.MultiTenancyConfig;
import io.weaviate.client.v1.misc.model.MultiTenancyConfig.MultiTenancyConfigBuilder;
import io.weaviate.client.v1.misc.model.PQConfig;
import io.weaviate.client.v1.misc.model.PQConfig.Encoder;
import io.weaviate.client.v1.misc.model.PQConfig.PQConfigBuilder;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.misc.model.ReplicationConfig.ReplicationConfigBuilder;
import io.weaviate.client.v1.misc.model.VectorIndexConfig;
import io.weaviate.client.v1.misc.model.VectorIndexConfig.VectorIndexConfigBuilder;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Property.PropertyBuilder;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.client.v1.schema.model.WeaviateClass.VectorConfig;
import io.weaviate.client.v1.schema.model.WeaviateClass.VectorConfig.VectorConfigBuilder;
import io.weaviate.client.v1.schema.model.WeaviateClass.WeaviateClassBuilder;;

/**
 * @see <a href=
 *      "https://weaviate.io/developers/weaviate/api/rest#tag/schema/post/schema">API
 *      Reference</a>
 * @see <a href=
 *      "https://weaviate.io/developers/weaviate/manage-data/collections">Collection
 *      docs</a>
 */
public class WeaviateCreateCollectionOpDispenser extends WeaviateBaseOpDispenser<WeaviateClass> {

	public WeaviateCreateCollectionOpDispenser(WeaviateDriverAdapter adapter, ParsedOp op,
			LongFunction<String> targetF) {
		super(adapter, op, targetF);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public LongFunction<WeaviateClass> getParamFunc(LongFunction<WeaviateClient> clientF, ParsedOp op,
			LongFunction<String> targetF) {

		LongFunction<WeaviateClassBuilder> ebF = l -> WeaviateClass.builder().className(targetF.apply(l));

		ebF = op.enhanceFuncOptionally(ebF, "description", String.class, WeaviateClassBuilder::description);
		ebF = op.enhanceFuncOptionally(ebF, "vectorizer", String.class, WeaviateClassBuilder::vectorizer);

		if (op.isDefined("properties")) {
			LongFunction<List<Property>> propertiesListF = buildPropertiesStruct(op);
			final LongFunction<WeaviateClassBuilder> propertiesF = ebF;
			ebF = l -> propertiesF.apply(l).properties(propertiesListF.apply(l));
		}

		Optional<LongFunction<Map>> rfF = op.getAsOptionalFunction("replicationConfig", Map.class);
		if (rfF.isPresent()) {
			final LongFunction<WeaviateClassBuilder> rfFunc = ebF;
			LongFunction<ReplicationConfig> repConfF = buildReplicationConfig(rfF.get());
			ebF = l -> rfFunc.apply(l).replicationConfig(repConfF.apply(l));
		}

		Optional<LongFunction<Map>> mtcF = op.getAsOptionalFunction("multiTenancyConfig", Map.class);
		if (mtcF.isPresent()) {
			final LongFunction<WeaviateClassBuilder> multiTCFunc = ebF;
			LongFunction<MultiTenancyConfig> multiTenantConfF = buildMultiTenancyConfig(mtcF.get());
			ebF = l -> multiTCFunc.apply(l).multiTenancyConfig(multiTenantConfF.apply(l));
		}

		ebF = op.enhanceFuncOptionally(ebF, "vectorIndexType", String.class, WeaviateClassBuilder::vectorIndexType);
		if(op.isDefined("vectorIndexConfig")) {
			LongFunction<VectorIndexConfig> vecIdxConfF = buildVectorIndexConfig(op);
			final LongFunction<WeaviateClassBuilder> vectorIndexConfigF = ebF;
			ebF = l -> vectorIndexConfigF.apply(l).vectorIndexConfig(vecIdxConfF.apply(l));
		}

		if (op.isDefined("vectorConfig")) {
			LongFunction<Map<String, VectorConfig>> vecConfF = buildVectorConfig(op);
			final LongFunction<WeaviateClassBuilder> vectorConfigFunc = ebF;
			ebF = l -> vectorConfigFunc.apply(l).vectorConfig(vecConfF.apply(l));
		}

		final LongFunction<WeaviateClassBuilder> lastF = ebF;
		return l -> lastF.apply(l).build();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LongFunction<Map<String, VectorConfig>> buildVectorConfig(ParsedOp op) {
		Optional<LongFunction<Map>> baseFunc = op.getAsOptionalFunction("vectorConfig", Map.class);
		return baseFunc.<LongFunction<Map<String, VectorConfig>>>map(mapLongFunc -> l -> {
			Map<String, Object> vcMap = mapLongFunc.apply(l);
			Map<String, VectorConfig> finalVecConf = new HashMap<>();
			vcMap.forEach((k, v) -> {
				if (v instanceof Map) {
					VectorConfigBuilder vcBdr = VectorConfig.builder();

					((Map<String, Object>) v).forEach((innerKey, innerVal) -> {
						if ("vectorizer".equals(innerKey)) {
							Map<String, Object> vectorizerMap = new HashMap<>(1);
							vectorizerMap.put((String) innerVal, (String) innerVal);
							vcBdr.vectorizer(vectorizerMap);
							// https://weaviate.io/developers/weaviate/api/rest#tag/schema/post/schema
							// Result status: WeaviateError(statusCode=422,
							// messages=[WeaviateErrorMessage(message=class.VectorConfig.Vectorizer must be
							// an object, got <nil>, throwable=null)])

							// WeaviateError(statusCode=422,
							// messages=[WeaviateErrorMessage(message=class.VectorConfig.Vectorizer must
							// consist only 1 configuration, got: 0, throwable=null)])
							logger.warn("For now, vectorizer is not properly implemented in named vector as its uncler");
						}
						if ("vectorIndexType".equals(innerKey)) {
							vcBdr.vectorIndexType((String) innerVal);
						}
						if ("vectorIndexConfig".equals(innerKey)) {
							vcBdr.vectorIndexConfig(extractVectorIndexConfig((Map<String, Object>) innerVal));
						}
					});

					finalVecConf.put(k, vcBdr.build());
				} else {
					throw new OpConfigError("Expected a map type for the value of '" + k
							+ "' named vector, but received " + v.getClass().getSimpleName());
				}
			});
			return finalVecConf;
		}).orElse(null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LongFunction<VectorIndexConfig> buildVectorIndexConfig(ParsedOp op) {
		Optional<LongFunction<Map>> baseFunc = op.getAsOptionalFunction("vectorIndexConfig", Map.class);
		return baseFunc.<LongFunction<VectorIndexConfig>>map(mapLongFunc -> l -> {
			Map<String, Object> nvMap = mapLongFunc.apply(l);
			return extractVectorIndexConfig(nvMap);
		}).orElse(null);
	}

	@SuppressWarnings({ "unchecked" })
	private VectorIndexConfig extractVectorIndexConfig(Map<String, Object> nvMap) {
		VectorIndexConfigBuilder vecIdxConfBuilder = VectorIndexConfig.builder();
		nvMap.forEach((key, value) -> {
			if (key.equals("cleanupIntervalSeconds")) {
				vecIdxConfBuilder.cleanupIntervalSeconds(((Number) value).intValue());
			}
			if (key.equals("distance")) {
				vecIdxConfBuilder.distance((String) value);
			}
			if (key.equals("ef")) {
				vecIdxConfBuilder.ef(((Number) value).intValue());
			}
			if (key.equals("efConstruction")) {
				vecIdxConfBuilder.efConstruction(((Number) value).intValue());
			}
			if (key.equals("maxConnections")) {
				vecIdxConfBuilder.maxConnections(((Number) value).intValue());
			}
			if (key.equals("dynamicEfMin")) {
				vecIdxConfBuilder.dynamicEfMin(((Number) value).intValue());
			}
			if (key.equals("dynamicEfMax")) {
				vecIdxConfBuilder.dynamicEfMax(((Number) value).intValue());
			}
			if (key.equals("dynamicEfFactor")) {
				vecIdxConfBuilder.dynamicEfFactor(((Number) value).intValue());
			}
			if (key.equals("flatSearchCutoff")) {
				vecIdxConfBuilder.flatSearchCutoff(((Number) value).intValue());
			}
			if (key.equals("skip")) {
				vecIdxConfBuilder.skip((Boolean) value);
			}
			if (key.equals("vectorCacheMaxObjects")) {
				vecIdxConfBuilder.vectorCacheMaxObjects(((Number) value).longValue());
			}
			if (key.equals("bq")) {
				BQConfigBuilder bqConfBuilder = BQConfig.builder();
				if (value instanceof Map<?, ?>) {
					((Map<String, Object>) value).forEach((bqKey, bqVal) -> {
						if (bqKey.equals("enabled")) {
							bqConfBuilder.enabled((Boolean) bqVal);
						}
						if (bqKey.equals("rescoreLimit")) {
							bqConfBuilder.rescoreLimit(((Number) bqVal).longValue());
						}
						if (bqKey.equals("cache")) {
							bqConfBuilder.cache((Boolean) bqVal);
						}
					});
				} else {
					throw new OpConfigError("Expected a map type for 'bq' leaf of the configuration");
				}
				vecIdxConfBuilder.bq(bqConfBuilder.build());
			}
			if (key.equals("pq")) {
				PQConfigBuilder pqConfBuilder = PQConfig.builder();
				if (value instanceof Map<?, ?>) {
					((Map<String, Object>) value).forEach((pqKey, pqVal) -> {
						if (pqKey.equals("enabled")) {
							pqConfBuilder.enabled((Boolean) pqVal);
						}
						if (pqKey.equals("rescoreLimit")) {
							pqConfBuilder.rescoreLimit(((Number) pqVal).longValue());
						}
						if (pqKey.equals("bitCompression")) {
							pqConfBuilder.bitCompression((Boolean) pqVal);
						}
						if (pqKey.equals("segments")) {
							pqConfBuilder.segments(((Number) pqVal).intValue());
						}
						if (pqKey.equals("centroids")) {
							pqConfBuilder.centroids(((Number) pqVal).intValue());
						}
						if (pqKey.equals("trainingLimit")) {
							pqConfBuilder.trainingLimit(((Number) pqVal).intValue());
						}
						if (pqKey.equals("cache")) {
							pqConfBuilder.cache((Boolean) pqVal);
						}
						if (pqKey.equals("encoder")) {
							PQConfig.Encoder.EncoderBuilder encBuilder = Encoder.builder();
							if (pqVal instanceof Map<?, ?>) {
								((Map<String, Object>) pqVal).forEach((encKey, encVal) -> {
									if (encKey.equals("type")) {
										encBuilder.type((String) encVal);
									}
									if (encKey.equals("distribution")) {
										encBuilder.distribution((String) encVal);
									}
								});
							} else {
								throw new OpConfigError(
										"Expected a map type for 'encoder's value of the configuration, but got "
												+ value.getClass().getSimpleName());
							}
							pqConfBuilder.encoder(encBuilder.build());
						}
					});
				} else {
					throw new OpConfigError("Expected a map type for 'bq' leaf of the configuration, but got "
							+ value.getClass().getSimpleName());
				}
				vecIdxConfBuilder.pq(pqConfBuilder.build());
			}
		});
		return vecIdxConfBuilder.build();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LongFunction<List<Property>> buildPropertiesStruct(ParsedOp op) {
//		if (!op.isDefined("properties")) {
//			throw new OpConfigError("Must provide values for 'properties' in 'create_collection' op");
//		}

		Optional<LongFunction<Map>> baseFunc = op.getAsOptionalFunction("properties", Map.class);
		return baseFunc.<LongFunction<List<Property>>>map(mapLongFunc -> l -> {
			Map<String, Object> nvMap = mapLongFunc.apply(l);
			List<Property> propertyList = new ArrayList<>();
			nvMap.forEach((name, value) -> {
				// TODO -- see if we need to set name at the higher (i.e. Property) level or
				// just at NetstedProperty level
				PropertyBuilder propBuilder = Property.builder().name(name);
				if (value instanceof Map) {
					((Map<String, Object>) value).forEach((innerKey, innerValue) -> {
						if (innerKey.equals("description")) {
							propBuilder.description((String) innerValue);
						}
						if (innerKey.equals("dataType")) {
							// logger.info(">>>>>>> property name '{}' has dataType '{}'", name,
							// innerValue);
							// https://weaviate.io/developers/weaviate/config-refs/datatypes#datatype-cross-reference
							// is unsupported at this time in NB driver
							propBuilder.dataType(Arrays.asList((String) innerValue));
						}
						if (innerKey.equals("tokenization")) {
							propBuilder.tokenization(((String) innerValue));
						}
						if (innerKey.equals("indexFilterable")) {
							propBuilder.indexFilterable((Boolean) innerValue);
						}
						if (innerKey.equals("indexSearchable")) {
							propBuilder.indexSearchable((Boolean) innerValue);
						}
					});
				} else {
					throw new OpConfigError(
							"Named vector properties must be a Map<String, Map<String, Object>>, but got "
									+ value.getClass().getSimpleName() + " instead for the inner value");
				}
				propertyList.add(propBuilder.build());
			});
			return propertyList;
		}).orElse(null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LongFunction<MultiTenancyConfig> buildMultiTenancyConfig(LongFunction<Map> mapLongFunction) {
		return l -> {
			MultiTenancyConfigBuilder mtcB = MultiTenancyConfig.builder();
			mapLongFunction.apply(l).forEach((key, val) -> {
				if (key.equals("enabled")) {
					mtcB.enabled((Boolean) val);
				}
			});
			return mtcB.build();
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LongFunction<ReplicationConfig> buildReplicationConfig(LongFunction<Map> mapLongFunction) {
		return l -> {
			ReplicationConfigBuilder rConfB = ReplicationConfig.builder();
			mapLongFunction.apply(l).forEach((key, val) -> {
				if (key.equals("factor")) {
					rConfB.factor(((Number) val).intValue());
				}
			});
			return rConfB.build();
		};
	}

	@Override
	public LongFunction<WeaviateBaseOp<WeaviateClass>> createOpFunc(LongFunction<WeaviateClass> paramF,
			LongFunction<WeaviateClient> clientF, ParsedOp op, LongFunction<String> targetF) {
		return l -> new WeaviateCreateCollectionOp(clientF.apply(l), paramF.apply(l));
	}

}
