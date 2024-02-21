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

public class MultiConditionFilterByLevel implements DatasetFilter {
    private final int filterLevel;
    private final boolean isValue;
    private static final String AND = "and";
    private static final String OR = "or";
    private static final String COMMA = ",";
    private static final String VALUE = "value";

    public MultiConditionFilterByLevel(int filterLevel, boolean isValue) {
        this.filterLevel = filterLevel;
        this.isValue = isValue;
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
        StringBuilder sb = new StringBuilder();
        switch (filterLevel) {
            case 1: {
                if (isValue) {
                    json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet()
                        .forEach(field -> sb.append(condition.getAsJsonObject().get(field).getAsJsonObject().get(VALUE)).append(COMMA)));
                } else {
                    json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet()
                        .forEach(field -> sb.append(field).append(COMMA)));
                }
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString().replaceAll("\"", ""); // remove quotes from sb;
            }
            case 2: {
                if (isValue) {
                    json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet()
                        .forEach(field -> condition.getAsJsonObject().get(field).getAsJsonObject().keySet()
                            .forEach(operator -> sb.append(condition.getAsJsonObject().get(field).getAsJsonObject().get(operator).getAsJsonObject().get(VALUE)).append(COMMA))));
                } else {
                    json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet()
                        .forEach(field -> condition.getAsJsonObject().get(field).getAsJsonObject().keySet()
                            .forEach(operator -> sb.append(operator).append(COMMA))));
                }
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString().replaceAll("\"", ""); // remove quotes from sb;
            }
            case 3: {
                if (isValue) {
                    json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet().forEach(field -> {
                        JsonObject p = condition.getAsJsonObject().get(field).getAsJsonObject();
                        p.keySet().forEach(operator -> sb.append(p.get(operator).getAsJsonObject().get(VALUE)).append(COMMA));
                    }));
                } else {
                    json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet().forEach(field -> {
                        JsonObject p = condition.getAsJsonObject().get(field).getAsJsonObject();
                        p.keySet().forEach(operator -> sb.append(operator).append(COMMA));
                    }));
                }
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString().replaceAll("\"", ""); // remove quotes from sb;
            }
            default: {
                throw new RuntimeException("Unsupported Filter Level: " + filterLevel);
            }
        }
    }

}
