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
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.query.Projection;
import io.nosqlbench.adapter.dataapi.DataApiSpace;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.*;
import java.util.function.LongFunction;

public abstract class DataApiOpDispenser extends BaseOpDispenser<DataApiBaseOp, DataApiSpace> {
    protected final LongFunction<String> targetFunction;
    protected final LongFunction<DataApiSpace> spaceFunction;
    private volatile boolean warnedDeprecatedUpdates = false;

    protected DataApiOpDispenser(DriverAdapter<? extends DataApiBaseOp, DataApiSpace> adapter, ParsedOp op,
                                 LongFunction<String> targetFunction) {
        super(adapter, op, adapter.getSpaceFunc(op));
        this.targetFunction = targetFunction;
        this.spaceFunction = adapter.getSpaceFunc(op);
    }

    protected Sort getSortFromOp(ParsedOp op, long l) {
        Sort fieldSort = null;
        Optional<LongFunction<Map>> sortFunction = op.getAsOptionalFunction("sort", Map.class);
        if (sortFunction.isPresent()) {
            Map<String,Object> sortFields = sortFunction.get().apply(l);
            String sortOrder = sortFields.get("type").toString();
            String sortField = sortFields.get("field").toString();
            switch(sortOrder) {
                case "asc" -> fieldSort = Sort.ascending(sortField);
                case "desc" -> fieldSort = Sort.descending(sortField);
            }
        }

        Sort vectorSort = null;
        if (op.isDefined("vector")) {
            float[] vector = getVectorValues(op, l);
            if (vector != null) {
                vectorSort = Sort.vector(vector);
            }
        }

        if (fieldSort != null && vectorSort != null) {
            throw new OpConfigError(
                "cannot sort by vector and regular asc/desc criteria at the same time."
            );
        }
        if (vectorSort != null) {
            return vectorSort;
        }
        if (fieldSort != null) {
            return fieldSort;
        }
        return null;
    }

    protected Filter getFilterFromOp(ParsedOp op, long l) {
        // TODO: Clarify 'filter' vs 'filters' or whether to support both uniformly
        Filter filter = null;
        Optional<LongFunction<List>> filterFunction = op.getAsOptionalFunction("filters", List.class)
            .or(() -> op.getAsOptionalFunction("filter",List.class));

        if (filterFunction.isPresent()) {
            List<Map<String,Object>> filters = filterFunction.get().apply(l);
            List<Filter> andFilterList = new ArrayList<>();
            List<Filter> orFilterList = new ArrayList<>();
            for (Map<String,Object> filterFields : filters) {
                switch ((String)filterFields.get("conjunction")) {
                    case "and" ->
                        addOperatorFilter(andFilterList, filterFields.get("operator").toString(), filterFields.get("field").toString(), filterFields.get("value"));
                    case "or" ->
                        addOperatorFilter(orFilterList, filterFields.get("operator").toString(), filterFields.get("field").toString(), filterFields.get("value"));
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

    protected void addOperatorFilter(List<Filter> filtersList, String operator, String fieldName, Object fieldValue) {
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

    @SuppressWarnings("unchecked")
    protected Update getUpdateFromOp(ParsedOp op, long l) {
        Update update = Update.create();
        Object rawUpdates = op.getAsOptionalFunction("updates", Object.class)
            .map(f -> f.apply(l))
            .orElse(null);
        if (rawUpdates instanceof List<?> rawList) {
            // New form: updates is a list of {operation, field, value} entries
            List<Map<String, Object>> updatesList = (List<Map<String, Object>>) rawList;
            for (Map<String, Object> entry : updatesList) {
                String operation = entry.get("operation").toString();
                String field = entry.get("field").toString();
                Object value = entry.get("value");
                applyUpdateOperation(update, operation, field, value);
            }
        } else if (rawUpdates instanceof Map<?,?> rawMap) {
            /* Backward-compatibility with the previous single-update form:
                updates:
                  update:
                    operation: inc
                    field: myfield
                    value: 21474
            */
            if (!warnedDeprecatedUpdates) {
                warnedDeprecatedUpdates = true;
                logger.warn(() -> "DEPRECATED: 'updates' as a map with a nested 'update' key is deprecated. " +
                    "Use a list of operation entries instead (see op template documentation).");
            }
            Map<String, Object> updateFields = (Map<String, Object>) ((Map<?,?>) rawMap).get("update");
            if (updateFields != null) {
                applyUpdateOperation(update,
                    updateFields.get("operation").toString(),
                    updateFields.get("field").toString(),
                    updateFields.get("value"));
            }
        }
        return update;
    }

    private void applyUpdateOperation(Update update, String operation, String field, Object value) {
        switch (operation) {
            case "set" -> update.set(field, value);
            case "inc" -> update.inc(field, ((Number) value).doubleValue());
            case "unset" -> update.unset(field);
            case "addToSet" -> update.addToSet(field, value);
            case "min" -> update.min(field, ((Number) value).doubleValue());
            case "rename" -> update.rename(field, value.toString());
            default -> logger.error(() -> "Operation '" + operation + "' not supported");
        }
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
        List<Object> vectorValuesList = (List<Object>) rawVectorValues;
        vectorValues = new float[vectorValuesList.size()];
        for (int i = 0; i < vectorValuesList.size(); i++) {
            vectorValues[i] = Float.parseFloat(vectorValuesList.get(i).toString());
        }
        return vectorValues;
    }

    protected Projection[] getProjectionFromOp(ParsedOp op, long l) {
        Projection[] projection = null;
        Optional<LongFunction<Map>> projectionFunction = op.getAsOptionalFunction("projection", Map.class);
        if (projectionFunction.isPresent()) {
            Map<String,List<String>> projectionFields = projectionFunction.get().apply(l);
            for (Map.Entry<String,List<String>> field : projectionFields.entrySet()) {
                List<String> includeFields = field.getValue();
                StringBuffer sb = new StringBuffer();
                for (String includeField : includeFields) {
                    sb.append(includeField).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                if (field.getKey().equalsIgnoreCase("include")) {
                    projection = Projection.include(sb.toString());
                } else if (field.getKey().equalsIgnoreCase("exclude")) {
                    projection = Projection.exclude(sb.toString());
                } else {
                    logger.error("Projection " + field + " not supported");
                }
            }
        }
        return projection;
    }

    protected Boolean getUpsertFromOp(ParsedOp op, long l) {
        Optional<LongFunction<Boolean>> upsertFunction = op.getAsOptionalFunction("upsert", Boolean.class);
        if (upsertFunction.isPresent()) {
            LongFunction<Boolean> uf = upsertFunction.get();
            return uf.apply(l);
        }
        return null;
    }

    protected CollectionDefinition getCollectionDefinitionFromOp(ParsedOp op, long l) {
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
                // paramFunc ignored (due to signature of vectorize method)
                optionsBldr = optionsBldr.vectorize(pf.apply(l), mf.apply(l));
            }
        }
        Optional<LongFunction<List>> allowFunc = op.getAsOptionalFunction("allowIndex", List.class);
        if (allowFunc.isPresent()) {
            LongFunction<List> af = allowFunc.get();
            optionsBldr = optionsBldr.indexingAllow(Arrays.toString(af.apply(l).toArray(new String[0])));
        }
        Optional<LongFunction<List>> denyFunc = op.getAsOptionalFunction("denyIndex", List.class);
        if (denyFunc.isPresent()) {
            LongFunction<List> df = denyFunc.get();
            optionsBldr = optionsBldr.indexingDeny(Arrays.toString(df.apply(l).toArray(new String[0])));
        }

        return optionsBldr;
    }

}
