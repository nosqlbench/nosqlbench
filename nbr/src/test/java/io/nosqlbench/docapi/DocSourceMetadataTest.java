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

package io.nosqlbench.docapi;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures that any markdown files declaring a {@code source:} attribute in their front matter
 * live at the path they advertise. This protects the contract used by doc exporters and the
 * documentation projection tooling.
 */
public class DocSourceMetadataTest {

    private static final String FRONT_MATTER_DELIMITER = "---";

    @Test
    public void sourceMetadataMatchesCanonicalPath() throws IOException {
        final Path moduleRoot = Paths.get(".").toAbsolutePath().normalize();
        final Path repoRoot = moduleRoot.getParent();
        if (repoRoot == null) {
            throw new IllegalStateException("Unable to resolve repository root from " + moduleRoot);
        }

        final List<Path> docRoots = List.of(
            repoRoot.resolve("nb-adapters"),
            repoRoot.resolve("nbr/src/main/resources")
        );

        final List<String> mismatches = new ArrayList<>();

        for (Path docRoot : docRoots) {
            if (!Files.exists(docRoot)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(docRoot, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)) {
                files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".md"))
                    .filter(p -> !p.toString().contains("/target/"))
                    .forEach(mdPath -> {
                        try {
                            Optional<String> declaredSource = extractSource(mdPath);
                            declaredSource.ifPresent(sourceValue -> {
                                String actual = repoRoot.relativize(mdPath.toAbsolutePath()).toString()
                                    .replace('\\', '/');
                                if (!sourceValue.equals(actual)) {
                                    mismatches.add(actual + " declares source='" + sourceValue + "'");
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException("Error reading " + mdPath, e);
                        }
                    });
            }
        }

        assertTrue(
            mismatches.isEmpty(),
            () -> "Source metadata mismatches:\n" + String.join("\n", mismatches)
        );
    }

    private static Optional<String> extractSource(Path path) throws IOException {
        final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            return Optional.empty();
        }
        if (!lines.get(0).trim().equals(FRONT_MATTER_DELIMITER)) {
            return Optional.empty();
        }
        StringBuilder frontMatter = new StringBuilder();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().equals(FRONT_MATTER_DELIMITER)) {
                break;
            }
            frontMatter.append(line).append('\n');
        }
        String fm = frontMatter.toString();
        if (fm.isEmpty()) {
            return Optional.empty();
        }
        return fm.lines()
            .map(String::trim)
            .filter(l -> l.startsWith("source:"))
            .map(l -> l.substring("source:".length()).trim())
            .map(value -> value.replace("\"", ""))
            .findFirst();
    }
}
