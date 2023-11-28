/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.datamappers.functions.hdf_to_cql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DefaultDatasetParser implements DatasetParser {
    private static final String WHERE = "WHERE";
    private static final String MATCH = "match";
    private static final String AND = "and";
    private static final String OR = "or";
    private static final String EQ = "=";
    private static final String IN = "IN";
    private static final String CONDITIONS = "conditions";
    private static final String VALUE = "value";
    private static final String SPACE = " ";
    private static final String SINGLE_QUOTE = "'";
    private static final String COMMA = ",";
    private static final String LEFT_PAREN = "(";
    private static final String RIGHT_PAREN = ")";
    private final Map<String, List<Operation>> operations = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(DefaultDatasetParser.class);

    private record Operation(String value, String operator, String conjunction, boolean isNumeric) {
    }

    @Override
    public String parse(String raw) {
        logger.debug(() -> "Parsing: " + raw);
        operations.clear();
        JsonObject conditions = JsonParser.parseString(raw).getAsJsonObject().get(CONDITIONS).getAsJsonObject();
        if (conditions.has(AND)) {
            parseAndConditions(conditions);
        }
        if (conditions.has(OR)) {
            parseOrConditions(conditions);
        }
        return buildCql();
    }

    public String parseInline(String raw) {
        logger.debug(() -> "Parsing: " + raw);
        JsonObject conditions = JsonParser.parseString(raw).getAsJsonObject().get(CONDITIONS).getAsJsonObject();
        if (conditions.has(AND)) {
            return parseAndConditionsInline(conditions);
        } else if (conditions.has(OR)) {
            return parseOrConditionsInline(conditions);
        } else {
            throw new RuntimeException("Unknown predicate type: " + conditions.keySet());
        }
    }

    private String buildCql() {
        StringBuilder sb = new StringBuilder();
        if (!operations.isEmpty()) {
            sb.append(WHERE);
            String currentField = null;
            for (int i = 0; i < operations.size(); i++) {
                Map.Entry<String, List<Operation>> entry = (Map.Entry<String, List<Operation>>) operations.entrySet().toArray()[i];
                String k = entry.getKey();
                List<Operation> v = entry.getValue();
                for (int j = 0; j < v.size(); j++) {
                    Operation op = v.get(j);
                    switch (op.operator()) {
                        case IN -> {
                            if (i == 0 && j == 0) {
                                sb.append(SPACE).append(k).append(SPACE).append(op.operator()).append(SPACE).append(LEFT_PAREN);
                                currentField = k;
                            } else {
                                if (currentField != null &&
                                    !currentField.equals(k)) {
                                    throw new RuntimeException("Cannot have multiple IN clauses with different fields");
                                }
                            }
                            if (op.isNumeric()) {
                                sb.append(op.value());
                            } else {
                                sb.append(SINGLE_QUOTE).append(op.value()).append(SINGLE_QUOTE);
                            }
                            if ((i == operations.size() - 1) && (j == v.size() - 1)) {
                                sb.append(RIGHT_PAREN);
                            } else {
                                sb.append(COMMA);
                            }
                        }
                        case EQ -> {
                            if (i != 0) sb.append(SPACE).append(op.conjunction());
                            if (!op.isNumeric()) {
                                sb.append(SPACE).append(k).append(op.operator()).append(SINGLE_QUOTE).append(op.value()).append(SINGLE_QUOTE);
                            } else {
                                sb.append(SPACE).append(k).append(op.operator()).append(op.value());
                            }
                        }
                        default -> throw new RuntimeException("Unknown operator: " + op.operator());
                    }

                }
            }
        }

        return sb.toString();
    }

    private void parseOrConditions(JsonObject conditions) {
        conditions.get(OR).getAsJsonArray().forEach(e -> {
            JsonObject condition = e.getAsJsonObject();
            String field = condition.keySet().iterator().next();
            JsonElement match = condition.get(field).getAsJsonObject().get(MATCH);
            if (match != null) {
                boolean isNumeric = match.getAsJsonObject().get(VALUE).isJsonPrimitive() &&
                    match.getAsJsonObject().get(VALUE).getAsJsonPrimitive().isNumber();
                if (operations.containsKey(field)) {
                    operations.get(field).add(new Operation(match.getAsJsonObject().get(VALUE).getAsString(), IN, OR, isNumeric));
                } else {
                    operations.put(field, new ArrayList<>(Collections.singleton(
                        new Operation(match.getAsJsonObject().get(VALUE).getAsString(), IN, OR, isNumeric))));
                    }
            } else {
                throw new RuntimeException("Unknown predicate type: " + condition.keySet());
            }
        });
    }

    private String parseOrConditionsInline(JsonObject conditions) {
        StringBuilder builder = new StringBuilder(WHERE);
        boolean first = true;
        for (JsonElement e : conditions.get(OR).getAsJsonArray()) {
            JsonObject condition = e.getAsJsonObject();
            String field = condition.keySet().iterator().next();
            JsonElement match = condition.get(field).getAsJsonObject().get(MATCH);
            if (match != null) {
                if (first) {
                    builder.append(SPACE).append(field).append(SPACE).append(IN).append(LEFT_PAREN);
                    first = false;
                } else {
                    builder.append(COMMA);
                }
                boolean isNumeric = match.getAsJsonObject().get(VALUE).isJsonPrimitive() &&
                    match.getAsJsonObject().get(VALUE).getAsJsonPrimitive().isNumber();

                builder.append(
                    isNumeric ?
                        match.getAsJsonObject().get(VALUE).getAsString() :
                        SINGLE_QUOTE + match.getAsJsonObject().get(VALUE).getAsString() + SINGLE_QUOTE
                );
            } else {
                logger.error(() -> "No match found for: " + condition.keySet());
            }
        }
        builder.append(RIGHT_PAREN);
        return builder.toString();
    }

    private void parseAndConditions(JsonObject conditions) {
        conditions.get(AND).getAsJsonArray().forEach(e -> {
            JsonObject condition = e.getAsJsonObject();
            String field = condition.keySet().iterator().next();
            JsonElement match = condition.get(field).getAsJsonObject().get(MATCH);
            if (match != null) {
                boolean isNumeric = match.getAsJsonObject().get(VALUE).isJsonPrimitive() &&
                    match.getAsJsonObject().get(VALUE).getAsJsonPrimitive().isNumber();
                if (operations.containsKey(field)) {
                    operations.get(field).add(new Operation(match.getAsJsonObject().get(VALUE).getAsString(), EQ, AND, isNumeric));
                } else {
                    operations.put(field, new ArrayList<>(Collections.singleton(
                        new Operation(match.getAsJsonObject().get(VALUE).getAsString(), EQ, AND, isNumeric))));
                }
            } else {
                throw new RuntimeException("Unknown predicate type: " + condition.keySet());
            }
        });
    }

    private String parseAndConditionsInline(JsonObject conditions) {
        StringBuilder builder = new StringBuilder(WHERE);
        boolean first = true;
        for (JsonElement e : conditions.get(AND).getAsJsonArray()) {
            JsonObject condition = e.getAsJsonObject();
            String field = condition.keySet().iterator().next();
            JsonElement match = condition.get(field).getAsJsonObject().get(MATCH);
            if (match != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(SPACE).append(AND);
                }
                boolean isNumeric = match.getAsJsonObject().get(VALUE).isJsonPrimitive() &&
                    match.getAsJsonObject().get(VALUE).getAsJsonPrimitive().isNumber();
                builder.append(SPACE).append(field).append(EQ);
                builder.append(
                    isNumeric ?
                        match.getAsJsonObject().get(VALUE).getAsString() :
                        SINGLE_QUOTE + match.getAsJsonObject().get(VALUE).getAsString() + SINGLE_QUOTE
                );
            } else {
                logger.error(() -> "No match found for: " + condition.keySet());
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        DefaultDatasetParser parser = new DefaultDatasetParser();
        String test1 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": 53}}}]}}";
        String test2 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": \"thirteen\"}}}, {\"b\": {\"match\": {\"value\": 54}}}]}}";
        String test3 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": \"thirteen\"}}}, {\"b\": {\"match\": {\"value\": 54}}},  {\"a\": {\"match\": {\"value\": 154}}}]}}";
        String test4 = "{\"conditions\": {\"or\": [{\"a\": {\"match\": {\"value\": \"nine\"}}}, {\"a\": {\"match\": {\"value\": 71}}}]}}";
        String test5 = "{\"conditions\": {\"or\": [{\"a\": {\"match\": {\"value\": \"nine\"}}}, {\"a\": {\"match\": {\"value\": 71}}}, {\"a\": {\"match\": {\"value\": 7}}}]}}";
        String test6 = "{\"conditions\": {\"or\": [{\"a\": {\"match\": {\"value\": 99}}}, {\"b\": {\"match\": {\"value\": 71}}}]}}";
        System.out.println(parser.parseInline(test1));
        System.out.println(parser.parseInline(test2));
        System.out.println(parser.parseInline(test3));
        System.out.println(parser.parseInline(test4));
        System.out.println(parser.parseInline(test5));
        System.out.println(parser.parseInline(test6));
    }
}
