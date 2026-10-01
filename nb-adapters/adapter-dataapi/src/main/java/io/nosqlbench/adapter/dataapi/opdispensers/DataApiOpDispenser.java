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

package io.nosqlbench.adapter.dataapi.opdispensers;

import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.ReturnDocument;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.commands.Updates;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.tables.commands.AlterTypeAddFields;
import com.datastax.astra.client.tables.commands.AlterTypeOperation;
import com.datastax.astra.client.tables.commands.AlterTypeRenameFields;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateTextIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTableIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTypeOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableTextIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;
import io.nosqlbench.adapter.dataapi.DataApiSpace;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.*;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DataApiOpDispenser extends BaseOpDispenser<DataApiBaseOp, DataApiSpace> {
    protected final LongFunction<String> targetFunction;
    protected final LongFunction<DataApiSpace> spaceFunction;

    protected DataApiOpDispenser(DriverAdapter<? extends DataApiBaseOp, DataApiSpace> adapter, ParsedOp op,
                                 LongFunction<String> targetFunction) {
        super(adapter, op, adapter.getSpaceFunc(op));
        this.targetFunction = targetFunction;
        this.spaceFunction = adapter.getSpaceFunc(op);
    }

    protected Sort[] getSortFromOp(ParsedOp op, long l) {
        List<Sort> sorts = null;

        long sortKeyCount = Stream.of("sort", "vector", "vectorize").filter(op::isDefined).count();
        if (sortKeyCount > 1) {
            throw new OpConfigError(
                "Can sort by only one of: 'sort' (regular asc/desc), 'vector', 'vectorize' in an op."
            );
        }

        Optional<LongFunction<Map>> sortFunction = op.getAsOptionalFunction("sort", Map.class);
        if (sortFunction.isPresent()) {
            @SuppressWarnings("unchecked")
            Map<String,Object> sortFields = sortFunction.get().apply(l);
            
            sorts = sortFields.entrySet().stream()
                .map(e -> {
                    String field = e.getKey();
                    String sortOrder = String.valueOf(e.getValue()).trim();
                    if (sortOrder.equalsIgnoreCase("asc") || sortOrder.equalsIgnoreCase("ascending")) {
                        return Sort.ascending(field);
                    } else if (sortOrder.equalsIgnoreCase("desc") || sortOrder.equalsIgnoreCase("descending")) {
                        return Sort.descending(field);
                    } else {
                        throw new OpConfigError(
                            "Invalid sort order '" + sortOrder + "' for field '" + field +
                            "'; expected 'asc' or 'desc' (case-insensitive)."
                        );
                    }
                })
                .collect(Collectors.toList());
        }

        if (op.isDefined("vector")) {
            float[] vector = getVectorValues(op, l);
            if (vector != null) {
                // TODO use DataAPIVector as soon as the client allows here:
                sorts = List.of(Sort.vector(vector));
            }
        }

        if (op.isDefined("vectorize")) {
            Optional<LongFunction<String>> vzeFunction = op.getAsOptionalFunction("vectorize", String.class);
            if (vzeFunction.isPresent()){
                String vectorize = vzeFunction.get().apply(l);
                sorts = List.of(Sort.vectorize(vectorize));
            }
        }

        if (sorts != null) {
            return sorts.toArray(new Sort[0]);
        }
        return null;
    }

    protected Filter getFilterFromOp(ParsedOp op, long l) {
        Map<String, Object> filterMap = getFreeFormFromOp(op, l, "filter", false);
        if (filterMap != null) {
            return new Filter(filterMap);
        }
        return null;
    }

    protected Document getReplacementFromOp(ParsedOp op, long l) {
        Map<String, Object> docMap = getFreeFormFromOp(op, l, "replacement", true);
        return new Document(docMap);
    }

    protected Document getDocumentFromRawMap(Map<String, Object> docMap) {
        Document doc = new Document(docMap);
        // if docMap has a '$vector' key, invoke:
        if (docMap.containsKey("$vector")) {
            doc.vector(new DataAPIVector(getVectorValues(docMap.get("$vector"))));
        }
        return doc;
    }

    protected Document getDocumentFromOp(ParsedOp op, long l) {
        Map<String, Object> docMap = getFreeFormFromOp(op, l, "document", true);
        return getDocumentFromRawMap(docMap);
    }

    protected List<Document> getDocumentsFromOp(ParsedOp op, long l) {
        List<Map<String, Object>> docMapList = getFreeFormListFromOp(op, l, "documents", true);
        return docMapList.stream().map((docMap) -> getDocumentFromRawMap(docMap)).toList();
    }

    protected Update getUpdateFromOp(ParsedOp op, long l) {
        Map<String, Object> updateMap = getFreeFormFromOp(op, l, "update", true);
        return new Update(updateMap);
    }

    protected Map<String, Object> getCollectionDefinitionFromOp(ParsedOp op, long l) {
        Map<String, Object> definition = getFreeFormFromOp(op, l, "definition", true);
        return definition;
    }

    protected float[] getVectorValues(ParsedOp op, long l) {
        Object rawVectorValues = op.get("vector", l);
        return getVectorValues(rawVectorValues);
    }

    protected float[] getVectorValues(Object rawVectorValues) {
        float[] floatValues;
        if (rawVectorValues instanceof float[] f) {
            return f;
        }
        if (rawVectorValues instanceof String) {
            String[] rawValues = (((String) rawVectorValues).split(","));
            floatValues = new float[rawValues.length];
            for (int i = 0; i < rawValues.length; i++) {
                floatValues[i] = Float.parseFloat(rawValues[i]);
            }
        } else if (rawVectorValues instanceof List) {
            return getVectorValuesList(rawVectorValues);
        } else if (rawVectorValues == null) {
            throw new RuntimeException("Invalid specification for values (null)");
        } else {
            throw new RuntimeException("Invalid type specified for values (type: " + rawVectorValues.getClass().getSimpleName() + "), values: " + rawVectorValues.toString());
        }
        return floatValues;
    }

    protected float[] getVectorValuesList(Object rawVectorValues) {
        float[] vectorValues = null;
        @SuppressWarnings("unchecked")
        List<Object> vectorValuesList = (List<Object>) rawVectorValues;
        vectorValues = new float[vectorValuesList.size()];
        for (int i = 0; i < vectorValuesList.size(); i++) {
            vectorValues[i] = Float.parseFloat(vectorValuesList.get(i).toString());
        }
        return vectorValues;
    }

    protected Projection[] getProjectionFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Map>> projectionFunction = op.getAsOptionalFunction("projection", Map.class);
        if (projectionFunction.isEmpty()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, List<String>> projectionFields = projectionFunction.get().apply(l);
        return projectionFields.entrySet().stream()
            .flatMap(field -> {
                String[] arr = field.getValue().toArray(new String[0]);
                if (field.getKey().equalsIgnoreCase("include")) {
                    return Arrays.stream(Projection.include(arr));
                } else if (field.getKey().equalsIgnoreCase("exclude")) {
                    return Arrays.stream(Projection.exclude(arr));
                } else {
                    logger.error("Projection directive '" + field.getKey() + "' not supported");
                    return Stream.<Projection>empty();
                }
            }).toArray(Projection[]::new);
    }

    protected Boolean getUpsertFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Boolean>> upsertFunction = op.getAsOptionalFunction("upsert", Boolean.class);
        if (upsertFunction.isPresent()) {
            LongFunction<Boolean> uf = upsertFunction.get();
            return uf.apply(l);
        }
        return null;
    }

    protected Integer getChunkSizeFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Integer>> csFunction = op.getAsOptionalFunction("chunk_size", Integer.class);
        if (csFunction.isPresent()) {
            LongFunction<Integer> cs = csFunction.get();
            return cs.apply(l);
        }
        return null;
    }

    protected Boolean getOrderedFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Boolean>> orderedFunction = op.getAsOptionalFunction("ordered", Boolean.class);
        if (orderedFunction.isPresent()) {
            LongFunction<Boolean> of = orderedFunction.get();
            return of.apply(l);
        }
        return null;
    }

    protected ReturnDocument getReturnDocumentFromOp(ParsedOp op, long l) {
        Optional<LongFunction<String>> rdf = op.getAsOptionalFunction("return_document", String.class);
        if (rdf.isPresent()) {
            String rdValue = rdf.get().apply(l);
            return switch (rdValue) {
                case "after" -> ReturnDocument.AFTER;
                case "before" -> ReturnDocument.BEFORE;
                default -> throw new RuntimeException("Invalid returnDocument value: " + op.get("return_document", l));
            };
        }
        return null;
    }

    protected Optional<Boolean> getIncludeSimilarityFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Boolean>> includeSimFunction = op.getAsOptionalFunction("include_similarity", Boolean.class);
        if (includeSimFunction.isPresent()) {
            LongFunction<Boolean> sf = includeSimFunction.get();
            return Optional.of(sf.apply(l));
        }
        return Optional.empty();
    }

    protected Optional<Boolean> getIncludeSortVectorFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Boolean>> includeSvFunction = op.getAsOptionalFunction("include_sort_vector", Boolean.class);
        if (includeSvFunction.isPresent()) {
            LongFunction<Boolean> sv = includeSvFunction.get();
            return Optional.of(sv.apply(l));
        }
        return Optional.empty();
    }

    protected Optional<Integer> getLimitFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Integer>> limitFunction = op.getAsOptionalFunction("limit", Integer.class);
        if (limitFunction.isPresent()) {
            LongFunction<Integer> lf = limitFunction.get();
            return Optional.of(lf.apply(l));
        }
        return Optional.empty();
    }

    protected Optional<Integer> getSkipFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Integer>> skipFunction = op.getAsOptionalFunction("skip", Integer.class);
        if (skipFunction.isPresent()) {
            LongFunction<Integer> sf = skipFunction.get();
            return Optional.of(sf.apply(l));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getFreeFormFromOp(ParsedOp op, long l, String fieldName, Boolean required) {
        Optional<LongFunction<Map>> ffMapFunc = op.getAsOptionalFunction(fieldName, Map.class);
        if (ffMapFunc.isPresent()) {
            LongFunction<Map> dmf = ffMapFunc.get();
            return dmf.apply(l);
        } else {
            if (required) {
                throw new OpConfigError(
                    "Required field '" + fieldName + "' not supplied."
                );
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getFreeFormListFromOp(ParsedOp op, long l, String fieldName, Boolean required) {
        Optional<LongFunction<List>> ffMapFunc = op.getAsOptionalFunction(fieldName, List.class);
        if (ffMapFunc.isPresent()) {
            LongFunction<List> dmf = ffMapFunc.get();
            return dmf.apply(l);
        } else {
            if (required) {
                throw new OpConfigError(
                    "Required field '" + fieldName + "' not supplied."
                );
            }
            return null;
        }
    }

    protected Optional<Boolean> getIncludeScoresFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Boolean>> includeScFunction = op.getAsOptionalFunction("include_scores", Boolean.class);
        if (includeScFunction.isPresent()) {
            LongFunction<Boolean> isf = includeScFunction.get();
            return Optional.of(isf.apply(l));
        }
        return Optional.empty();
    }

    protected Optional<String> getRerankQueryFromOp(ParsedOp op, long l) {
        Optional<LongFunction<String>> rqf = op.getAsOptionalFunction("rerank_query", String.class);
        if (rqf.isPresent()) {
            String rqValue = rqf.get().apply(l);
            return Optional.of(rqValue);
        }
        return Optional.empty();
    }

    protected Optional<String> getRerankOnFromOp(ParsedOp op, long l) {
        Optional<LongFunction<String>> rof = op.getAsOptionalFunction("rerank_on", String.class);
        if (rof.isPresent()) {
            String roValue = rof.get().apply(l);
            return Optional.of(roValue);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    protected Optional<Object> getHybridLimitsFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Object>> hlFunc = op.getAsOptionalFunction("hybrid_limits", Object.class);
        if (hlFunc.isPresent()) {
            Object hlValue = hlFunc.get().apply(l);
            if (hlValue instanceof Integer || hlValue instanceof Long) {
                return Optional.of(hlValue);
            } else if (hlValue instanceof Map<?, ?> rawMap) {
                return Optional.of((Map<String, Object>) rawMap);
            } else {
                throw new OpConfigError(
                    "'hybrid_limits' must be an integer or a map, got: " + hlValue.getClass().getSimpleName()
                );
            }
        }
        return Optional.empty();
    }

    protected Optional<RerankServiceOptions> getRerankServiceFromOp(ParsedOp op, long l) {
        Map<String, Object> rerankService = getFreeFormFromOp(op, l, "rerank", false);
        if (rerankService != null) {
            RerankServiceOptions rsOptions = new RerankServiceOptions();
            if (rerankService.containsKey("provider")) {
                rsOptions = rsOptions.provider((String) rerankService.get("provider"));
            }
            if (rerankService.containsKey("modelName")) {
                rsOptions = rsOptions.modelName((String) rerankService.get("modelName"));
            }
            if (rerankService.containsKey("authentication")) {
                rsOptions = rsOptions.authentication((Map<String, Object>) rerankService.get("authentication"));
            }
            if (rerankService.containsKey("parameters")) {
                rsOptions = rsOptions.parameters((Map<String, Object>) rerankService.get("parameters"));
            }
            return Optional.of(rsOptions);
        }
        return Optional.empty();
    }

    protected Sort getFindAndRerankSortFromOp(ParsedOp op, long l) {
        Map<String, Object> docMap = getFreeFormFromOp(op, l, "sort", true);

        if (docMap.size() != 1 || !docMap.containsKey("$hybrid")) {
            throw new OpConfigError("'sort' must contain exactly one key: '$hybrid'");
        }

        Object hybridValue = docMap.get("$hybrid");

        if (hybridValue instanceof String hybridString) {
            return Sort.hybrid(hybridString);
        }

        if (hybridValue instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> hybMap = (Map<String, Object>) rawMap;

            boolean hasVectorize = hybMap.containsKey("$vectorize");
            boolean hasVector = hybMap.containsKey("$vector");
            if (!hasVectorize && !hasVector) {
                throw new OpConfigError(
                    "'$hybrid' sub-object must contain at least one of '$vectorize' or '$vector'"
                );
            }

            Set<String> allowedKeys = Set.of("$vectorize", "$vector", "$lexical");
            for (String key : hybMap.keySet()) {
                if (!allowedKeys.contains(key)) {
                    throw new OpConfigError("Unexpected key in '$hybrid' sub-object: '" + key + "'");
                }
            }

            Hybrid hyb = new Hybrid();
            if (hasVectorize) {
                hyb = hyb.vectorize((String) hybMap.get("$vectorize"));
            }
            if (hasVector) {
                hyb = hyb.vector(new DataAPIVector(getVectorValues(hybMap.get("$vector"))));
            }
            if (hybMap.containsKey("$lexical")) {
                hyb = hyb.lexical((String) hybMap.get("$lexical"));
            }
            return Sort.hybrid(hyb);
        }

        throw new OpConfigError(
            "'$hybrid' value must be a string or a sub-object, got: " + hybridValue.getClass().getSimpleName()
        );
    }

     /* LEGACY OP DISPENSER UTILS START HERE */

    /*
    updates:
        update:
            operation: inc
            field: "incd field"
            value: 500
     */
    protected Update legacyGetUpdateFromOp(ParsedOp op, long l) {
        Update update = new Update();
        Optional<LongFunction<Map>> updatesFunction = op.getAsOptionalFunction("updates", Map.class);
        if (updatesFunction.isPresent()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> updates = updatesFunction.get().apply(l);
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("update")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> updateFields = (Map<String, Object>) entry.getValue();
                    switch (updateFields.get("operation").toString()) {
                        case "set" ->
                            update = Updates.set(updateFields.get("field").toString(), updateFields.get("value"));
                        case "inc" ->
                            update = Updates.inc(updateFields.get("field").toString(), ((Number) updateFields.get("value")).doubleValue());
                        case "unset" -> update = Updates.unset(updateFields.get("field").toString());
                        case "addToSet" ->
                            update = Updates.addToSet(updateFields.get("field").toString(), updateFields.get("value"));
                        case "min" ->
                            update = Updates.min(updateFields.get("field").toString(), ((Number) updateFields.get("value")).doubleValue());
                        case "rename" ->
                            update = Updates.rename(updateFields.get("field").toString(), updateFields.get("value").toString());
                        default -> logger.error(() -> "Operation " + updateFields.get("operation") + " not supported");
                    }
                } else {
                    logger.error(() -> "Unsupported entry under 'updates': '" + entry.getKey() + "'");
                }
            }
        }
        return update;
    }

    protected void legacyAddOperatorFilter(List<Filter> filtersList, String operator, String fieldName, Object fieldValue) {
        switch (operator) {
            case "all" ->
                filtersList.add(Filters.all(fieldName, fieldValue));
            case "eq" ->
                filtersList.add(Filters.eq(fieldName, fieldValue));
            case "exists" -> {
                if (fieldValue != null) {
                    logger.warn(() -> "'exists' operator does not support value field");
                }
                filtersList.add(Filters.exists(fieldName));
            }
            case "gt" ->
                filtersList.add(Filters.gt(fieldName, ((Number) fieldValue).longValue()));
            case "gte" ->
                filtersList.add(Filters.gte(fieldName, ((Number) fieldValue).longValue()));
            case "hasSize" ->
                filtersList.add(Filters.hasSize(fieldName, ((Number) fieldValue).intValue()));
            case "in" ->
                filtersList.add(Filters.in(fieldName, fieldValue));
            case "lt" ->
                filtersList.add(Filters.lt(fieldName, ((Number) fieldValue).longValue()));
            case "lte" ->
                filtersList.add(Filters.lte(fieldName, ((Number) fieldValue).longValue()));
            case "ne" ->
                filtersList.add(Filters.ne(fieldName, fieldValue));
            case "nin" ->
                filtersList.add(Filters.nin(fieldName, fieldValue));
            default -> logger.error(() -> "Operation '" + operator + "' not supported");
        }
    }

    /*
    filters:
        - conjunction: "and"
        operator: "eq"
        field: "field1"
        value: 74
        - conjunction: "and"
        operator: "lt"
        field: "field2"
        value: 111
     */
    protected Filter legacyGetFilterFromOp(ParsedOp op, long l) {
        Filter filter = null;
        Optional<LongFunction<List>> filterFunction = op.getAsOptionalFunction("filters", List.class)
            .or(() -> op.getAsOptionalFunction("filter",List.class));

        if (filterFunction.isPresent()) {
            @SuppressWarnings("unchecked")
            List<Map<String,Object>> filters = filterFunction.get().apply(l);
            List<Filter> andFilterList = new ArrayList<>();
            List<Filter> orFilterList = new ArrayList<>();
            for (Map<String,Object> filterFields : filters) {
                switch ((String)filterFields.get("conjunction")) {
                    case "and" ->
                        legacyAddOperatorFilter(andFilterList, filterFields.get("operator").toString(), filterFields.get("field").toString(), filterFields.get("value"));
                    case "or" ->
                        legacyAddOperatorFilter(orFilterList, filterFields.get("operator").toString(), filterFields.get("field").toString(), filterFields.get("value"));
                    default -> logger.error(() -> "Conjunction " + filterFields.get("conjunction") + " not supported");
                }
            }
            if (!andFilterList.isEmpty() && !orFilterList.isEmpty()) {
                throw new OpConfigError(
                    "filters list mixes 'and' and 'or' conjunctions, which is not supported; " +
                    "use only one conjunction type per filters list"
                );
            }
            if (!andFilterList.isEmpty())
                filter = Filters.and(andFilterList.toArray(new Filter[0]));
            if (!orFilterList.isEmpty())
                filter = Filters.or(orFilterList.toArray(new Filter[0]));
        }
        return filter;
    }

    /*
    sort:
       field: the_field
       type: desc
     */
    protected Sort[] legacyGetSortFromOp(ParsedOp op, long l) {
        List<Sort> sorts = null;

        long sortKeyCount = Stream.of("sort", "vector", "vectorize").filter(op::isDefined).count();
        if (sortKeyCount > 1) {
            throw new OpConfigError(
                "Can sort by only one of: 'sort' (regular asc/desc), 'vector', 'vectorize' in an op."
            );
        }

        Optional<LongFunction<Map>> sortFunction = op.getAsOptionalFunction("sort", Map.class);
        if (sortFunction.isPresent()) {
            @SuppressWarnings("unchecked")
            Map<String,Object> sortFields = sortFunction.get().apply(l);
            String sortOrder = sortFields.get("type").toString();
            String sortField = sortFields.get("field").toString();
            switch(sortOrder) {
                case "asc" -> sorts = List.of(Sort.ascending(sortField));
                case "desc" -> sorts = List.of(Sort.descending(sortField));
            }
        }

        if (op.isDefined("vector")) {
            float[] vector = getVectorValues(op, l);
            if (vector != null) {
                // TODO use DataAPIVector as soon as the client allows here:
                sorts = List.of(Sort.vector(vector));
            }
        }

        if (op.isDefined("vectorize")) {
            Optional<LongFunction<String>> vzeFunction = op.getAsOptionalFunction("vectorize", String.class);
            if (vzeFunction.isPresent()){
                String vectorize = vzeFunction.get().apply(l);
                sorts = List.of(Sort.vectorize(vectorize));
            }
        }

        if (sorts != null) {
            return sorts.toArray(new Sort[0]);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected CollectionDefinition legacyGetCollectionDefinitionFromOp(ParsedOp op, long l) {
        CollectionDefinition optionsBldr = new CollectionDefinition();
        Optional<LongFunction<Integer>> dimFunc = op.getAsOptionalFunction("dimensions", Integer.class);
        if (dimFunc.isPresent()) {
            LongFunction<Integer> af = dimFunc.get();
            optionsBldr = optionsBldr.vectorDimension(af.apply(l));
        }
        Optional<LongFunction<String>> simFunc = op.getAsOptionalFunction("similarity", String.class);
        if (simFunc.isPresent()) {
            LongFunction<String> sf = simFunc.get();
            optionsBldr = optionsBldr.vectorSimilarity(SimilarityMetric.fromValue(sf.apply(l)));
        }
        Optional<LongFunction<String>> typeFunc = op.getAsOptionalFunction("collectionType", String.class);
        if (typeFunc.isPresent()) {
            LongFunction<String> tf = typeFunc.get();
            optionsBldr = optionsBldr.defaultId(CollectionDefaultIdTypes.fromValue(tf.apply(l)));
        }
        Optional<LongFunction<String>> providerFunc = op.getAsOptionalFunction("serviceProvider", String.class);
        Optional<LongFunction<String>> modeFunc = op.getAsOptionalFunction("serviceMode", String.class);
        Optional<LongFunction<String>> apiKeyFunc = op.getAsOptionalFunction("serviceSharedSecretKey", String.class);
        Optional<LongFunction<Map>> paramFunc = op.getAsOptionalFunction("serviceParameters", Map.class);
        if (providerFunc.isPresent() && modeFunc.isPresent()) {
            LongFunction<String> pf = providerFunc.get();
            LongFunction<String> mf = modeFunc.get();
            if (apiKeyFunc.isPresent()) {
                LongFunction<String> ak = apiKeyFunc.get();
                optionsBldr = paramFunc.isPresent() ?
                    optionsBldr.vectorize(pf.apply(l), mf.apply(l), ak.apply(l), paramFunc.get().apply(l)) :
                    optionsBldr.vectorize(pf.apply(l), mf.apply(l), ak.apply(l));
            } else {
                if (paramFunc.isPresent()) {
                    optionsBldr = optionsBldr.vectorize(pf.apply(l), mf.apply(l), paramFunc.get().apply(l));
                } else {
                    optionsBldr = optionsBldr.vectorize(pf.apply(l), mf.apply(l));
                }
            }
        }
        @SuppressWarnings("unchecked")
        Optional<LongFunction<List<String>>> allowFunc = (Optional<LongFunction<List<String>>>) (Optional<?>) op.getAsOptionalFunction("allowIndex", List.class);
        if (allowFunc.isPresent()) {
            LongFunction<List<String>> af = allowFunc.get();
            optionsBldr = optionsBldr.indexingAllow(af.apply(l).toArray(new String[0]));
        }
        @SuppressWarnings("unchecked")
        Optional<LongFunction<List<String>>> denyFunc = (Optional<LongFunction<List<String>>>) (Optional<?>) op.getAsOptionalFunction("denyIndex", List.class);
        if (denyFunc.isPresent()) {
            LongFunction<List<String>> df = denyFunc.get();
            optionsBldr = optionsBldr.indexingDeny(df.apply(l).toArray(new String[0]));
        }

        return optionsBldr;
    }

    /* TABLE OP DISPENSER UTILS START HERE */

    /**
     * Builds a {@link Row} from the op's "row" field, which is expected to be a free-form map of column->value.
     */
    protected Row getRowFromOp(ParsedOp op, long l) {
        Map<String, Object> rowMap = getFreeFormFromOp(op, l, "row", true);
        Row row = Row.create();
        rowMap.forEach(row::add);
        return row;
    }

    /**
     * Builds a list of {@link Row}s from the op's "rows" field (list of free-form maps).
     */
    protected List<Row> getRowsFromOp(ParsedOp op, long l) {
        List<Map<String, Object>> rowMapList = getFreeFormListFromOp(op, l, "rows", true);
        return rowMapList.stream().map(rowMap -> {
            Row row = Row.create();
            rowMap.forEach(row::add);
            return row;
        }).toList();
    }

    /**
     * Builds a {@link TableDefinition} from the op's "table_definition" field.
     * The map must have a "columns" sub-map and a "primary_key" key listing partition and optional clustering columns.
     * Example YAML:
     * <pre>
     * table_definition:
     *   columns:
     *     id: uuid
     *     name: text
     *     age: int
     *   primary_key:
     *     partition_by: [id]
     *     partition_sort:
     *       age: asc
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected TableDefinition getTableDefinitionFromOp(ParsedOp op, long l) {
        Map<String, Object> defMap = getFreeFormFromOp(op, l, "table_definition", true);
        TableDefinition td = new TableDefinition();

        Map<String, Object> columns = (Map<String, Object>) defMap.get("columns");
        if (columns == null) {
            throw new OpConfigError("table_definition must contain a 'columns' map");
        }
        for (Map.Entry<String, Object> col : columns.entrySet()) {
            String colName = col.getKey();
            String typeName = String.valueOf(col.getValue()).toUpperCase();
            TableColumnTypes colType;
            try {
                colType = TableColumnTypes.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                throw new OpConfigError("Unknown column type '" + col.getValue() + "' for column '" + colName + "'");
            }
            td.addColumn(colName, colType);
        }

        Map<String, Object> primaryKey = (Map<String, Object>) defMap.get("primary_key");
        if (primaryKey != null) {
            List<String> partitionBy = (List<String>) primaryKey.get("partition_by");
            if (partitionBy != null && !partitionBy.isEmpty()) {
                td.partitionKey(partitionBy.toArray(new String[0]));
            }
            Map<String, Object> partitionSort = (Map<String, Object>) primaryKey.get("partition_sort");
            if (partitionSort != null) {
                Sort[] sortArr = partitionSort.entrySet().stream().map(e -> {
                    String field = e.getKey();
                    String order = String.valueOf(e.getValue()).trim();
                    if (order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("ascending")) {
                        return Sort.ascending(field);
                    } else if (order.equalsIgnoreCase("desc") || order.equalsIgnoreCase("descending")) {
                        return Sort.descending(field);
                    } else {
                        throw new OpConfigError("Invalid sort order '" + order + "' for clustering column '" + field + "'");
                    }
                }).toArray(Sort[]::new);
                td.clusteringColumns(sortArr);
            }
        }

        return td;
    }

    /**
     * Builds a {@link CreateTableOptions} from the op's optional "if_not_exists" field.
     */
    protected CreateTableOptions getCreateTableOptionsFromOp(ParsedOp op, long l) {
        CreateTableOptions options = new CreateTableOptions();
        Optional<LongFunction<Boolean>> ineFunc = op.getAsOptionalFunction("if_not_exists", Boolean.class);
        if (ineFunc.isPresent()) {
            options = options.ifNotExists(ineFunc.get().apply(l));
        }
        return options;
    }

    /**
     * Builds a {@link TableUpdateOperation} from the op's "update" field.
     * The map is expected to contain "$set" and/or "$unset" sub-maps.
     */
    @SuppressWarnings("unchecked")
    protected TableUpdateOperation getTableUpdateFromOp(ParsedOp op, long l) {
        Map<String, Object> updateMap = getFreeFormFromOp(op, l, "update", true);
        TableUpdateOperation update = new TableUpdateOperation();
        Map<String, Object> setMap = (Map<String, Object>) updateMap.get("$set");
        if (setMap != null) {
            setMap.forEach(update::set);
        }
        Object unsetObj = updateMap.get("$unset");
        if (unsetObj instanceof Map) {
            ((Map<String, Object>) unsetObj).keySet().forEach(update::unset);
        } else if (unsetObj instanceof List) {
            ((List<String>) unsetObj).forEach(update::unset);
        }
        return update;
    }

    /**
     * Builds a {@link TableUserDefinedTypeDefinition} from the op's "type_definition" field.
     * Example YAML:
     * <pre>
     * type_definition:
     *   fields:
     *     street: text
     *     zip: int
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected TableUserDefinedTypeDefinition getUdtDefinitionFromOp(ParsedOp op, long l) {
        Map<String, Object> defMap = getFreeFormFromOp(op, l, "type_definition", true);
        TableUserDefinedTypeDefinition def = new TableUserDefinedTypeDefinition();
        Map<String, Object> fields = (Map<String, Object>) defMap.get("fields");
        if (fields == null) {
            throw new OpConfigError("type_definition must contain a 'fields' map");
        }
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            String typeName = String.valueOf(field.getValue()).toUpperCase();
            try {
                def.addField(field.getKey(), TableUserDefinedTypeFieldTypes.valueOf(typeName));
            } catch (IllegalArgumentException e) {
                throw new OpConfigError("Unknown UDT field type '" + field.getValue() + "' for field '" + field.getKey() + "'");
            }
        }
        return def;
    }

    /**
     * Builds a {@link CreateTypeOptions} from the op's optional "if_not_exists" field.
     */
    protected CreateTypeOptions getCreateTypeOptionsFromOp(ParsedOp op, long l) {
        CreateTypeOptions options = new CreateTypeOptions();
        Optional<LongFunction<Boolean>> ineFunc = op.getAsOptionalFunction("if_not_exists", Boolean.class);
        if (ineFunc.isPresent()) {
            options = options.ifNotExists(ineFunc.get().apply(l));
        }
        return options;
    }

    /**
     * Builds a {@link DropTypeOptions} from the op's optional "if_exists" field.
     */
    protected DropTypeOptions getDropTypeOptionsFromOp(ParsedOp op, long l) {
        DropTypeOptions options = new DropTypeOptions();
        // DropTypeOptions has no if_exists in this SDK version, return as-is
        return options;
    }

    /**
     * Builds an {@link AlterTypeOperation} from the op's "alter_type_operation" field.
     * Supports "add_fields" (map of fieldName->type) and "rename_fields" (map of oldName->newName).
     * Example YAML:
     * <pre>
     * alter_type_operation:
     *   add_fields:
     *     new_col: text
     * # or:
     * alter_type_operation:
     *   rename_fields:
     *     old_name: new_name
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected AlterTypeOperation<?, ?> getAlterTypeOperationFromOp(ParsedOp op, long l) {
        Map<String, Object> opMap = getFreeFormFromOp(op, l, "alter_type_operation", true);
        if (opMap.containsKey("add_fields")) {
            Map<String, Object> fields = (Map<String, Object>) opMap.get("add_fields");
            AlterTypeAddFields addOp = new AlterTypeAddFields();
            for (Map.Entry<String, Object> e : fields.entrySet()) {
                String typeName = String.valueOf(e.getValue()).toUpperCase();
                try {
                    addOp.addField(e.getKey(), TableUserDefinedTypeFieldTypes.valueOf(typeName));
                } catch (IllegalArgumentException ex) {
                    throw new OpConfigError("Unknown UDT field type '" + e.getValue() + "' for field '" + e.getKey() + "'");
                }
            }
            return addOp;
        } else if (opMap.containsKey("rename_fields")) {
            Map<String, Object> renames = (Map<String, Object>) opMap.get("rename_fields");
            AlterTypeRenameFields renameOp = new AlterTypeRenameFields();
            renames.forEach((oldName, newName) -> renameOp.addField(oldName, String.valueOf(newName)));
            return renameOp;
        }
        throw new OpConfigError("alter_type_operation must contain either 'add_fields' or 'rename_fields'");
    }

    /**
     * Builds a {@link TableRegularIndexDefinition} from the op's "index_definition" field.
     * Example YAML:
     * <pre>
     * index_definition:
     *   column: my_col
     * # optional options:
     *   ascii: true
     *   normalize: true
     *   case_sensitive: false
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected TableRegularIndexDefinition getRegularIndexDefinitionFromOp(ParsedOp op, long l) {
        Map<String, Object> defMap = getFreeFormFromOp(op, l, "index_definition", true);
        TableRegularIndexDefinition def = new TableRegularIndexDefinition();
        String column = (String) defMap.get("column");
        if (column == null) throw new OpConfigError("index_definition must contain 'column'");
        def.column(column);
        if (defMap.containsKey("ascii"))
            def.ascii(Boolean.parseBoolean(String.valueOf(defMap.get("ascii"))));
        if (defMap.containsKey("normalize"))
            def.normalize(Boolean.parseBoolean(String.valueOf(defMap.get("normalize"))));
        if (defMap.containsKey("case_sensitive"))
            def.caseSensitive(Boolean.parseBoolean(String.valueOf(defMap.get("case_sensitive"))));
        return def;
    }

    /**
     * Builds a {@link TableTextIndexDefinition} from the op's "index_definition" field.
     * Example YAML:
     * <pre>
     * index_definition:
     *   column: my_text_col
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected TableTextIndexDefinition getTextIndexDefinitionFromOp(ParsedOp op, long l) {
        Map<String, Object> defMap = getFreeFormFromOp(op, l, "index_definition", true);
        TableTextIndexDefinition def = new TableTextIndexDefinition();
        String column = (String) defMap.get("column");
        if (column == null) throw new OpConfigError("index_definition must contain 'column'");
        def.column(column);
        return def;
    }

    /**
     * Builds a {@link TableVectorIndexDefinition} from the op's "index_definition" field.
     * Example YAML:
     * <pre>
     * index_definition:
     *   column: my_vector_col
     * # optional:
     *   source_model: "ada002"
     * </pre>
     */
    @SuppressWarnings("unchecked")
    protected TableVectorIndexDefinition getVectorIndexDefinitionFromOp(ParsedOp op, long l) {
        Map<String, Object> defMap = getFreeFormFromOp(op, l, "index_definition", true);
        TableVectorIndexDefinition def = new TableVectorIndexDefinition();
        String column = (String) defMap.get("column");
        if (column == null) throw new OpConfigError("index_definition must contain 'column'");
        def.column(column);
        if (defMap.containsKey("source_model"))
            def.sourceModel(String.valueOf(defMap.get("source_model")));
        return def;
    }

    /**
     * Builds a {@link DropTableIndexOptions} from the op's optional "if_exists" field.
     */
    protected DropTableIndexOptions getDropIndexOptionsFromOp(ParsedOp op, long l) {
        DropTableIndexOptions options = new DropTableIndexOptions();
        Optional<LongFunction<Boolean>> ieFunc = op.getAsOptionalFunction("if_exists", Boolean.class);
        if (ieFunc.isPresent()) {
            options = options.ifExists(ieFunc.get().apply(l));
        }
        return options;
    }

}
