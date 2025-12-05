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

package io.nosqlbench.docsys.apps.doclint;

import io.nosqlbench.docsys.apps.docinventory.DocInventoryApp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocLintAppTest {

    @Test
    public void doclintRunsAgainstGeneratedInventory() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path moduleRoot = Paths.get("").toAbsolutePath().normalize();
        Path targetInventory = moduleRoot.resolve("target/docs_inventory.json");

        int inventoryExit = new DocInventoryApp().applyAsInt(new String[] {
            "--root", repoRoot.toString(),
            "--output", targetInventory.toString()
        });
        assertEquals(0, inventoryExit, "docs-inventory app failed to run for doclint test.");

        int lintExit = new DocLintApp().applyAsInt(new String[0]);
        Path reportPath = moduleRoot.resolve("target/doclint-report.json");
        assertTrue(Files.exists(reportPath), "doclint report missing");
        if (lintExit != 0) {
            throw new AssertionError(buildFailureMessage(reportPath));
        }
    }

    private static Path locateRepoRoot() throws IOException {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IOException("Unable to locate repository root for doclint tests.");
    }

    private static String buildFailureMessage(Path reportPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode report = mapper.readTree(reportPath.toFile());
        int errorCount = report.path("errors").asInt();
        StringBuilder sb = new StringBuilder();
        sb.append("doclint found ").append(errorCount).append(" issue(s).\n");

        JsonNode byFile = report.path("by_file");
        if (byFile.isObject() && byFile.size() > 0) {
            List<String> paths = new ArrayList<>();
            byFile.fieldNames().forEachRemaining(paths::add);
            Collections.sort(paths);
            int fileLimit = Math.min(10, paths.size());
            for (int i = 0; i < fileLimit; i++) {
                String path = paths.get(i);
                List<String> fileErrors = new ArrayList<>();
                byFile.path(path).forEach(node -> fileErrors.add(node.asText()));
                sb.append("  ").append(path).append(":\n");
                int perFileLimit = Math.min(5, fileErrors.size());
                for (int j = 0; j < perFileLimit; j++) {
                    sb.append("    - ").append(fileErrors.get(j)).append('\n');
                }
                if (fileErrors.size() > perFileLimit) {
                    sb.append("    ... (").append(fileErrors.size() - perFileLimit).append(" more)\n");
                }
            }
            if (paths.size() > fileLimit) {
                sb.append("  ... (").append(paths.size() - fileLimit).append(" more files)\n");
            }
        } else {
            List<String> messages = new ArrayList<>();
            report.path("messages").forEach(node -> {
                String text = node.asText();
                if (!text.isBlank()) {
                    messages.add(text);
                }
            });
            int limit = Math.min(10, messages.size());
            for (int i = 0; i < limit; i++) {
                sb.append("  - ").append(messages.get(i)).append('\n');
            }
            if (messages.size() > limit) {
                sb.append("  ... (").append(messages.size() - limit).append(" more)\n");
            }
        }
        sb.append("See ").append(reportPath.toAbsolutePath().normalize()).append(" for full details.");
        return sb.toString();
    }
}
