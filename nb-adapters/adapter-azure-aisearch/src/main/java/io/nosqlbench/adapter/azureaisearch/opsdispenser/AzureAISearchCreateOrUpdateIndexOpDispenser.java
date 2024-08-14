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
package io.nosqlbench.adapter.azureaisearch.opsdispenser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.BinaryQuantizationCompression;
import com.azure.search.documents.indexes.models.ExhaustiveKnnAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.ExhaustiveKnnParameters;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.HnswParameters;
import com.azure.search.documents.indexes.models.ScalarQuantizationCompression;
import com.azure.search.documents.indexes.models.ScalarQuantizationParameters;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmMetric;
import com.azure.search.documents.indexes.models.VectorSearchCompression;
import com.azure.search.documents.indexes.models.VectorSearchCompressionTarget;
import com.azure.search.documents.indexes.models.VectorSearchProfile;

import io.nosqlbench.adapter.azureaisearch.AzureAISearchDriverAdapter;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchBaseOp;
import io.nosqlbench.adapter.azureaisearch.ops.AzureAISearchCreateOrUpdateIndexOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

/**
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/rest/api/searchservice/indexes/create-or-update?view=rest-searchservice-2024-07-01&tabs=HTTP">API
 *      Reference</a>
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/java/api/com.azure.search.documents?view=azure-java-stable">Index
 *      docs</a>
 */
public class AzureAISearchCreateOrUpdateIndexOpDispenser extends AzureAISearchBaseOpDispenser<SearchIndex> {
	private SearchField searchField;
	private VectorSearchProfile vsProfile;

	public AzureAISearchCreateOrUpdateIndexOpDispenser(AzureAISearchDriverAdapter adapter, ParsedOp op,
			LongFunction<String> targetF) {
		super(adapter, op, targetF);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public LongFunction<SearchIndex> getParamFunc(LongFunction<SearchIndexClient> clientF, ParsedOp op,
			LongFunction<String> targetF) {
		logger.debug(">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>getParamFunc");
		LongFunction<SearchIndex> ebF = l -> new SearchIndex(targetF.apply(l));

		Optional<LongFunction<Map>> fieldsMapF = op.getAsOptionalFunction("fields", Map.class);
		if (fieldsMapF.isPresent()) {
			final LongFunction<List<SearchField>> searchFieldListF = buildFieldsStruct(op);
			final LongFunction<SearchIndex> fieldsF = ebF;
			ebF = l -> fieldsF.apply(l).setFields(searchFieldListF.apply(l));
		}

		Optional<LongFunction<Map>> vsearchMapF = op.getAsOptionalFunction("vectorSearch", Map.class);
		if (vsearchMapF.isPresent()) {
			final LongFunction<VectorSearch> vSearchF = buildVectorSearchStruct(op);
			final LongFunction<SearchIndex> vsF = ebF;
			ebF = l -> vsF.apply(l).setVectorSearch(vSearchF.apply(l));
		}

		final LongFunction<SearchIndex> lastF = ebF;
		return l -> lastF.apply(l);
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	private LongFunction<VectorSearch> buildVectorSearchStruct(ParsedOp op) {
		Optional<LongFunction<Map>> baseFunc = op.getAsOptionalFunction("vectorSearch", Map.class);
		return baseFunc.<LongFunction<VectorSearch>>map(mapLongFunc -> l -> {
			Map<String, Object> vsMap = mapLongFunc.apply(l);
			VectorSearch vectorSearch = new VectorSearch();
			vsMap.forEach((vsField, vsValue) -> {
				logger.debug(
						">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>vsField:{}  vsValue:{}",
						vsField, vsValue);
				if (vsValue instanceof Map) {
					((Map<String, Object>) vsValue).forEach((innerKey, innerValue) -> {
						logger.debug(
								">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>innerKey:{}  innerValue:{}",
								innerKey, innerValue);
						if ("compressions".equals(vsField)) {
							List<VectorSearchCompression> vsCompList = new ArrayList<>();
							String kind;
							if (((Map<String, Object>) innerValue).containsKey("kind")) {
								kind = (String) ((Map<String, Object>) innerValue).get("kind");
								logger.debug(
										">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>compressions>>>>kind:{}",
										kind);
								if (kind.equals("scalarQuantization")) {
									ScalarQuantizationCompression sqComp = new ScalarQuantizationCompression(innerKey);
									((Map<String, Object>) innerValue).forEach((compressKey, compressValue) -> {
										logger.debug(
												">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>kind:{}  compressKey:{}  compressValue:{}",
												kind, compressKey, compressValue);
										if (compressKey.equals("kind")) {
											sqComp.getKind().fromString((String) compressValue);
										}
										if (compressKey.equals("rerankWithOriginalVectors")) {
											sqComp.setRerankWithOriginalVectors((Boolean) compressValue);
										}
										if (compressKey.equals("defaultOversampling")) {
											sqComp.setDefaultOversampling(((Number) compressValue).doubleValue());
										}
										if (compressKey.equals("scalarQuantizationParameters")) {
											ScalarQuantizationParameters sqParams = new ScalarQuantizationParameters();
											((Map<String, Object>) compressValue).forEach((sqKey, sqVal) -> {
												if (sqKey.equals("quantizedDataType")) {
													sqParams.setQuantizedDataType(
															VectorSearchCompressionTarget.fromString((String) sqVal));
												}
											});
											sqComp.setParameters(sqParams);
										}
									});
									vsCompList.add(sqComp);
//									vsCompList.add(buildVectorSearchCompression(bqComp, compressKey, compressValue, true));
								} else {
									// BinaryQuantization is assumed here

									BinaryQuantizationCompression bqComp = new BinaryQuantizationCompression(innerKey);
									((Map<String, Object>) innerValue).forEach((compressKey, compressValue) -> {
										logger.debug(
												">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>kind:{}  compressKey:{}  compressValue:{}",
												kind, compressKey, compressValue);
										if (compressKey.equals("kind")) {
											bqComp.getKind().fromString((String) compressValue);
										}
										if (compressKey.equals("rerankWithOriginalVectors")) {
											bqComp.setRerankWithOriginalVectors((Boolean) compressValue);
										}
										if (compressKey.equals("defaultOversampling")) {
											bqComp.setDefaultOversampling(((Number) compressValue).doubleValue());
										}
									});
									vsCompList.add(bqComp);
//									vsCompList.add(
//											buildVectorSearchCompression(bqComp, compressKey, compressValue, false));
								}
							} else {
								VectorSearchCompression vsComp = new VectorSearchCompression(innerKey);
								((Map<String, Object>) innerValue).forEach((compressKey, compressValue) -> {
									logger.debug(
											">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>kind:{}  compressKey:{}  compressValue:{}",
											null, compressKey, compressValue);
									if (compressKey.equals("kind")) {
										vsComp.getKind().fromString((String) compressValue);
									}
									if (compressKey.equals("rerankWithOriginalVectors")) {
										vsComp.setRerankWithOriginalVectors((Boolean) compressValue);
									}
									if (compressKey.equals("defaultOversampling")) {
										vsComp.setDefaultOversampling(((Number) compressValue).doubleValue());
									}
								});
								vsCompList.add(vsComp);
							}
							vectorSearch.setCompressions(vsCompList);
							vectorSearch.getCompressions().forEach((comp) -> {
								logger.debug(
										">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>compressions FINAL: Name:{}",
										comp.getCompressionName());
							});
						}
						if ("algorithms".equals(vsField)) {
							List<VectorSearchAlgorithmConfiguration> vsAlgoList = new ArrayList<>();
							String kind;
							if (((Map<String, Object>) innerValue).containsKey("kind")) {
								kind = (String) ((Map<String, Object>) innerValue).get("kind");
								logger.debug(
										">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>algorithms>>>>kind:{}",
										kind);
								if("hnsw".equals(kind)) {
									HnswAlgorithmConfiguration hnswAlgoConf = new HnswAlgorithmConfiguration(innerKey);
									((Map<String, Object>) innerValue).forEach((hnswKey, hnswValue) -> {
										logger.debug(
												">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>algorithms>>>>kind:{}  hnswKey:{}  hnswValue:{}",
												kind, hnswKey, hnswValue);
										if ("hnswParameters".equals(hnswKey)) {
											((Map<String, Object>) innerValue)
													.forEach((hnswParamsKey, hnswParamsValue) -> {
														logger.debug(
																">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>algorithms>>>>kind:{}  hnswKey:{}  hnswValue:{}  hnswParamsKey:{} hnswParamsValue:{}",
																kind, hnswKey, hnswValue, hnswParamsKey,
																hnswParamsValue);
														HnswParameters hnswParams = new HnswParameters();
														if ("m".equals(hnswParamsKey)) {
															hnswParams.setM(((Number) hnswParamsValue).intValue());
														}
														if ("efConstruction".equals(hnswParamsKey)) {
															hnswParams.setEfConstruction(
																	((Number) hnswParamsValue).intValue());
														}
														if ("efSearch".equals(hnswParamsKey)) {
															hnswParams
																	.setEfSearch(((Number) hnswParamsValue).intValue());
														}
														if ("metric".equals(hnswParamsKey)) {
															hnswParams.setMetric(VectorSearchAlgorithmMetric
																	.fromString((String) hnswParamsValue));
														}
														hnswAlgoConf.setParameters(hnswParams);
													});
										}
									});
									vsAlgoList.add(hnswAlgoConf);
								}
								if ("exhaustiveKnn".equals(kind)) {
									ExhaustiveKnnAlgorithmConfiguration exhausKnnAlgoConf = new ExhaustiveKnnAlgorithmConfiguration(
											innerKey);
									((Map<String, Object>) innerValue).forEach((algoKey, algoValue) -> {
										logger.debug(
												">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>algorithms>>>>kind:{}  algoKey:{}  algoValue:{}",
												kind, algoKey, algoValue);
										if (algoKey.equals("exhaustiveKnnParameters")) {
											ExhaustiveKnnParameters eKnnParms = new ExhaustiveKnnParameters();
											((Map<String, Object>) algoValue).forEach((ekpKey, ekpVal) -> {
												logger.debug(
														">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>algorithms>>>>kind:{}  algoKey:{}  algoValue:{}  ekpKey:{}  ekpVal:{}",
														kind, algoKey, algoValue, ekpKey, ekpVal);
												if (ekpKey.equals("quantizedDataType")) {
													eKnnParms.setMetric(
															VectorSearchAlgorithmMetric.fromString((String) ekpVal));
												}
											});
											exhausKnnAlgoConf.setParameters(eKnnParms);
										}
									});
									vsAlgoList.add(exhausKnnAlgoConf);
								}
							}
							vectorSearch.setAlgorithms(vsAlgoList);
							vectorSearch.getAlgorithms().forEach((algo) -> {
								logger.debug(
										">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>algorithms FINAL: Name:{}",
										algo.getName());
							});
						}
						if ("profiles".equals(vsField)) {
							logger.debug(
									">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>profiles");
							List<VectorSearchProfile> vsProfileList = new ArrayList<>();
							// VectorSearchProfile vsProfile = new VectorSearchProfile(innerKey, null);
							((Map<String, Object>) vsValue).forEach((profKey, profVal) -> {
								logger.debug(
										">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>profiles: profKey:{}  profVal:{}",
										profKey, profVal);
								((Map<String, Object>) profVal).forEach((pK, pV) -> {
									logger.debug(
											">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>profiles: profKey:{}  profVal:{}  pK:{}  pV:{}",
											profKey, profVal, pK, pV);
									if ("algorithm".equals(pK)) {
										vsProfile = new VectorSearchProfile(profKey, (String) pV);
									}
									if ("compression".equals(pK)) {
										vsProfile.setCompressionName((String) pV);
									}

									logger.debug(
											">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>profiles: Name:{}>>>AlgoName:{}>>>CompressionName:{}",
											vsProfile.getName(), vsProfile.getAlgorithmConfigurationName(),
											vsProfile.getCompressionName());
								});
								vsProfileList.add(vsProfile);
							});
							vectorSearch.setProfiles(vsProfileList);
							vectorSearch.getProfiles().forEach((profile) -> {
								logger.debug(
										">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>VectorSearch>>>>buildVectorSearchStruct>>>>profiles FINAL: Name:{}  AlgorithmConfName:{}",
										profile.getName(), profile.getAlgorithmConfigurationName());
							});
						}
					});
				} else {
					throw new OpConfigError(
							"Vector Search properties must be a Map<String, Map<String, Object>>, but got "
									+ vsValue.getClass().getSimpleName() + " instead for the inner value");
				}
			});
			return vectorSearch;
		}).orElse(null);
	}

	@SuppressWarnings({ "unchecked", "static-access" })
	private VectorSearchCompression buildVectorSearchCompression(VectorSearchCompression vsComp, String key, Object val,
			boolean isSQ) {
		if (key.equals("kind")) {
			vsComp.getKind().fromString((String) val);
		}
		if (key.equals("rerankWithOriginalVectors")) {
			vsComp.setRerankWithOriginalVectors((Boolean) val);
		}
		if (key.equals("defaultOversampling")) {
			vsComp.setDefaultOversampling(((Number) val).doubleValue());
		}
		if (isSQ) {
			if (key.equals("scalarQuantizationParameters")) {
				ScalarQuantizationParameters sqParams = new ScalarQuantizationParameters();
				((Map<String, Object>) val).forEach((sqKey, sqVal) -> {
					if (sqKey.equals("quantizedDataType")) {
						sqParams.setQuantizedDataType(VectorSearchCompressionTarget.fromString((String) sqVal));
					}
				});
				((ScalarQuantizationCompression) vsComp).setParameters(sqParams);
			}
		}
		return vsComp;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LongFunction<List<SearchField>> buildFieldsStruct(ParsedOp op) {
		logger.debug(">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>buildFieldsStruct");
		Optional<LongFunction<Map>> baseFunc = op.getAsOptionalFunction("fields", Map.class);
		return baseFunc.<LongFunction<List<SearchField>>>map(mapLongFunc -> l -> {
			Map<String, Object> fMap = mapLongFunc.apply(l);
			List<SearchField> fieldsList = new ArrayList<>();
			fMap.forEach((fName, fValue) -> {
				if (fValue instanceof Map) {
					logger.debug(
							">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>buildFieldsStruct>>>>fName:{}  fValue:{}",
							fName, fValue);
					((Map<String, Object>) fValue).forEach((innerKey, innerValue) -> {
						logger.debug(
								">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>buildFieldsStruct>>>>fName:{}  fValue:{}  fName:{}  fValue:{}",
								fName, fValue, innerKey, innerValue);
						if (innerKey.equals("type")) {
							searchField = new SearchField(fName, SearchFieldDataType.fromString((String) innerValue));
						}
						if (innerKey.equals("key")) {
							searchField.setKey((Boolean) innerValue);
						}
						if (innerKey.equals("dimensions")) {
							searchField.setVectorSearchDimensions(((Number) innerValue).intValue());
						}
						if (innerKey.equals("vectorSearchProfile")) {
							searchField.setVectorSearchProfileName((String) innerValue);
							logger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% {} %n",
									searchField.getVectorSearchProfileName());
						}
						if (innerKey.equals("filterable")) {
							searchField.setFilterable((Boolean) innerValue);
						}
						if (innerKey.equals("sortable")) {
							searchField.setSortable(((Boolean) innerValue));
						}
						if (innerKey.equals("searchable")) {
							searchField.setSearchable((Boolean) innerValue);
						}
						if (innerKey.equals("facetable")) {
							searchField.setFacetable((Boolean) innerValue);
						}
						if (innerKey.equals("retrievable")) {
							// For now we're ignoring this as this is internally set to 'hidden' property's
							// value by the searchIndexClient
						}
						if (innerKey.equals("hidden")) {
							searchField.setHidden((Boolean) innerValue);
						}
					});
				} else {
					throw new OpConfigError(
							"Fields properties must be a Map<String, Map<String, Object>>, but got "
									+ fValue.getClass().getSimpleName() + " instead for the inner value");
				}
				fieldsList.add(searchField);
				if (logger.isDebugEnabled()) {
					fieldsList.forEach((field) -> {
						logger.debug(
								">>>>>>>>>>>>AzureAISearchCreateOrUpdateIndexOpDispenser>>>>>>>>>>>>buildFieldsStruct>>>> fields FINAL: Name:{}  VSProfileName:{}",
								field.getName(), field.getVectorSearchProfileName());
					});
				}
			});
			return fieldsList;
		}).orElse(null);
	}

	@Override
	public LongFunction<AzureAISearchBaseOp<SearchIndex>> createOpFunc(LongFunction<SearchIndex> paramF,
			LongFunction<SearchIndexClient> clientF, ParsedOp op, LongFunction<String> targetF) {
		return l -> new AzureAISearchCreateOrUpdateIndexOp(clientF.apply(l), paramF.apply(l));
	}

}
