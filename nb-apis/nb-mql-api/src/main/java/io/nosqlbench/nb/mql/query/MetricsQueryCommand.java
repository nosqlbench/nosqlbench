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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Interface for all MetricsQL query commands.
 * Each command translates high-level query parameters into SQL and executes against
 * the NoSQLBench metrics database.
 */
public interface MetricsQueryCommand {

    /**
     * Get the command name (e.g., "rate", "instant", "agg").
     */
    String getName();

    /**
     * Get a human-readable description of what this command does.
     */
    String getDescription();

    /**
     * Execute the query command.
     *
     * @param conn Database connection
     * @param params Query parameters (metric name, time windows, labels, etc.)
     * @return Query result with columns and rows
     * @throws SQLException If query execution fails
     * @throws InvalidQueryException If parameters are invalid
     */
    QueryResult execute(Connection conn, Map<String, Object> params) throws SQLException, InvalidQueryException;

    /**
     * Validate query parameters before execution.
     * Should throw InvalidQueryException with clear error messages if parameters are invalid.
     *
     * @param params Query parameters to validate
     * @throws InvalidQueryException If parameters are invalid
     */
    void validate(Map<String, Object> params) throws InvalidQueryException;

    /**
     * Get usage examples for this command.
     * Used for help text and documentation.
     */
    default String getUsageExamples() {
        return "No examples available.";
    }
}
