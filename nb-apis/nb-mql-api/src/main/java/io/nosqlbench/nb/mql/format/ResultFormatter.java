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

/**
 * Interface for formatting query results in different output formats.
 */
public interface ResultFormatter {

    /**
     * Format a query result as a string.
     *
     * @param result The query result to format
     * @return Formatted string representation
     */
    String format(QueryResult result);

    /**
     * Get the name of this formatter (e.g., "table", "json", "csv").
     *
     * @return Formatter name
     */
    String getName();

    /**
     * Get the file extension for this format (e.g., "txt", "json", "csv").
     *
     * @return File extension without the dot
     */
    String getFileExtension();
}
