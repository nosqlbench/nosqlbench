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
import com.google.gson.JsonParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.DatasetFilter;

public class MultiConditionFilter implements DatasetFilter {
    private final String filterString;
    private static final String AND = "and";
    private static final String OR = "or";
    private static final String FIELD = "field";
    private static final String OPERATOR = "operator";
    private static final String COMPARATOR = "comparator";
    private static final String COMMA = ",";
    private static final String VALUE = "value";

    //TODO: MOVE TO TEST CLASS
    private static String test = "{\n" +
        //"  \"query\": [-0.034, -0.185, -0.21, ...],\n" +
        "  \"conditions\": {\n" +
        "    \"and\": [\n" +
        "      {\n" +
        "        \"department_name\": {\n" +
        "          \"EQ\": {\n" +
        "            \"value\": \"Divided Shoes\"\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"department_type\": {\n" +
        "          \"EQ\": {\n" +
        "            \"value\": \"Footwear\"\n" +
        "          }\n" +
        "        }\n" +
        "      }\n"  +
        "    ]\n" +
        "  },\n" +
        "  \"closest_ids\": [565, 15631, 100747, ....],\n" +
        "  \"closest_scores\": [0.734, 0.698, 0.697, 0.689, ...]\n" +
        "}";
    public static void main(String[] args) {
        MultiConditionFilter mcf = new MultiConditionFilter("comparator");
        JsonObject conditions = JsonParser.parseString(test).getAsJsonObject().get("conditions").getAsJsonObject();
        mcf.applyFilter(conditions);
    }

    ///////////////////

    public MultiConditionFilter(String filterString) {
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
        StringBuilder sb = new StringBuilder();
        switch (filterString) {
            case FIELD: {
                json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet()
                    .forEach(field -> sb.append(field).append(COMMA)));
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString();
            }
            case OPERATOR: {
                json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet()
                    .forEach(field -> condition.getAsJsonObject().get(field).getAsJsonObject().keySet()
                        .forEach(operator -> sb.append(operator).append(COMMA))));
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString();
            }
            case COMPARATOR: {
                json.get(conditionType).getAsJsonArray().forEach(condition -> condition.getAsJsonObject().keySet().forEach(field -> {
                    JsonObject p = condition.getAsJsonObject().get(field).getAsJsonObject();
                    p.keySet().forEach(operator -> sb.append(p.get(operator).getAsJsonObject().get(VALUE) ).append(COMMA));
                }));
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString();
            }
            default: {
                throw new RuntimeException("Unknown filter string: " + filterString);
            }
        }
    }

}
