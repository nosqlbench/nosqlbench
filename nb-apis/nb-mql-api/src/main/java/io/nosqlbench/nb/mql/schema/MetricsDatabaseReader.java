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

package io.nosqlbench.nb.mql.schema;

import org.sqlite.SQLiteConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages read-only connections to NoSQLBench SQLite metrics databases.
 * Supports querying both live sessions (via logs/metrics.db symlink) and
 * historical session files.
 */
public class MetricsDatabaseReader {

    private static final Path DEFAULT_DB_PATH = Path.of("logs/metrics.db");
    private static final int DEFAULT_BUSY_TIMEOUT_MS = 5000;

    static {
        // Explicitly load SQLite JDBC driver for standalone JAR usage
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    /**
     * Connect to the default metrics database (logs/metrics.db).
     * This typically points to the current session via symlink.
     *
     * @return Read-only database connection
     * @throws SQLException If connection fails or database doesn't exist
     */
    public static Connection connect() throws SQLException {
        return connect(DEFAULT_DB_PATH);
    }

    /**
     * Connect to a specific metrics database file.
     *
     * @param dbPath Path to the database file (can be a symlink)
     * @return Read-only database connection
     * @throws SQLException If connection fails or database doesn't exist
     */
    public static Connection connect(Path dbPath) throws SQLException {
        return connect(dbPath, DEFAULT_BUSY_TIMEOUT_MS);
    }

    /**
     * Connect to a specific metrics database with custom busy timeout.
     *
     * @param dbPath Path to the database file
     * @param busyTimeoutMs Timeout in milliseconds for handling database locks
     * @return Read-only database connection
     * @throws SQLException If connection fails or database doesn't exist
     */
    public static Connection connect(Path dbPath, int busyTimeoutMs) throws SQLException {
        Path resolvedPath = (dbPath != null) ? dbPath : DEFAULT_DB_PATH;

        // Check if database file exists
        if (!Files.exists(resolvedPath)) {
            throw new SQLException(
                "Database not found at: " + resolvedPath.toAbsolutePath() + "\n" +
                "Hint: Ensure a NoSQLBench session is running or specify a session file with --db <path>"
            );
        }

        // Build JDBC URL
        String jdbcUrl = "jdbc:sqlite:" + resolvedPath.toAbsolutePath();

        // Configure SQLite for read-only access
        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        config.setBusyTimeout(busyTimeoutMs);

        // Create connection with config
        return DriverManager.getConnection(jdbcUrl, config.toProperties());
    }

    /**
     * Connect to a database specified by JDBC URL string.
     * Useful for testing with in-memory databases.
     *
     * @param jdbcUrl JDBC URL (e.g., "jdbc:sqlite::memory:")
     * @return Read-only database connection
     * @throws SQLException If connection fails
     */
    public static Connection connectByUrl(String jdbcUrl) throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        config.setBusyTimeout(DEFAULT_BUSY_TIMEOUT_MS);

        return DriverManager.getConnection(jdbcUrl, config.toProperties());
    }

    private MetricsDatabaseReader() {
        // Utility class - prevent instantiation
    }
}
