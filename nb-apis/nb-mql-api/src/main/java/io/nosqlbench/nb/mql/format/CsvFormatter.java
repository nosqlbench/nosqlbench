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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Formats query results as CSV for spreadsheet import.
 */
public class CsvFormatter implements ResultFormatter {

    private final boolean includeHeaders;

    public CsvFormatter() {
        this(true);
    }

    public CsvFormatter(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    @Override
    public String format(QueryResult result) {
        StringWriter writer = new StringWriter();

        try {
            CSVFormat format = includeHeaders
                ? CSVFormat.DEFAULT.builder().setHeader(result.columns().toArray(new String[0])).build()
                : CSVFormat.DEFAULT;

            try (CSVPrinter printer = new CSVPrinter(writer, format)) {
                for (Map<String, Object> row : result.rows()) {
                    List<Object> values = new ArrayList<>();
                    for (String column : result.columns()) {
                        values.add(row.get(column));
                    }
                    printer.printRecord(values);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to format CSV", e);
        }

        return writer.toString();
    }

    @Override
    public String getName() {
        return "csv";
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }
}
