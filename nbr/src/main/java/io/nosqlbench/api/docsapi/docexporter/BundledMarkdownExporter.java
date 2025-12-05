/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.api.docsapi.docexporter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nosqlbench.nb.api.apps.BundledApp;
import io.nosqlbench.nb.annotations.Service;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service(value=BundledApp.class,selector = "docs-export")
public class BundledMarkdownExporter implements BundledApp {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        new BundledMarkdownExporter().applyAsInt(args);

    }
    @Override
    public int applyAsInt(String[] args) {
        final OptionParser parser = new OptionParser();

        OptionSpec<String> zipfileSpec = parser.accepts("zipfile", "zip file to write to")
            .withOptionalArg().ofType(String.class).defaultsTo("exported_docs.zip");
        OptionSpec<String> inventorySpec = parser.accepts("inventory", "docs inventory json produced by docs-inventory")
            .withOptionalArg().ofType(String.class).defaultsTo("nb-docsys/target/docs_inventory.json");
        OptionSpec<String> doclintSpec = parser.accepts("doclint-report", "doclint JSON report to embed (must show zero errors)")
            .withOptionalArg().ofType(String.class).defaultsTo("nb-docsys/target/doclint-report.json");
        OptionSpec<?> forceSpec = parser.acceptsAll(List.of("force", "f"), "Overwrite existing output zip if present");

        OptionSpec<String> dirSpec = parser.accepts("dir", "directory to export to (e.g., docs/)")
            .withOptionalArg().ofType(String.class);

        OptionSpec<Void> tomlSpec = parser.accepts("toml", "use TOML front matter instead of YAML (for Zola)");

        OptionSpec<?> helpSpec = parser.acceptsAll(List.of("help", "h", "?"), "Display help").forHelp();
        OptionSet options = parser.parse(args);
        if (options.has(helpSpec)) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                throw new RuntimeException("Unable to show help:" + e);
            }
        }

        String zipfile = options.valueOf(zipfileSpec);
        Path outputPath = Path.of(zipfile).toAbsolutePath().normalize();
        if (Files.exists(outputPath) && !options.has(forceSpec)) {
            throw new IllegalStateException("Output file already exists: " + outputPath + " (use -f to overwrite)");
        }
        try {
            Files.deleteIfExists(outputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to overwrite existing output: " + outputPath, e);
        }

        Path inventoryPath = resolveInventoryPath(Path.of(options.valueOf(inventorySpec)));
        Path doclintPath = resolveDoclintPath(Path.of(options.valueOf(doclintSpec)));

        ensureInventoryReady(inventoryPath);
        ensureDoclintClean(doclintPath);

        Map<String, Path> metadata = new LinkedHashMap<>();
        metadata.put("metadata/docs_inventory.json", inventoryPath);
        if (Files.exists(doclintPath)) {
            metadata.put("metadata/doclint-report.json", doclintPath);
        }

        new BundledMarkdownZipExporter(new BundledFrontmatterInjector(1000,100))
            .exportDocs(Path.of(zipfile), metadata);

        return 0;
    }

    private Path resolveInventoryPath(Path requested) {
        Path candidate = absolutize(requested);
        if (Files.exists(candidate)) {
            return candidate;
        }
        for (Path fallback : List.of(
            Paths.get("nb-docsys/target/docs_inventory.json"),
            Paths.get("docs/docs_inventory.json")
        )) {
            Path resolved = absolutize(fallback);
            if (Files.exists(resolved)) {
                return resolved;
            }
        }
        throw new IllegalStateException("Missing docs inventory at " + candidate +
            " (also checked nb-docsys/target/docs_inventory.json and docs/docs_inventory.json)");
    }

    private Path resolveDoclintPath(Path requested) {
        Path candidate = absolutize(requested);
        if (Files.exists(candidate)) {
            return candidate;
        }
        for (Path fallback : List.of(
            Paths.get("nb-docsys/target/doclint-report.json"),
            Paths.get("target/doclint-report.json")
        )) {
            Path resolved = absolutize(fallback);
            if (Files.exists(resolved)) {
                return resolved;
            }
        }
        throw new IllegalStateException("Missing doclint report at " + candidate +
            " (also checked nb-docsys/target/doclint-report.json and target/doclint-report.json)");
    }

    private Path absolutize(Path path) {
        return path.isAbsolute() ? path.normalize() : Path.of("").toAbsolutePath().resolve(path).normalize();
    }

    private void ensureInventoryReady(Path inventoryPath) {
        if (!Files.exists(inventoryPath)) {
            throw new IllegalStateException("Missing docs inventory at " + inventoryPath + " (run `nb app docs-inventory --root .` first).");
        }
    }

    private void ensureDoclintClean(Path doclintPath) {
        if (!Files.exists(doclintPath)) {
            throw new IllegalStateException("Missing doclint report at " + doclintPath + " (run doclint before exporting).");
        }
        try {
            JsonNode report = MAPPER.readTree(doclintPath.toFile());
            int errors = report.path("errors").asInt();
            if (errors > 0) {
                throw new IllegalStateException(formatDoclintFailure(report));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read doclint report: " + doclintPath, e);
        }
    }

    private String formatDoclintFailure(JsonNode report) {
        StringBuilder sb = new StringBuilder();
        int errors = report.path("errors").asInt();
        sb.append("doclint report contains ").append(errors).append(" error(s):\n");

        JsonNode byFile = report.path("by_file");
        if (byFile.isObject() && byFile.size() > 0) {
            List<String> paths = new ArrayList<>();
            byFile.fieldNames().forEachRemaining(paths::add);
            Collections.sort(paths);
            int fileLimit = Math.min(10, paths.size());
            for (int i = 0; i < fileLimit; i++) {
                String path = paths.get(i);
                List<String> perFile = new ArrayList<>();
                byFile.path(path).forEach(node -> perFile.add(node.asText()));
                sb.append("  ").append(path).append(":\n");
                int perFileLimit = Math.min(5, perFile.size());
                for (int j = 0; j < perFileLimit; j++) {
                    sb.append("    - ").append(perFile.get(j)).append('\n');
                }
                if (perFile.size() > perFileLimit) {
                    sb.append("    ... (").append(perFile.size() - perFileLimit).append(" more)\n");
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

        sb.append("Fix the issues above before running docs-export.");
        return sb.toString();
    }
}
