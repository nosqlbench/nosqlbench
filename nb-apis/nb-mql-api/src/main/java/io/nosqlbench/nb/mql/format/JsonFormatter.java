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

package io.nosqlbench.nb.mql.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.nb.mql.query.QueryResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formats query results as JSON for programmatic consumption.
 */
public class JsonFormatter implements ResultFormatter {

    private final Gson gson;
    private final boolean pretty;

    public JsonFormatter() {
        this(false);
    }

    public JsonFormatter(boolean pretty) {
        this.pretty = pretty;
        GsonBuilder builder = new GsonBuilder();
        if (pretty) {
            builder.setPrettyPrinting();
        }
        this.gson = builder.create();
    }

    @Override
    public String format(QueryResult result) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("columns", result.columns());
        output.put("rows", result.rows());
        output.put("rowCount", result.rowCount());
        output.put("executionTimeMs", result.executionTimeMs());

        return gson.toJson(output);
    }

    @Override
    public String getName() {
        return "json";
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}
