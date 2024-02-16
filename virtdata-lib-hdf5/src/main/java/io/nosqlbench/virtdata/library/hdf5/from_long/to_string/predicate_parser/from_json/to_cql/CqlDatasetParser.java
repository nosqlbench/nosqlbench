/*
 * Copyright (c) 2023-2024 nosqlbench
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

package io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.from_json.to_cql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nosqlbench.virtdata.library.hdf5.from_long.to_string.predicate_parser.DatasetParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to parse the raw JSON from the HDF dataset into a CQL predicate. This is the default
 * implementation. It accepts a JSON string of the form found at <a href="https://github.com/qdrant/ann-filtering-benchmark-datasets">...</a>
 * and converts it into a CQL predicate in String form
 */
public class CqlDatasetParser implements DatasetParser {
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
    private static final Logger logger = LogManager.getLogger(CqlDatasetParser.class);

    @Override
    public String parse(String raw) {
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

}
