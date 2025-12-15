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

package io.nosqlbench.nb.mql.query;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of a metrics query.
 *
 * @param columns List of column names in order
 * @param rows List of row data as maps (column name -> value)
 * @param executedSQL The SQL query that was executed (for debugging/explain)
 * @param executionTimeMs Time taken to execute the query in milliseconds
 */
public record QueryResult(
    List<String> columns,
    List<Map<String, Object>> rows,
    String executedSQL,
    long executionTimeMs
) {
    /**
     * Get the number of rows returned.
     */
    public int rowCount() {
        return rows != null ? rows.size() : 0;
    }

    /**
     * Check if the result is empty.
     */
    public boolean isEmpty() {
        return rowCount() == 0;
    }
}
