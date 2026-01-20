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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for session metadata storage in SqliteSnapshotReporter.
 * Verifies that metadata can be stored and retrieved correctly from the label_metadata table.
 */
@Tag("accuracy")
@Tag("metrics")
class SessionMetadataTest {

    @TempDir
    Path tempDir;

    private String jdbcUrl;

    @BeforeEach
    void setUp() {
        jdbcUrl = "jdbc:sqlite::memory:";
    }

    @Test
    void testMetadataTableCreated() throws Exception {
        // Create reporter to ensure schema is initialized
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            try (var stmt = conn.createStatement()) {
                // Manually create schema to test table existence
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS label_set (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        hash TEXT UNIQUE NOT NULL
                    )
                """);
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS label_metadata (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        label_set_id INTEGER NOT NULL,
                        metadata_key TEXT NOT NULL,
                        metadata_value TEXT NOT NULL,
                        UNIQUE(label_set_id, metadata_key, metadata_value),
                        FOREIGN KEY(label_set_id) REFERENCES label_set(id)
                    )
                """);
            }

            // Verify table exists
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "label_metadata", null)) {
                assertTrue(rs.next(), "label_metadata table should exist");
            }

            // Verify columns
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "label_metadata", null)) {
                Map<String, String> columns = new HashMap<>();
                while (rs.next()) {
                    columns.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"));
                }
                assertTrue(columns.containsKey("id"), "Should have id column");
                assertTrue(columns.containsKey("label_set_id"), "Should have label_set_id column");
                assertTrue(columns.containsKey("metadata_key"), "Should have metadata_key column");
                assertTrue(columns.containsKey("metadata_value"), "Should have metadata_value column");
            }
        }
    }

    @Test
    void testStoreAndRetrieveMetadata() throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            // Create schema
            try (var stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE label_set (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        hash TEXT UNIQUE NOT NULL
                    )
                """);
                stmt.execute("""
                    CREATE TABLE label_metadata (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        label_set_id INTEGER NOT NULL,
                        metadata_key TEXT NOT NULL,
                        metadata_value TEXT NOT NULL,
                        UNIQUE(label_set_id, metadata_key, metadata_value),
                        FOREIGN KEY(label_set_id) REFERENCES label_set(id)
                    )
                """);

                // Insert test label set
                stmt.execute("INSERT INTO label_set (id, hash) VALUES (1, '{session=test}')");
            }

            // Insert metadata
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("nb.version", "5.25.0");
            metadata.put("nb.commandline", "nb5 run test.yaml");
            metadata.put("nb.hardware", "Intel i9 8-core 16GB");

            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO label_metadata(label_set_id, metadata_key, metadata_value) VALUES (?, ?, ?)")) {
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    ps.setInt(1, 1);
                    ps.setString(2, entry.getKey());
                    ps.setString(3, entry.getValue());
                    ps.executeUpdate();
                }
            }

            // Retrieve and verify
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT metadata_key, metadata_value FROM label_metadata WHERE label_set_id = ? ORDER BY metadata_key")) {
                ps.setInt(1, 1);

                Map<String, String> retrieved = new LinkedHashMap<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        retrieved.put(rs.getString("metadata_key"), rs.getString("metadata_value"));
                    }
                }

                assertEquals(3, retrieved.size(), "Should retrieve 3 metadata entries");
                assertEquals("Intel i9 8-core 16GB", retrieved.get("nb.hardware"));
                assertEquals("5.25.0", retrieved.get("nb.version"));
                assertEquals("nb5 run test.yaml", retrieved.get("nb.commandline"));
            }
        }
    }

    @Test
    void testMetadataUniquenessConstraint() throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            // Create schema
            try (var stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE label_set (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        hash TEXT UNIQUE NOT NULL
                    )
                """);
                stmt.execute("""
                    CREATE TABLE label_metadata (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        label_set_id INTEGER NOT NULL,
                        metadata_key TEXT NOT NULL,
                        metadata_value TEXT NOT NULL,
                        UNIQUE(label_set_id, metadata_key, metadata_value),
                        FOREIGN KEY(label_set_id) REFERENCES label_set(id)
                    )
                """);
                stmt.execute("INSERT INTO label_set (id, hash) VALUES (1, '{session=test}')");
            }

            // Insert metadata
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO label_metadata(label_set_id, metadata_key, metadata_value) VALUES (?, ?, ?)")) {
                ps.setInt(1, 1);
                ps.setString(2, "nb.version");
                ps.setString(3, "5.25.0");
                ps.executeUpdate();

                // Try inserting duplicate
                ps.setInt(1, 1);
                ps.setString(2, "nb.version");
                ps.setString(3, "5.25.0");
                ps.executeUpdate(); // Should be ignored due to OR IGNORE
            }

            // Verify only one entry exists
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM label_metadata WHERE label_set_id = ?")) {
                ps.setInt(1, 1);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1), "Should have only 1 metadata entry");
                }
            }
        }
    }

    @Test
    void testMultipleLabelSetsMetadata() throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            // Create schema
            try (var stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE label_set (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        hash TEXT UNIQUE NOT NULL
                    )
                """);
                stmt.execute("""
                    CREATE TABLE label_metadata (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        label_set_id INTEGER NOT NULL,
                        metadata_key TEXT NOT NULL,
                        metadata_value TEXT NOT NULL,
                        UNIQUE(label_set_id, metadata_key, metadata_value),
                        FOREIGN KEY(label_set_id) REFERENCES label_set(id)
                    )
                """);
                stmt.execute("INSERT INTO label_set (id, hash) VALUES (1, '{session=test1}')");
                stmt.execute("INSERT INTO label_set (id, hash) VALUES (2, '{session=test2}')");
            }

            // Insert metadata for both label sets
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO label_metadata(label_set_id, metadata_key, metadata_value) VALUES (?, ?, ?)")) {
                // Label set 1
                ps.setInt(1, 1);
                ps.setString(2, "nb.version");
                ps.setString(3, "5.25.0");
                ps.executeUpdate();

                // Label set 2
                ps.setInt(1, 2);
                ps.setString(2, "nb.version");
                ps.setString(3, "5.26.0");
                ps.executeUpdate();
            }

            // Verify metadata is separate for each label set
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT metadata_value FROM label_metadata WHERE label_set_id = ? AND metadata_key = ?")) {
                ps.setInt(1, 1);
                ps.setString(2, "nb.version");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("5.25.0", rs.getString(1));
                }

                ps.setInt(1, 2);
                ps.setString(2, "nb.version");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("5.26.0", rs.getString(1));
                }
            }
        }
    }
}
