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
import java.util.concurrent.atomic.AtomicBoolean;

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
    private Map<String, Set<Operation>> operations = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(DefaultDatasetParser.class);

    private static class Operation {
        private final String value;
        private final String operator;
        private final String conjunction;

        public Operation(String value, String operator, String conjunction) {
            this.value = value;
            this.operator = operator;
            this.conjunction = conjunction;
        }
        public String getValue() {
            return value;
        }
        public String getOperator() {
            return operator;
        }
        public String getConjunction() {
            return conjunction;
        }
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

    private String buildCql() {
        StringBuffer sb = new StringBuffer();
        if (!operations.isEmpty()) {
            sb.append(WHERE);
            AtomicBoolean initial = new AtomicBoolean(true);
            operations.forEach((k, v) -> {
                v.forEach(e -> {
                    if (initial.get()) {
                        initial.set(false);
                    } else {
                        sb.append(SPACE).append(e.getConjunction());
                    }
                    //TODO: String values need to be quoted, numerics not so much
                    switch (e.getOperator()) {
                        case IN ->
                            sb.append(" ").append(k).append(" ").append(e.getOperator()).append(" (").append(e.getValue()).append(")");
                        case EQ ->
                            sb.append(" ").append(k).append(e.getOperator()).append("'").append(e.getValue()).append("'");
                        default ->
                            throw new RuntimeException("Unknown operator: " + e.getOperator());
                    }

                });
            });
        }

        return sb.toString();
    }

    private void parseOrConditions(JsonObject conditions) {
        conditions.get(OR).getAsJsonArray().forEach(e -> {
            JsonObject condition = e.getAsJsonObject();
            String field = condition.keySet().iterator().next();
            //TODO: Determine if we need to support more than just match
            JsonElement match = condition.get(field).getAsJsonObject().get(MATCH);
            if (match != null) {
                if (operations.containsKey(field)) {
                    operations.get(field).add(new Operation(match.getAsJsonObject().get(VALUE).getAsString(), IN, OR));
                } else {
                    operations.put(field, new HashSet<>(Collections.singleton(
                        new Operation(match.getAsJsonObject().get(VALUE).getAsString(), IN, OR))));
                    }
            } else {
                throw new RuntimeException("Unknown predicate type: " + condition.keySet());
            }
        });
    }

    private void parseAndConditions(JsonObject conditions) {
        conditions.get(AND).getAsJsonArray().forEach(e -> {
            JsonObject condition = e.getAsJsonObject();
            String field = condition.keySet().iterator().next();
            //TODO: Determine if we need to support more than just match
            JsonElement match = condition.get(field).getAsJsonObject().get(MATCH);
            if (match != null) {
                if (operations.containsKey(field)) {
                    operations.get(field).add(new Operation(match.getAsJsonObject().get(VALUE).getAsString(), EQ, AND));
                } else {
                    operations.put(field, new HashSet<>(Collections.singleton(
                        new Operation(match.getAsJsonObject().get(VALUE).getAsString(), EQ, AND))));
                }
            } else {
                throw new RuntimeException("Unknown predicate type: " + condition.keySet());
            }
        });
    }

    public static void main(String[] args) {
        DefaultDatasetParser parser = new DefaultDatasetParser();
        String test1 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": 53}}}]}}";
        String test2 = "{\"conditions\": {\"and\": [{\"a\": {\"match\": {\"value\": 13}}}, {\"b\": {\"match\": {\"value\": 54}}}]}}";
        String test3 = "{\"conditions\": {\"or\": [{\"a\": {\"match\": {\"value\": 99}}}, {\"a\": {\"match\": {\"value\": 71}}}]}}";
        String test4 = "{\"conditions\": {\"or\": [{\"a\": {\"match\": {\"value\": 99}}}, {\"b\": {\"match\": {\"value\": 71}}}]}}";
        System.out.println(parser.parse(test1));
        System.out.println(parser.parse(test2));
        System.out.println(parser.parse(test3));
        System.out.println(parser.parse(test4));
    }
}
