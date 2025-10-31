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

package io.nosqlbench.nb.mql.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Helper class for adding regex support to SQLite connections.
 *
 * <p>SQLite doesn't have built-in regex support, but allows custom functions.
 * This class registers a REGEXP function that can be used in SQL queries.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * Connection conn = DriverManager.getConnection("jdbc:sqlite:...");
 * RegexHelper.enableRegex(conn);
 * // Now can use: WHERE column REGEXP 'pattern'
 * }</pre>
 */
public class RegexHelper {
    private static final Logger logger = LoggerFactory.getLogger(RegexHelper.class);

    /**
     * Enables regex support on a SQLite connection by registering a REGEXP function.
     *
     * @param conn The SQLite connection
     * @throws SQLException if registration fails
     */
    public static void enableRegex(Connection conn) throws SQLException {
        try {
            // Create a custom REGEXP function using SQLite's function API
            org.sqlite.Function.create(conn, "REGEXP", new org.sqlite.Function() {
                @Override
                protected void xFunc() throws SQLException {
                    String regex = value_text(0);
                    String text = value_text(1);

                    if (regex == null || text == null) {
                        result(0);
                        return;
                    }

                    try {
                        Pattern pattern = Pattern.compile(regex);
                        result(pattern.matcher(text).find() ? 1 : 0);
                    } catch (Exception e) {
                        logger.warn("Regex pattern compilation failed: {}", regex, e);
                        result(0);
                    }
                }
            });
            logger.debug("REGEXP function registered successfully");
        } catch (Exception e) {
            logger.error("Failed to register REGEXP function", e);
            throw new SQLException("Could not enable regex support", e);
        }
    }

    /**
     * Converts simple glob patterns to SQLite LIKE patterns.
     * Useful for simple cases like "prefix*" or "*suffix".
     *
     * @param regex The regex pattern
     * @return LIKE pattern if convertible, null otherwise
     */
    public static String tryConvertToLike(String regex) {
        // Simple conversions for common patterns
        if (regex.matches("^\\w+\\*$")) {
            // prefix* -> prefix%
            return regex.replace("*", "%");
        } else if (regex.matches("^\\*\\w+$")) {
            // *suffix -> %suffix
            return regex.replace("*", "%");
        } else if (regex.matches("^\\w+$")) {
            // exact -> exact (no wildcards)
            return regex;
        }

        return null;  // Can't convert to LIKE, need regex
    }
}
