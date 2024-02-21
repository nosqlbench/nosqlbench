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
 *
 */

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json;

import com.google.gson.JsonObject;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.DatasetFilter;

public class SingleConditionFilterByKeyword implements DatasetFilter {
    private final String filterString;
    private static final String AND = "and";
    private static final String OR = "or";
    private static final String FIELD = "field";
    private static final String OPERATOR = "operator";
    private static final String COMPARATOR = "comparator";
    private static final String COMMA = ",";
    private static final String VALUE = "value";

    public SingleConditionFilterByKeyword(String filterString) {
        this.filterString = filterString;
    }

    @Override
    public String applyFilter(JsonObject json) {
        if (json.has(AND)) {
            return parseConditions(json, AND);
        } else if (json.has(OR)) {
            return parseConditions(json, OR);
        } else {
            throw new RuntimeException("Unknown predicate type: " + json.keySet());
        }
    }

    private String parseConditions(JsonObject json, String conditionType) {
        return switch (filterString) {
            case FIELD ->
                    json.get(conditionType).getAsJsonArray().get(0).getAsJsonObject().keySet().stream().findFirst()
                            .get().replaceAll("\"", "");
            case OPERATOR ->
                    json.get(conditionType).getAsJsonArray().get(0).getAsJsonObject().entrySet().stream().findFirst()
                            .get().getValue().getAsJsonObject().keySet().stream().findFirst().get().replaceAll("\"", "");
            case COMPARATOR ->
                    json.get(conditionType).getAsJsonArray().get(0).getAsJsonObject().entrySet().stream().findFirst()
                            .get().getValue().getAsJsonObject().entrySet().stream().findFirst().get().getValue().getAsJsonObject()
                            .get(VALUE).toString().replaceAll("\"", "");
            default -> throw new RuntimeException("Unknown filter string: " + filterString);
        };
    }

}
