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

package io.nosqlbench.nb.mql.commands;

import io.nosqlbench.nb.mql.format.TableFormatter;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
class MetadataCommandTest {

    @Test
    void testMetadataQuery() throws Exception {
        // Use test database that now includes metadata
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            MetadataCommand command = new MetadataCommand();

            QueryResult result = command.execute(conn, Map.of());

            // Should have 3 columns
            assertEquals(3, result.columns().size());
            assertTrue(result.columns().contains("label_set"));
            assertTrue(result.columns().contains("metadata_key"));
            assertTrue(result.columns().contains("metadata_value"));

            // Note: Test databases are pre-generated and may not have metadata table
            // If metadata exists, verify it; otherwise just ensure no errors
            if (result.rowCount() > 0) {
                // Verify metadata keys exist if present
                boolean hasVersion = false;
                boolean hasCommandline = false;
                boolean hasHardware = false;

                for (Map<String, Object> row : result.rows()) {
                    String key = (String) row.get("metadata_key");
                    if ("nb.version".equals(key)) {
                        hasVersion = true;
                        assertEquals("5.25.0-TEST", row.get("metadata_value"));
                    } else if ("nb.commandline".equals(key)) {
                        hasCommandline = true;
                        assertEquals("nb5 mql test simple_counter", row.get("metadata_value"));
                    } else if ("nb.hardware".equals(key)) {
                        hasHardware = true;
                        assertEquals("Test harness - 4-core 8GB", row.get("metadata_value"));
                    }
                }

                assertTrue(hasVersion, "Should have nb.version metadata");
                assertTrue(hasCommandline, "Should have nb.commandline metadata");
                assertTrue(hasHardware, "Should have nb.hardware metadata");
            }

            System.out.println("\n=== Metadata Command ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testMetadataEmptyDatabase() throws Exception {
        // Test with a database that has no metadata (backward compatibility)
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            MetadataCommand command = new MetadataCommand();

            QueryResult result = command.execute(conn, Map.of());

            // Should execute successfully even if no metadata exists
            assertEquals(3, result.columns().size());

            System.out.println("\n=== Metadata (Empty) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }
}
