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
import io.nosqlbench.nb.mql.query.QueryResult;

import java.util.Map;

/**
 * Formats query results as JSONL (JSON Lines) for streaming data processing.
 * Each row is output as a single JSON object on its own line, with no array wrapper.
 * This format is efficient for large result sets and compatible with streaming processors.
 */
public class JsonLinesFormatter implements ResultFormatter {

    private final Gson gson;

    public JsonLinesFormatter() {
        // Compact JSON (no pretty printing for JSONL)
        this.gson = new Gson();
    }

    @Override
    public String format(QueryResult result) {
        StringBuilder sb = new StringBuilder();

        // Each row is a separate JSON object on its own line
        for (Map<String, Object> row : result.rows()) {
            sb.append(gson.toJson(row));
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String getName() {
        return "jsonl";
    }

    @Override
    public String getFileExtension() {
        return "jsonl";
    }
}
