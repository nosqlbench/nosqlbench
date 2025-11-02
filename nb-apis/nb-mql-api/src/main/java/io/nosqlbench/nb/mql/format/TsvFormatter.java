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

import io.nosqlbench.nb.mql.query.QueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Formats query results as TSV (Tab-Separated Values) for data processing tools.
 * Compatible with awk, cut, paste, and other Unix text processing utilities.
 */
public class TsvFormatter implements ResultFormatter {

    private final boolean includeHeaders;

    public TsvFormatter() {
        this(true);
    }

    public TsvFormatter(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    @Override
    public String format(QueryResult result) {
        StringBuilder sb = new StringBuilder();

        // Header row
        if (includeHeaders) {
            sb.append(String.join("\t", result.columns()));
            sb.append("\n");
        }

        // Data rows
        for (Map<String, Object> row : result.rows()) {
            List<String> values = new ArrayList<>();
            for (String column : result.columns()) {
                Object value = row.get(column);
                // TSV doesn't require quoting - just convert to string
                // Tabs and newlines in data should be replaced
                String strValue = value != null ? value.toString() : "";
                strValue = strValue.replace("\t", " ").replace("\n", " ").replace("\r", "");
                values.add(strValue);
            }
            sb.append(String.join("\t", values));
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String getName() {
        return "tsv";
    }

    @Override
    public String getFileExtension() {
        return "tsv";
    }
}
