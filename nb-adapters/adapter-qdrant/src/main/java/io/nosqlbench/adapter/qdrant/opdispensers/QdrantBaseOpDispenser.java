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

import com.google.protobuf.Timestamp;
import io.nosqlbench.adapter.qdrant.QdrantDriverAdapter;
import io.nosqlbench.adapter.qdrant.QdrantSpace;
import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.Condition;
import io.qdrant.client.grpc.Points.DatetimeRange;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

import static io.qdrant.client.ConditionFactory.*;

public abstract class QdrantBaseOpDispenser<REQUEST,RESULT>
    extends BaseOpDispenser<QdrantBaseOp<?,?>, QdrantSpace> {

    protected final LongFunction<QdrantSpace> qdrantSpaceFunction;
    protected final LongFunction<QdrantClient> clientFunction;
    private final LongFunction<? extends QdrantBaseOp<REQUEST, RESULT>> opF;
    private final LongFunction<REQUEST> paramF;

    protected QdrantBaseOpDispenser(QdrantDriverAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super((DriverAdapter)adapter, op);
        this.qdrantSpaceFunction = adapter.getSpaceFunc(op);
        this.clientFunction = (long l) -> this.qdrantSpaceFunction.apply(l).getClient();
        this.paramF = getParamFunc(this.clientFunction,op,targetF);
        this.opF = createOpFunc(paramF, this.clientFunction, op, targetF);
    }
    protected QdrantDriverAdapter getDriverAdapter() {
        return (QdrantDriverAdapter) adapter;
    }

    public abstract LongFunction<REQUEST> getParamFunc(
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    public abstract LongFunction<QdrantBaseOp<REQUEST, RESULT>> createOpFunc(
        LongFunction<REQUEST> paramF,
        LongFunction<QdrantClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    @Override
    public QdrantBaseOp<REQUEST, RESULT> getOp(long value) {
        return opF.apply(value);
    }

    /**
     * Builds the complete {@link Filter.Builder} from the {@code ParsedOp}
     *
     * @param {@link ParsedOp}
     * @return {@code LongFunction<Filter.Builder>}
     * @see <a href="https://qdrant.tech/documentation/concepts/filtering">Filtering</a>
     */
    protected LongFunction<Filter.Builder> getFilterFromOp(ParsedOp op) {
        Optional<LongFunction<List>> filterFunc = op.getAsOptionalFunction("filter", List.class);
        return filterFunc.<LongFunction<Filter.Builder>>map(filterListLongF -> l -> {
            Filter.Builder filterBuilder = Filter.newBuilder();
            List<Map<String, Object>> filters = filterListLongF.apply(l);
            return constructFilterBuilder(filters, filterBuilder);
        }).orElse(null);
    }

    private Filter.Builder constructFilterBuilder(List<Map<String, Object>> filters, Filter.Builder filterBuilder) {
        List<Condition> mustClauseList = new ArrayList<>();
        List<Condition> mustNotClauseList = new ArrayList<>();
        List<Condition> shouldClauseList = new ArrayList<>();
        for (Map<String, Object> filterFields : filters) {
            switch ((String) filterFields.get("clause")) {
                case "must" -> {
                    logger.debug("[QdrantBaseOpDispenser] - Building 'must' filter clause");
                    switch (filterFields.get("condition").toString()) {
                        case "match" -> //filterBuilder.addMust(getMatchFilterCondition(filterFields));
                            mustClauseList.add(getMatchFilterCondition(filterFields));
                        case "match_any" -> mustClauseList.add(getMatchAnyExceptCondition(filterFields, "match_any"));
                        case "match_except" ->
                            mustClauseList.add(getMatchAnyExceptCondition(filterFields, "match_except"));
                        case "text" -> mustClauseList.add(getMatchTextCondition(filterFields));
                        case "range" -> mustClauseList.add(getRangeCondition(filterFields));
                        case "geo_bounding_box" -> mustClauseList.add(getGeoBoundingBoxCondition(filterFields));
                        case "geo_radius" -> mustClauseList.add(getGeoRadiusCondition(filterFields));
                        case "geo_polygon" -> mustClauseList.add(getGeoPolygonCondition(filterFields));
                        case "values_count" -> mustClauseList.add(getValuesCountCondition(filterFields));
                        case "is_empty" -> mustClauseList.add(getIsEmptyCondition(filterFields));
                        case "is_null" -> mustClauseList.add(getIsNullCondition(filterFields));
                        case "has_id" -> mustClauseList.add(getHasIdCondition(filterFields));
                        case "nested" -> mustClauseList.add(getNestedCondition(filterFields));
                        default -> logger.warn("Filter condition '{}' is not supported", filterFields.get("condition"));
                    }
                }
                case "must_not" -> {
                    logger.debug("[QdrantBaseOpDispenser] - Building 'must_not' filter clause");
                    switch (filterFields.get("condition").toString()) {
                        case "match" -> mustNotClauseList.add(getMatchFilterCondition(filterFields));
                        case "match_any" ->
                            mustNotClauseList.add(getMatchAnyExceptCondition(filterFields, "match_any"));
                        case "match_except" ->
                            mustNotClauseList.add(getMatchAnyExceptCondition(filterFields, "match_except"));
                        case "range" -> mustNotClauseList.add(getRangeCondition(filterFields));
                        case "geo_bounding_box" -> mustNotClauseList.add(getGeoBoundingBoxCondition(filterFields));
                        case "geo_radius" -> mustNotClauseList.add(getGeoRadiusCondition(filterFields));
                        case "values_count" -> mustNotClauseList.add(getValuesCountCondition(filterFields));
                        case "is_empty" -> mustNotClauseList.add(getIsEmptyCondition(filterFields));
                        case "is_null" -> mustNotClauseList.add(getIsNullCondition(filterFields));
                        case "has_id" -> mustClauseList.add(getHasIdCondition(filterFields));
                        default -> logger.warn("Filter condition '{}' is not supported", filterFields.get("condition"));
                    }
                }
                case "should" -> {
                    logger.debug("[QdrantBaseOpDispenser] - Building 'should' filter clause");
                    switch (filterFields.get("condition").toString()) {
                        case "match" -> shouldClauseList.add(getMatchFilterCondition(filterFields));
                        case "match_any" -> shouldClauseList.add(getMatchAnyExceptCondition(filterFields, "match_any"));
                        case "match_except" ->
                            shouldClauseList.add(getMatchAnyExceptCondition(filterFields, "match_except"));
                        case "range" -> shouldClauseList.add(getRangeCondition(filterFields));
                        case "geo_bounding_box" -> shouldClauseList.add(getGeoBoundingBoxCondition(filterFields));
                        case "geo_radius" -> shouldClauseList.add(getGeoRadiusCondition(filterFields));
                        case "values_count" -> shouldClauseList.add(getValuesCountCondition(filterFields));
                        case "is_empty" -> shouldClauseList.add(getIsEmptyCondition(filterFields));
                        case "is_null" -> shouldClauseList.add(getIsNullCondition(filterFields));
                        case "has_id" -> mustClauseList.add(getHasIdCondition(filterFields));
                        default -> logger.warn("Filter condition '{}' is not supported", filterFields.get("condition"));
                    }
                }
                default -> logger.error("Clause '{}' is not supported", filterFields.get("clause"));
            }
        }
        if (!mustClauseList.isEmpty()) {
            filterBuilder.addAllMust(mustClauseList);
        }
        if (!mustNotClauseList.isEmpty()) {
            filterBuilder.addAllMustNot(mustNotClauseList);
        }
        if (!shouldClauseList.isEmpty()) {
            filterBuilder.addAllShould(shouldClauseList);
        }
        return filterBuilder;
    }

    private Condition getMatchFilterCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'match' filter condition");
        if (filterFields.get("value") instanceof String) {
            return matchKeyword((String) filterFields.get("key"), (String) filterFields.get("value"));
        } else if (filterFields.get("value") instanceof Number) {
            return match((String) filterFields.get("key"), ((Number) filterFields.get("value")).intValue());
        } else if (filterFields.get("value") instanceof Boolean) {
            return match((String) filterFields.get("key"), ((Boolean) filterFields.get("value")));
        } else if (filterFields.containsKey("text")) {
            // special case of 'match'
            // https://qdrant.tech/documentation/concepts/filtering/#full-text-match
            return getMatchTextCondition(filterFields);
        } else {
            throw new OpConfigError("Unsupported value type [" + filterFields.get("value").getClass().getSimpleName() + "] for 'match' condition");
        }
    }

    private Condition getMatchAnyExceptCondition(Map<String, Object> filterFields, String filterCondition) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'match_any'/'match_except' filter condition");
        if (filterFields.get("value") instanceof List) {
            switch (((List<?>) filterFields.get("value")).getFirst().getClass().getSimpleName()) {
                case "String" -> {
                    if ("match_any".equals(filterCondition)) {
                        return matchKeywords(filterFields.get("key").toString(), (List<String>) filterFields.get("value"));
                    } else if ("match_except".equals(filterCondition)) {
                        return matchExceptKeywords(filterFields.get("key").toString(), (List<String>) filterFields.get("value"));
                    } else {
                        throw new OpConfigError("Unsupported filter condition [" + filterCondition + "]");
                    }
                }
                case "Long" -> {
                    if ("match_any".equals(filterCondition)) {
                        return matchValues(filterFields.get("key").toString(), (List<Long>) filterFields.get("value"));
                    } else if ("match_except".equals(filterCondition)) {
                        return matchExceptValues(filterFields.get("key").toString(), (List<Long>) filterFields.get("value"));
                    } else {
                        throw new OpConfigError("Unsupported filter condition [" + filterCondition + "]");
                    }
                }
                case "Integer" -> {
                    List<Long> convertedIntToLongValues = new ArrayList<>();
                    for (Integer intVal : (List<Integer>) filterFields.get("value")) {
                        convertedIntToLongValues.add(intVal.longValue());
                    }
                    if ("match_any".equals(filterCondition)) {
                        return matchValues(filterFields.get("key").toString(), convertedIntToLongValues);
                    } else if ("match_except".equals(filterCondition)) {
                        return matchExceptValues(filterFields.get("key").toString(), convertedIntToLongValues);
                    } else {
                        throw new OpConfigError("Unsupported filter condition [" + filterCondition + "]");
                    }
                }
                default -> throw new OpConfigError("Unsupported value type [" +
                    filterFields.get("value").getClass().getSimpleName() +
                    "] within value list for 'match_any'/'match_except' condition.\n" + filterFields.get("value"));
            }
        } else {
            throw new OpConfigError("Unsupported value type [" + filterFields.get("value").getClass().getSimpleName() +
                "] for 'match_any'/'match_except' condition");
        }
    }

    private Condition getMatchTextCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'match_text' filter condition");
        if (filterFields.get("text") instanceof String) {
            return matchText((String) filterFields.get("key"), (String) filterFields.get("text"));
        } else {
            throw new OpConfigError("Unsupported value type [" +
                filterFields.get("text").getClass().getSimpleName() + "] for 'match -> text' condition");
        }
    }

    private Condition getRangeCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'range' filter condition");
        if (filterFields.get("value") instanceof Map) {
            if (((Map<String, ?>) filterFields.get("value")).keySet().stream().noneMatch(key ->
                key.equals("gte") || key.equals("lte") || key.equals("lt") || key.equals("gt"))) {
                throw new OpConfigError("Only gte/gt/lte/lt is expected for range condition, but received: "
                    + ((Map<String, ?>) filterFields.get("value")).keySet());
            }

            Range.Builder rangeBuilder = Range.newBuilder();
            DatetimeRange.Builder datetimerangeBuilder = DatetimeRange.newBuilder();

            ((Map<?, ?>) filterFields.get("value")).forEach((rKey, rValue) -> {
                if (rValue != null) {
                    if (rValue instanceof Number) {
                        switch ((String) rKey) {
                            case "gte" -> rangeBuilder.setGte(((Number) rValue).doubleValue());
                            case "gt" -> rangeBuilder.setGt(((Number) rValue).doubleValue());
                            case "lte" -> rangeBuilder.setLte(((Number) rValue).doubleValue());
                            case "lt" -> rangeBuilder.setLt(((Number) rValue).doubleValue());
                        }
                    } else if (rValue instanceof String) {
                        // This is now a https://qdrant.tech/documentation/concepts/filtering/#datetime-range type
                        long rVal = Instant.parse(String.valueOf(rValue)).getEpochSecond();
                        Timestamp.Builder timestampBuilder = Timestamp.newBuilder().setSeconds(rVal);
                        switch ((String) rKey) {
                            case "gte" -> datetimerangeBuilder.setGte(timestampBuilder);
                            case "gt" -> datetimerangeBuilder.setGt(timestampBuilder);
                            case "lte" -> datetimerangeBuilder.setLte(timestampBuilder);
                            case "lt" -> datetimerangeBuilder.setLt(timestampBuilder);
                        }
                    } else {
                        logger.warn("Unsupported value [{}] ignored for 'range' filter condition.", rValue);
                    }
                }
            });
            if (datetimerangeBuilder.hasGt() || datetimerangeBuilder.hasGte()
                || datetimerangeBuilder.hasLte() || datetimerangeBuilder.hasLt()) {
                return datetimeRange((String) filterFields.get("key"), datetimerangeBuilder.build());
            } else {
                // we assume here this is Range type
                if (rangeBuilder.hasGt() || rangeBuilder.hasGte() || rangeBuilder.hasLte() || rangeBuilder.hasLt())
                    return range((String) filterFields.get("key"), rangeBuilder.build());
            }
        } else {
            throw new OpConfigError("Unsupported value type [" + filterFields.get("value").getClass().getSimpleName() +
                "] for 'range' condition. Needs a list containing gt/gte/lt/lte");
        }
        return null;
    }

    private Condition getGeoBoundingBoxCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'geo_bounding_box' filter condition");
        if (filterFields.get("value") instanceof Map) {
            Map<String, ?> valueMap = (Map<String, ?>) filterFields.get("value");
            if (valueMap.keySet().stream().noneMatch(key -> key.equals("bottom_right") || key.equals("top_left"))) {
                throw new OpConfigError("Both top_left & bottom_right are expected for geo_bounding_box condition, " +
                    "but received: " + valueMap.keySet());
            }

            valueMap.forEach((rKey, rValue) -> {
                if (rValue instanceof Map) {
                    if (((Map<String, Number>) rValue).keySet().stream().noneMatch(geoLatLon ->
                        geoLatLon.equals("lat") || geoLatLon.equals("lon"))) {
                        throw new OpConfigError("Both 'top_left' & 'bottom_right' for 'geo_bounding_box' are expected" +
                            " to have both 'lat' & 'lon' fields");
                    }
                } else {
                    throw new OpConfigError("Unsupported value [" + rValue + "] ignored for 'geo_bounding_box' filter condition.");
                }
            });
            double topLeftLat = (((Map<String, Number>) valueMap.get("top_left")).get("lat")).doubleValue();
            double topLeftLon = (((Map<String, Number>) valueMap.get("top_left")).get("lon")).doubleValue();
            double bottomRightLat = (((Map<String, Number>) valueMap.get("bottom_right")).get("lat")).doubleValue();
            double bottomRightLon = (((Map<String, Number>) valueMap.get("bottom_right")).get("lon")).doubleValue();

            return Condition.newBuilder()
                .setField(Points.FieldCondition.newBuilder()
                    .setKey((String) filterFields.get("key"))
                    .setGeoBoundingBox(Points.GeoBoundingBox.newBuilder()
                        .setTopLeft(Points.GeoPoint.newBuilder()
                            .setLat(topLeftLat)
                            .setLon(topLeftLon)
                            .build())
                        .setBottomRight(Points.GeoPoint.newBuilder()
                            .setLat(bottomRightLat)
                            .setLon(bottomRightLon)
                            .build())
                        .build())
                    .build())
                .build();
        } else {
            throw new OpConfigError("Unsupported value type [" + filterFields.get("value").getClass().getSimpleName() + "]" +
                " for 'geo_bounding_box' condition. Needs a map containing 'top_left' & 'bottom_right' with 'lat' & 'lon'");
        }
    }

    private Condition getGeoRadiusCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'geo_radius' filter condition");
        if (filterFields.get("value") instanceof Map) {
            Map<String, ?> valueMap = (Map<String, ?>) filterFields.get("value");
            if (valueMap.keySet().stream().noneMatch(key -> key.equals("center") || key.equals("radius"))) {
                throw new OpConfigError("Both 'center' & 'radius' are expected for 'geo_radius' condition, " +
                    "but received: " + valueMap.keySet());
            }

            valueMap.forEach((rKey, rValue) -> {
                if (rKey.equals("center")) {
                    if (rValue instanceof Map) {
                        if (((Map<String, Number>) rValue).keySet().stream().noneMatch(geoLatLon ->
                            geoLatLon.equals("lat") || geoLatLon.equals("lon"))) {
                            throw new OpConfigError("Both 'lat' & 'lon' within 'center' are expected" +
                                " for the 'geo_radius' condition");
                        }
                    } else {
                        throw new OpConfigError("Unsupported value [" + rValue + "] ignored for 'geo_radius'" +
                            " -> 'center' filter condition.");
                    }
                }
            });
            double centerLat = (((Map<String, Number>) valueMap.get("center")).get("lat")).doubleValue();
            double centerLon = (((Map<String, Number>) valueMap.get("center")).get("lon")).doubleValue();
            float radius = ((Number) valueMap.get("radius")).floatValue();

            return Condition.newBuilder()
                .setField(Points.FieldCondition.newBuilder()
                    .setKey((String) filterFields.get("key"))
                    .setGeoRadius(Points.GeoRadius.newBuilder()
                        .setCenter(Points.GeoPoint.newBuilder()
                            .setLat(centerLat)
                            .setLon(centerLon)
                            .build())
                        .setRadius(radius)
                        .build())
                    .build())
                .build();
        } else {
            throw new OpConfigError("Unsupported value type [" + filterFields.get("value").getClass().getSimpleName() + "]" +
                " for 'geo_radius' condition. Needs a map containing 'center' ('lat' & 'lon') and 'radius' fields");
        }
    }

    private Condition getGeoPolygonCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'geo_polygon' filter condition");
        if (filterFields.get("value") instanceof Map) {
            Map<String, ?> valueMap = (Map<String, ?>) filterFields.get("value");
            if (valueMap.keySet().stream().noneMatch(key -> key.equals("exterior_points") || key.equals("interior_points"))) {
                throw new OpConfigError("Both 'exterior_points' & 'interior_points' with lat/lon array is required" +
                    " for 'geo_polygon' filter condition");
            }
            Points.GeoLineString.Builder exteriorPoints = Points.GeoLineString.newBuilder();
            List<Points.GeoPoint> extPoints = new ArrayList<>();
            Points.GeoLineString.Builder interiorPoints = Points.GeoLineString.newBuilder();
            List<Points.GeoPoint> intPoints = new ArrayList<>();
            valueMap.forEach((gpKey, gpVal) -> {
                switch ((String) gpKey) {
                    case "exterior_points" -> {
                        ((List<Map<String, Number>>) gpVal).forEach((endEp) -> {
                            extPoints.add(Points.GeoPoint.newBuilder()
                                .setLat((endEp).get("lat").doubleValue())
                                .setLon((endEp).get("lon").doubleValue())
                                .build());
                        });
                    }
                    case "interior_points" -> {
                        ((List<Map<String, Number>>) gpVal).forEach((endIp) -> {
                            intPoints.add(Points.GeoPoint.newBuilder()
                                .setLat((endIp).get("lat").doubleValue())
                                .setLon((endIp).get("lon").doubleValue())
                                .build());
                        });
                    }
                }
            });
            exteriorPoints.addAllPoints(extPoints);
            interiorPoints.addAllPoints(intPoints);
            return geoPolygon((String) filterFields.get("key"), exteriorPoints.build(), List.of(interiorPoints.build()));
        } else {
            throw new OpConfigError("Unsupported type [" + filterFields.get("value").getClass().getSimpleName() +
                "] passed for 'geo_polygon' filter condition");
        }
    }

    private Condition getValuesCountCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'values_count' filter condition");
        if (filterFields.get("value") instanceof Map) {
            Map<String, ?> valueMap = (Map<String, ?>) filterFields.get("value");
            if (valueMap.keySet().stream().noneMatch(key -> key.equals("gte") || key.equals("gt")
                || key.equals("lte") || key.equals("lt"))) {
                throw new OpConfigError("Only 'gte', 'gt', 'lte' or 'lt' is expected for 'values_count' condition, " +
                    "but received: " + valueMap.keySet());
            }
            Points.ValuesCount.Builder valCntBuilder = Points.ValuesCount.newBuilder();
            valueMap.forEach((vcKey, vcValue) -> {
                if (vcValue != null) {
                    switch (vcKey) {
                        case "gte" -> valCntBuilder.setGte(((Number) vcValue).longValue());
                        case "gt" -> valCntBuilder.setGt(((Number) vcValue).longValue());
                        case "lte" -> valCntBuilder.setLte(((Number) vcValue).longValue());
                        case "lt" -> valCntBuilder.setLt(((Number) vcValue).longValue());
                    }
                }
            });

            return Condition.newBuilder()
                .setField(Points.FieldCondition.newBuilder()
                    .setKey((String) filterFields.get("key"))
                    .setValuesCount(valCntBuilder)
                    .build())
                .build();
        } else {
            throw new OpConfigError("Unsupported type [" + filterFields.get("value").getClass().getSimpleName() + "]" +
                " for 'values_count' filter condition");
        }
    }

    private Condition getIsNullCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'is_null' filter condition");
        return Condition.newBuilder()
            .setIsNull(Points.IsNullCondition.newBuilder()
                .setKey((String) filterFields.get("key"))
                .build())
            .build();
    }

    private Condition getIsEmptyCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'is_empty' filter condition");
        return Condition.newBuilder()
            .setIsEmpty(Points.IsEmptyCondition.newBuilder()
                .setKey((String) filterFields.get("key"))
                .build())
            .build();
    }

    /**
     * This {@link io.qdrant.client.grpc.Points.NestedCondition} is only valid within a 'must' filter condition.
     *
     * @param filterFields
     * @return
     * @see <a href="https://qdrant.tech/documentation/concepts/filtering/#nested-object-filter">Nested Object Filter</a>
     * @see <a href="https://github.com/qdrant/java-client/blob/v1.9.1/src/main/java/io/qdrant/client/ConditionFactory.java#L220-L249">code</a>
     */
    private Condition getNestedCondition(Map<String, Object> filterFields) {
        logger.debug("[QdrantBaseOpDispenser] - Building 'nested' filter condition");
        if (filterFields.get("nested") instanceof List) {
            List<Condition> mustClauseList = new ArrayList<>();
            Filter.Builder nestedFilter = Filter.newBuilder();

            ((List<Map<String, Object>>) filterFields.get("nested")).forEach((nList) -> {
                mustClauseList.add(getMatchFilterCondition(nList));
            });
            nestedFilter.addAllMust(mustClauseList);
            return Condition.newBuilder()
                .setNested(Points.NestedCondition.newBuilder()
                    .setKey((String) filterFields.get("key"))
                    .setFilter(nestedFilter)
                    .build())
                .build();
        } else {
            throw new OpConfigError("Unsupported type [" + filterFields.get("nested").getClass().getSimpleName() + "]" +
                " for 'nested' filter condition");
        }
    }

    private Condition getHasIdCondition(Map<String, Object> filterFields) {
        if (filterFields.get("value") instanceof List) {
            List<Points.PointId> pointIds = new ArrayList<>();
            ((List<Object>) filterFields.get("value")).forEach((pId) -> {
                if (pId instanceof Number) {
                    pointIds.add(PointIdFactory.id(((Number) pId).longValue()));
                } else if (pId instanceof String) {
                    pointIds.add(Points.PointId.newBuilder().setUuid(pId.toString()).build());
                } else {
                    throw new OpConfigError("Unsupported type for 'id' specified [" + pId.getClass().getSimpleName() + "]");
                }
            });
            return Condition.newBuilder()
                .setHasId(Points.HasIdCondition.newBuilder()
                    .addAllHasId(pointIds)
                    .build())
                .build();
        } else {
            throw new OpConfigError("Unsupported type [" + filterFields.get("value").getClass().getSimpleName() + "]" +
                " for 'has_id' filter condition");
        }
    }
}
