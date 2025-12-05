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

package io.nosqlbench.docsys.apps.docinventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures every Markdown file tracked in {@code docs/docs_inventory.json} lives under
 * a root authorized by {@code docs/docs_tree.md}. Any file added outside the chartered
 * tree should be relocated before committing.
 */
public class DocumentationTreeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Set<String> ROOT_FILES = Set.of(
        "README.md",
        "DOWNLOADS.md",
        "PREVIEW_NOTES.md",
        "RELEASE_NOTES.md",
        "BUILDING.md",
        "CONTRIBUTING.md",
        "CODE_OF_CONDUCT.md",
        "CONVENTIONS.md",
        "MODULES.md",
        "arch_planning.md",
        "branch_docsplan.md",
        "metricsqlite-plan.md",
        "nb_521.md",
        "nb_523.md"
    );

    private static final List<Predicate<String>> ALLOWLIST = List.of(
        path -> path.startsWith("docs/"),
        path -> path.startsWith("devdocs/"),
        path -> path.startsWith("nb-adapters/"),
        path -> path.startsWith("nb-virtdata/"),
        path -> path.startsWith("nb-engine/"),
        path -> path.startsWith("nb-apis/"),
        path -> path.startsWith("nb5/"),
        path -> path.startsWith("nb-spectest/"),
        path -> path.startsWith("nbr/src/main/resources/"),
        path -> path.startsWith("nbr-demos/"),
        path -> path.startsWith("sort_docs/")
    );

    @Test
    public void maintainedDocsStayWithinCharteredRoots() throws IOException {
        Path repoRoot = locateRepoRoot();
        Path inventoryPath = repoRoot.resolve("docs/docs_inventory.json");
        if (!Files.exists(inventoryPath)) {
            throw new IllegalStateException("Missing docs/docs_inventory.json. Run nb app docs-inventory first.");
        }

        JsonNode docs = MAPPER.readTree(inventoryPath.toFile()).path("docs");
        List<String> rootViolations = new ArrayList<>();
        List<String> originViolations = new ArrayList<>();
        for (JsonNode doc : docs) {
            String path = doc.path("path").asText();
            if (path.startsWith("local/")) {
                // Local is expressly excluded scratch space; ignore it here so CI focuses on maintained roots.
                continue;
            }
            if (!isAllowed(path)) {
                rootViolations.add(path);
            }
            String expectedModule = expectedModuleFor(path);
            if (expectedModule != null) {
                String actualModule = doc.path("origin").path("module").asText();
                if (!expectedModule.equals(actualModule)) {
                    originViolations.add(path + " declares origin.module='" + actualModule + "' (expected '" + expectedModule + "')");
                }
            }
        }

        assertTrue(
            rootViolations.isEmpty(),
            () -> "Docs outside chartered roots:\n" + String.join("\n", rootViolations)
                + "\nSee docs/docs_tree.md for approved locations."
        );

        assertTrue(
            originViolations.isEmpty(),
            () -> "Docs with mismatched origin.module values:\n" + String.join("\n", originViolations)
                + "\nEnsure origin metadata reflects the owning module/root."
        );
    }

    private static boolean isAllowed(String path) {
        if (ROOT_FILES.contains(path)) {
            return true;
        }
        for (Predicate<String> rule : ALLOWLIST) {
            if (rule.test(path)) {
                return true;
            }
        }
        return false;
    }

    private static String expectedModuleFor(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (!path.contains("/")) {
            // Historical root files record their own filename as the module.
            return path;
        }
        int slash = path.indexOf('/');
        return path.substring(0, slash);
    }

    private static Path locateRepoRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isDirectory(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to locate repository root (missing .git directory).");
    }
}
